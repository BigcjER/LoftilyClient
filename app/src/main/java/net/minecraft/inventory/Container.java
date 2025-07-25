package net.minecraft.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class Container
{
    public NonNullList<ItemStack> inventoryItemStacks = NonNullList.<ItemStack>func_191196_a();
    public List<Slot> inventorySlots = Lists.<Slot>newArrayList();
    public int windowId;
    private short transactionID;

    /**
     * The current drag mode (0 : evenly split, 1 : one item by slot, 2 : not used ?)
     */
    private int dragMode = -1;

    /** The current drag event (0 : start, 1 : add slot : 2 : end) */
    private int dragEvent;
    private final Set<Slot> dragSlots = Sets.<Slot>newHashSet();
    protected List<IContainerListener> listeners = Lists.<IContainerListener>newArrayList();
    private final Set<EntityPlayer> playerList = Sets.<EntityPlayer>newHashSet();

    /**
     * Adds an item slot to this container
     */
    protected Slot addSlotToContainer(Slot slotIn)
    {
        slotIn.slotNumber = this.inventorySlots.size();
        this.inventorySlots.add(slotIn);
        this.inventoryItemStacks.add(ItemStack.field_190927_a);
        return slotIn;
    }

    public void addListener(IContainerListener listener)
    {
        if (this.listeners.contains(listener))
        {
            throw new IllegalArgumentException("Listener already listening");
        }
        else
        {
            this.listeners.add(listener);
            listener.updateCraftingInventory(this, this.getInventory());
            this.detectAndSendChanges();
        }
    }

    /**
     * Remove the given Listener. Method name is for legacy.
     */
    public void removeListener(IContainerListener listener)
    {
        this.listeners.remove(listener);
    }

    public NonNullList<ItemStack> getInventory()
    {
        NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>func_191196_a();

        for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
            nonnulllist.add(((Slot)this.inventorySlots.get(i)).getStack());
        }

        return nonnulllist;
    }

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
    public void detectAndSendChanges()
    {
        for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
            ItemStack itemstack = ((Slot)this.inventorySlots.get(i)).getStack();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack))
            {
                itemstack1 = itemstack.isEmptyStack() ? ItemStack.field_190927_a : itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);

                for (int j = 0; j < this.listeners.size(); ++j)
                {
                    ((IContainerListener)this.listeners.get(j)).sendSlotContents(this, i, itemstack1);
                }
            }
        }
    }

    /**
     * Handles the given Button-click on the server, currently only used by enchanting. Name is for legacy.
     */
    public boolean enchantItem(EntityPlayer playerIn, int id)
    {
        return false;
    }

    @Nullable
    public Slot getSlotFromInventory(IInventory inv, int slotIn)
    {
        for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
            Slot slot = this.inventorySlots.get(i);

            if (slot.isHere(inv, slotIn))
            {
                return slot;
            }
        }

        return null;
    }

    public Slot getSlot(int slotId)
    {
        return this.inventorySlots.get(slotId);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        Slot slot = this.inventorySlots.get(index);
        return slot != null ? slot.getStack() : ItemStack.field_190927_a;
    }

    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        ItemStack itemstack = ItemStack.field_190927_a;
        InventoryPlayer inventoryplayer = player.inventory;

        if (clickTypeIn == ClickType.QUICK_CRAFT)
        {
            int j1 = this.dragEvent;
            this.dragEvent = getDragEvent(dragType);

            if ((j1 != 1 || this.dragEvent != 2) && j1 != this.dragEvent)
            {
                this.resetDrag();
            }
            else if (inventoryplayer.getItemStack().isEmptyStack())
            {
                this.resetDrag();
            }
            else if (this.dragEvent == 0)
            {
                this.dragMode = extractDragMode(dragType);

                if (isValidDragMode(this.dragMode, player))
                {
                    this.dragEvent = 1;
                    this.dragSlots.clear();
                }
                else
                {
                    this.resetDrag();
                }
            }
            else if (this.dragEvent == 1)
            {
                Slot slot7 = this.inventorySlots.get(slotId);
                ItemStack itemstack12 = inventoryplayer.getItemStack();

                if (slot7 != null && canAddItemToSlot(slot7, itemstack12, true) && slot7.isItemValid(itemstack12) && (this.dragMode == 2 || itemstack12.func_190916_E() > this.dragSlots.size()) && this.canDragIntoSlot(slot7))
                {
                    this.dragSlots.add(slot7);
                }
            }
            else if (this.dragEvent == 2)
            {
                if (!this.dragSlots.isEmpty())
                {
                    ItemStack itemstack9 = inventoryplayer.getItemStack().copy();
                    int k1 = inventoryplayer.getItemStack().func_190916_E();

                    for (Slot slot8 : this.dragSlots)
                    {
                        ItemStack itemstack13 = inventoryplayer.getItemStack();

                        if (slot8 != null && canAddItemToSlot(slot8, itemstack13, true) && slot8.isItemValid(itemstack13) && (this.dragMode == 2 || itemstack13.func_190916_E() >= this.dragSlots.size()) && this.canDragIntoSlot(slot8))
                        {
                            ItemStack itemstack14 = itemstack9.copy();
                            int j3 = slot8.getHasStack() ? slot8.getStack().func_190916_E() : 0;
                            computeStackSize(this.dragSlots, this.dragMode, itemstack14, j3);
                            int k3 = Math.min(itemstack14.getMaxStackSize(), slot8.getItemStackLimit(itemstack14));

                            if (itemstack14.func_190916_E() > k3)
                            {
                                itemstack14.func_190920_e(k3);
                            }

                            k1 -= itemstack14.func_190916_E() - j3;
                            slot8.putStack(itemstack14);
                        }
                    }

                    itemstack9.func_190920_e(k1);
                    inventoryplayer.setItemStack(itemstack9);
                }

                this.resetDrag();
            }
            else
            {
                this.resetDrag();
            }
        }
        else if (this.dragEvent != 0)
        {
            this.resetDrag();
        }
        else if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1))
        {
            if (slotId == -999)
            {
                if (!inventoryplayer.getItemStack().isEmptyStack())
                {
                    if (dragType == 0)
                    {
                        player.dropItem(inventoryplayer.getItemStack(), true);
                        inventoryplayer.setItemStack(ItemStack.field_190927_a);
                    }

                    if (dragType == 1)
                    {
                        player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);
                    }
                }
            }
            else if (clickTypeIn == ClickType.QUICK_MOVE)
            {
                if (slotId < 0)
                {
                    return ItemStack.field_190927_a;
                }

                Slot slot5 = this.inventorySlots.get(slotId);

                if (slot5 == null || !slot5.canTakeStack(player))
                {
                    return ItemStack.field_190927_a;
                }

                for (ItemStack itemstack7 = this.transferStackInSlot(player, slotId); !itemstack7.isEmptyStack() && ItemStack.areItemsEqual(slot5.getStack(), itemstack7); itemstack7 = this.transferStackInSlot(player, slotId))
                {
                    itemstack = itemstack7.copy();
                }
            }
            else
            {
                if (slotId < 0)
                {
                    return ItemStack.field_190927_a;
                }

                Slot slot6 = this.inventorySlots.get(slotId);

                if (slot6 != null)
                {
                    ItemStack itemstack8 = slot6.getStack();
                    ItemStack itemstack11 = inventoryplayer.getItemStack();

                    if (!itemstack8.isEmptyStack())
                    {
                        itemstack = itemstack8.copy();
                    }

                    if (itemstack8.isEmptyStack())
                    {
                        if (!itemstack11.isEmptyStack() && slot6.isItemValid(itemstack11))
                        {
                            int i3 = dragType == 0 ? itemstack11.func_190916_E() : 1;

                            if (i3 > slot6.getItemStackLimit(itemstack11))
                            {
                                i3 = slot6.getItemStackLimit(itemstack11);
                            }

                            slot6.putStack(itemstack11.splitStack(i3));
                        }
                    }
                    else if (slot6.canTakeStack(player))
                    {
                        if (itemstack11.isEmptyStack())
                        {
                            if (itemstack8.isEmptyStack())
                            {
                                slot6.putStack(ItemStack.field_190927_a);
                                inventoryplayer.setItemStack(ItemStack.field_190927_a);
                            }
                            else
                            {
                                int l2 = dragType == 0 ? itemstack8.func_190916_E() : (itemstack8.func_190916_E() + 1) / 2;
                                inventoryplayer.setItemStack(slot6.decrStackSize(l2));

                                if (itemstack8.isEmptyStack())
                                {
                                    slot6.putStack(ItemStack.field_190927_a);
                                }

                                slot6.func_190901_a(player, inventoryplayer.getItemStack());
                            }
                        }
                        else if (slot6.isItemValid(itemstack11))
                        {
                            if (itemstack8.getItem() == itemstack11.getItem() && itemstack8.getMetadata() == itemstack11.getMetadata() && ItemStack.areItemStackTagsEqual(itemstack8, itemstack11))
                            {
                                int k2 = dragType == 0 ? itemstack11.func_190916_E() : 1;

                                if (k2 > slot6.getItemStackLimit(itemstack11) - itemstack8.func_190916_E())
                                {
                                    k2 = slot6.getItemStackLimit(itemstack11) - itemstack8.func_190916_E();
                                }

                                if (k2 > itemstack11.getMaxStackSize() - itemstack8.func_190916_E())
                                {
                                    k2 = itemstack11.getMaxStackSize() - itemstack8.func_190916_E();
                                }

                                itemstack11.func_190918_g(k2);
                                itemstack8.func_190917_f(k2);
                            }
                            else if (itemstack11.func_190916_E() <= slot6.getItemStackLimit(itemstack11))
                            {
                                slot6.putStack(itemstack11);
                                inventoryplayer.setItemStack(itemstack8);
                            }
                        }
                        else if (itemstack8.getItem() == itemstack11.getItem() && itemstack11.getMaxStackSize() > 1 && (!itemstack8.getHasSubtypes() || itemstack8.getMetadata() == itemstack11.getMetadata()) && ItemStack.areItemStackTagsEqual(itemstack8, itemstack11) && !itemstack8.isEmptyStack())
                        {
                            int j2 = itemstack8.func_190916_E();

                            if (j2 + itemstack11.func_190916_E() <= itemstack11.getMaxStackSize())
                            {
                                itemstack11.func_190917_f(j2);
                                itemstack8 = slot6.decrStackSize(j2);

                                if (itemstack8.isEmptyStack())
                                {
                                    slot6.putStack(ItemStack.field_190927_a);
                                }

                                slot6.func_190901_a(player, inventoryplayer.getItemStack());
                            }
                        }
                    }

                    slot6.onSlotChanged();
                }
            }
        }
        else if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9)
        {
            Slot slot4 = this.inventorySlots.get(slotId);
            ItemStack itemstack6 = inventoryplayer.getStackInSlot(dragType);
            ItemStack itemstack10 = slot4.getStack();

            if (!itemstack6.isEmptyStack() || !itemstack10.isEmptyStack())
            {
                if (itemstack6.isEmptyStack())
                {
                    if (slot4.canTakeStack(player))
                    {
                        inventoryplayer.setInventorySlotContents(dragType, itemstack10);
                        slot4.func_190900_b(itemstack10.func_190916_E());
                        slot4.putStack(ItemStack.field_190927_a);
                        slot4.func_190901_a(player, itemstack10);
                    }
                }
                else if (itemstack10.isEmptyStack())
                {
                    if (slot4.isItemValid(itemstack6))
                    {
                        int l1 = slot4.getItemStackLimit(itemstack6);

                        if (itemstack6.func_190916_E() > l1)
                        {
                            slot4.putStack(itemstack6.splitStack(l1));
                        }
                        else
                        {
                            slot4.putStack(itemstack6);
                            inventoryplayer.setInventorySlotContents(dragType, ItemStack.field_190927_a);
                        }
                    }
                }
                else if (slot4.canTakeStack(player) && slot4.isItemValid(itemstack6))
                {
                    int i2 = slot4.getItemStackLimit(itemstack6);

                    if (itemstack6.func_190916_E() > i2)
                    {
                        slot4.putStack(itemstack6.splitStack(i2));
                        slot4.func_190901_a(player, itemstack10);

                        if (!inventoryplayer.addItemStackToInventory(itemstack10))
                        {
                            player.dropItem(itemstack10, true);
                        }
                    }
                    else
                    {
                        slot4.putStack(itemstack6);
                        inventoryplayer.setInventorySlotContents(dragType, itemstack10);
                        slot4.func_190901_a(player, itemstack10);
                    }
                }
            }
        }
        else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode && inventoryplayer.getItemStack().isEmptyStack() && slotId >= 0)
        {
            Slot slot3 = this.inventorySlots.get(slotId);

            if (slot3 != null && slot3.getHasStack())
            {
                ItemStack itemstack5 = slot3.getStack().copy();
                itemstack5.func_190920_e(itemstack5.getMaxStackSize());
                inventoryplayer.setItemStack(itemstack5);
            }
        }
        else if (clickTypeIn == ClickType.THROW && inventoryplayer.getItemStack().isEmptyStack() && slotId >= 0)
        {
            Slot slot2 = this.inventorySlots.get(slotId);

            if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(player))
            {
                ItemStack itemstack4 = slot2.decrStackSize(dragType == 0 ? 1 : slot2.getStack().func_190916_E());
                slot2.func_190901_a(player, itemstack4);
                player.dropItem(itemstack4, true);
            }
        }
        else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0)
        {
            Slot slot = this.inventorySlots.get(slotId);
            ItemStack itemstack1 = inventoryplayer.getItemStack();

            if (!itemstack1.isEmptyStack() && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player)))
            {
                int i = dragType == 0 ? 0 : this.inventorySlots.size() - 1;
                int j = dragType == 0 ? 1 : -1;

                for (int k = 0; k < 2; ++k)
                {
                    for (int l = i; l >= 0 && l < this.inventorySlots.size() && itemstack1.func_190916_E() < itemstack1.getMaxStackSize(); l += j)
                    {
                        Slot slot1 = this.inventorySlots.get(l);

                        if (slot1.getHasStack() && canAddItemToSlot(slot1, itemstack1, true) && slot1.canTakeStack(player) && this.canMergeSlot(itemstack1, slot1))
                        {
                            ItemStack itemstack2 = slot1.getStack();

                            if (k != 0 || itemstack2.func_190916_E() != itemstack2.getMaxStackSize())
                            {
                                int i1 = Math.min(itemstack1.getMaxStackSize() - itemstack1.func_190916_E(), itemstack2.func_190916_E());
                                ItemStack itemstack3 = slot1.decrStackSize(i1);
                                itemstack1.func_190917_f(i1);

                                if (itemstack3.isEmptyStack())
                                {
                                    slot1.putStack(ItemStack.field_190927_a);
                                }

                                slot1.func_190901_a(player, itemstack3);
                            }
                        }
                    }
                }
            }

            this.detectAndSendChanges();
        }

        return itemstack;
    }

    /**
     * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in
     * is null for the initial slot that was double-clicked.
     */
    public boolean canMergeSlot(ItemStack stack, Slot slotIn)
    {
        return true;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer playerIn)
    {
        InventoryPlayer inventoryplayer = playerIn.inventory;

        if (!inventoryplayer.getItemStack().isEmptyStack())
        {
            playerIn.dropItem(inventoryplayer.getItemStack(), false);
            inventoryplayer.setItemStack(ItemStack.field_190927_a);
        }
    }

    protected void func_193327_a(EntityPlayer p_193327_1_, World p_193327_2_, IInventory p_193327_3_)
    {
        if (!p_193327_1_.isEntityAlive() || p_193327_1_ instanceof EntityPlayerMP && ((EntityPlayerMP)p_193327_1_).func_193105_t())
        {
            for (int j = 0; j < p_193327_3_.getSizeInventory(); ++j)
            {
                p_193327_1_.dropItem(p_193327_3_.removeStackFromSlot(j), false);
            }
        }
        else
        {
            for (int i = 0; i < p_193327_3_.getSizeInventory(); ++i)
            {
                p_193327_1_.inventory.func_191975_a(p_193327_2_, p_193327_3_.removeStackFromSlot(i));
            }
        }
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        this.detectAndSendChanges();
    }

    /**
     * Puts an ItemStack in a slot.
     */
    public void putStackInSlot(int slotID, ItemStack stack)
    {
        this.getSlot(slotID).putStack(stack);
    }

    public void func_190896_a(List<ItemStack> p_190896_1_)
    {
        for (int i = 0; i < p_190896_1_.size(); ++i)
        {
            this.getSlot(i).putStack(p_190896_1_.get(i));
        }
    }

    public void updateProgressBar(int id, int data)
    {
    }

    /**
     * Gets a unique transaction ID. Parameter is unused.
     */
    public short getNextTransactionID(InventoryPlayer invPlayer)
    {
        ++this.transactionID;
        return this.transactionID;
    }

    /**
     * gets whether or not the player can craft in this inventory or not
     */
    public boolean getCanCraft(EntityPlayer player)
    {
        return !this.playerList.contains(player);
    }

    /**
     * sets whether the player can craft in this inventory or not
     */
    public void setCanCraft(EntityPlayer player, boolean canCraft)
    {
        if (canCraft)
        {
            this.playerList.remove(player);
        }
        else
        {
            this.playerList.add(player);
        }
    }

    /**
     * Determines whether supplied player can use this container
     */
    public abstract boolean canInteractWith(EntityPlayer playerIn);

    /**
     * Merges provided ItemStack with the first avaliable one in the container/player inventor between minIndex
     * (included) and maxIndex (excluded). Args : stack, minIndex, maxIndex, negativDirection. /!\ the Container
     * implementation do not check if the item is valid for the slot
     */
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection)
    {
        boolean flag = false;
        int i = startIndex;

        if (reverseDirection)
        {
            i = endIndex - 1;
        }

        if (stack.isStackable())
        {
            while (!stack.isEmptyStack())
            {
                if (reverseDirection)
                {
                    if (i < startIndex)
                    {
                        break;
                    }
                }
                else if (i >= endIndex)
                {
                    break;
                }

                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();

                if (!itemstack.isEmptyStack() && itemstack.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, itemstack))
                {
                    int j = itemstack.func_190916_E() + stack.func_190916_E();

                    if (j <= stack.getMaxStackSize())
                    {
                        stack.func_190920_e(0);
                        itemstack.func_190920_e(j);
                        slot.onSlotChanged();
                        flag = true;
                    }
                    else if (itemstack.func_190916_E() < stack.getMaxStackSize())
                    {
                        stack.func_190918_g(stack.getMaxStackSize() - itemstack.func_190916_E());
                        itemstack.func_190920_e(stack.getMaxStackSize());
                        slot.onSlotChanged();
                        flag = true;
                    }
                }

                if (reverseDirection)
                {
                    --i;
                }
                else
                {
                    ++i;
                }
            }
        }

        if (!stack.isEmptyStack())
        {
            if (reverseDirection)
            {
                i = endIndex - 1;
            }
            else
            {
                i = startIndex;
            }

            while (true)
            {
                if (reverseDirection)
                {
                    if (i < startIndex)
                    {
                        break;
                    }
                }
                else if (i >= endIndex)
                {
                    break;
                }

                Slot slot1 = this.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();

                if (itemstack1.isEmptyStack() && slot1.isItemValid(stack))
                {
                    if (stack.func_190916_E() > slot1.getSlotStackLimit())
                    {
                        slot1.putStack(stack.splitStack(slot1.getSlotStackLimit()));
                    }
                    else
                    {
                        slot1.putStack(stack.splitStack(stack.func_190916_E()));
                    }

                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection)
                {
                    --i;
                }
                else
                {
                    ++i;
                }
            }
        }

        return flag;
    }

    /**
     * Extracts the drag mode. Args : eventButton. Return (0 : evenly split, 1 : one item by slot, 2 : not used ?)
     */
    public static int extractDragMode(int eventButton)
    {
        return eventButton >> 2 & 3;
    }

    /**
     * Args : clickedButton, Returns (0 : start drag, 1 : add slot, 2 : end drag)
     */
    public static int getDragEvent(int clickedButton)
    {
        return clickedButton & 3;
    }

    public static int getQuickcraftMask(int p_94534_0_, int p_94534_1_)
    {
        return p_94534_0_ & 3 | (p_94534_1_ & 3) << 2;
    }

    public static boolean isValidDragMode(int dragModeIn, EntityPlayer player)
    {
        if (dragModeIn == 0)
        {
            return true;
        }
        else if (dragModeIn == 1)
        {
            return true;
        }
        else
        {
            return dragModeIn == 2 && player.capabilities.isCreativeMode;
        }
    }

    /**
     * Reset the drag fields
     */
    protected void resetDrag()
    {
        this.dragEvent = 0;
        this.dragSlots.clear();
    }

    /**
     * Checks if it's possible to add the given itemstack to the given slot.
     */
    public static boolean canAddItemToSlot(@Nullable Slot slotIn, ItemStack stack, boolean stackSizeMatters)
    {
        boolean flag = slotIn == null || !slotIn.getHasStack();

        if (!flag && stack.isItemEqual(slotIn.getStack()) && ItemStack.areItemStackTagsEqual(slotIn.getStack(), stack))
        {
            return slotIn.getStack().func_190916_E() + (stackSizeMatters ? 0 : stack.func_190916_E()) <= stack.getMaxStackSize();
        }
        else
        {
            return flag;
        }
    }

    /**
     * Compute the new stack size, Returns the stack with the new size. Args : dragSlots, dragMode, dragStack,
     * slotStackSize
     */
    public static void computeStackSize(Set<Slot> dragSlotsIn, int dragModeIn, ItemStack stack, int slotStackSize)
    {
        switch (dragModeIn)
        {
            case 0:
                stack.func_190920_e(MathHelper.floor((float)stack.func_190916_E() / (float)dragSlotsIn.size()));
                break;

            case 1:
                stack.func_190920_e(1);
                break;

            case 2:
                stack.func_190920_e(stack.getItem().getItemStackLimit());
        }

        stack.func_190917_f(slotStackSize);
    }

    /**
     * Returns true if the player can "drag-spilt" items into this slot,. returns true by default. Called to check if
     * the slot can be added to a list of Slots to split the held ItemStack across.
     */
    public boolean canDragIntoSlot(Slot slotIn)
    {
        return true;
    }

    /**
     * Like the version that takes an inventory. If the given TileEntity is not an Inventory, 0 is returned instead.
     */
    public static int calcRedstone(@Nullable TileEntity te)
    {
        return te instanceof IInventory ? calcRedstoneFromInventory((IInventory)te) : 0;
    }

    public static int calcRedstoneFromInventory(@Nullable IInventory inv)
    {
        if (inv == null)
        {
            return 0;
        }
        else
        {
            int i = 0;
            float f = 0.0F;

            for (int j = 0; j < inv.getSizeInventory(); ++j)
            {
                ItemStack itemstack = inv.getStackInSlot(j);

                if (!itemstack.isEmptyStack())
                {
                    f += (float)itemstack.func_190916_E() / (float)Math.min(inv.getInventoryStackLimit(), itemstack.getMaxStackSize());
                    ++i;
                }
            }

            f = f / (float)inv.getSizeInventory();
            return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
    }

    protected void func_192389_a(World p_192389_1_, EntityPlayer p_192389_2_, InventoryCrafting p_192389_3_, InventoryCraftResult p_192389_4_)
    {
        if (!p_192389_1_.isRemote)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)p_192389_2_;
            ItemStack itemstack = ItemStack.field_190927_a;
            IRecipe irecipe = CraftingManager.func_192413_b(p_192389_3_, p_192389_1_);

            if (irecipe != null && (irecipe.func_192399_d() || !p_192389_1_.getGameRules().getBoolean("doLimitedCrafting") || entityplayermp.func_192037_E().func_193830_f(irecipe)))
            {
                p_192389_4_.func_193056_a(irecipe);
                itemstack = irecipe.getCraftingResult(p_192389_3_);
            }

            p_192389_4_.setInventorySlotContents(0, itemstack);
            entityplayermp.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, itemstack));
        }
    }
}
