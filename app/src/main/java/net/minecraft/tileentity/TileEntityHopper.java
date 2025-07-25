package net.minecraft.tileentity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHopper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class TileEntityHopper extends TileEntityLockableLoot implements IHopper, ITickable
{
    private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>func_191197_a(5, ItemStack.field_190927_a);
    private int transferCooldown = -1;
    private long field_190578_g;

    public static void registerFixesHopper(DataFixer fixer)
    {
        fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists(TileEntityHopper.class, new String[] {"Items"}));
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.inventory = NonNullList.<ItemStack>func_191197_a(this.getSizeInventory(), ItemStack.field_190927_a);

        if (!this.checkLootAndRead(compound))
        {
            ItemStackHelper.func_191283_b(compound, this.inventory);
        }

        if (compound.hasKey("CustomName", 8))
        {
            this.field_190577_o = compound.getString("CustomName");
        }

        this.transferCooldown = compound.getInteger("TransferCooldown");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (!this.checkLootAndWrite(compound))
        {
            ItemStackHelper.func_191282_a(compound, this.inventory);
        }

        compound.setInteger("TransferCooldown", this.transferCooldown);

        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.field_190577_o);
        }

        return compound;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory()
    {
        return this.inventory.size();
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count)
    {
        this.fillWithLoot((EntityPlayer)null);
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.func_190576_q(), index, count);
        return itemstack;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        this.fillWithLoot((EntityPlayer)null);
        this.func_190576_q().set(index, stack);

        if (stack.func_190916_E() > this.getInventoryStackLimit())
        {
            stack.func_190920_e(this.getInventoryStackLimit());
        }
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName()
    {
        return this.hasCustomName() ? this.field_190577_o : "container.hopper";
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    public int getInventoryStackLimit()
    {
        return 64;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        if (this.world != null && !this.world.isRemote)
        {
            --this.transferCooldown;
            this.field_190578_g = this.world.getTotalWorldTime();

            if (!this.isOnTransferCooldown())
            {
                this.setTransferCooldown(0);
                this.updateHopper();
            }
        }
    }

    private boolean updateHopper()
    {
        if (this.world != null && !this.world.isRemote)
        {
            if (!this.isOnTransferCooldown() && BlockHopper.isEnabled(this.getBlockMetadata()))
            {
                boolean flag = false;

                if (!this.isEmpty())
                {
                    flag = this.transferItemsOut();
                }

                if (!this.isFull())
                {
                    flag = captureDroppedItems(this) || flag;
                }

                if (flag)
                {
                    this.setTransferCooldown(8);
                    this.markDirty();
                    return true;
                }
            }

            return false;
        }
        else
        {
            return false;
        }
    }

    private boolean isEmpty()
    {
        for (ItemStack itemstack : this.inventory)
        {
            if (!itemstack.isEmptyStack())
            {
                return false;
            }
        }

        return true;
    }

    public boolean func_191420_l()
    {
        return this.isEmpty();
    }

    private boolean isFull()
    {
        for (ItemStack itemstack : this.inventory)
        {
            if (itemstack.isEmptyStack() || itemstack.func_190916_E() != itemstack.getMaxStackSize())
            {
                return false;
            }
        }

        return true;
    }

    private boolean transferItemsOut()
    {
        IInventory iinventory = this.getInventoryForHopperTransfer();

        if (iinventory == null)
        {
            return false;
        }
        else
        {
            EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata()).getOpposite();

            if (this.isInventoryFull(iinventory, enumfacing))
            {
                return false;
            }
            else
            {
                for (int i = 0; i < this.getSizeInventory(); ++i)
                {
                    if (!this.getStackInSlot(i).isEmptyStack())
                    {
                        ItemStack itemstack = this.getStackInSlot(i).copy();
                        ItemStack itemstack1 = putStackInInventoryAllSlots(this, iinventory, this.decrStackSize(i, 1), enumfacing);

                        if (itemstack1.isEmptyStack())
                        {
                            iinventory.markDirty();
                            return true;
                        }

                        this.setInventorySlotContents(i, itemstack);
                    }
                }

                return false;
            }
        }
    }

    /**
     * Returns false if the inventory has any room to place items in
     */
    private boolean isInventoryFull(IInventory inventoryIn, EnumFacing side)
    {
        if (inventoryIn instanceof ISidedInventory)
        {
            ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
            int[] aint = isidedinventory.getSlotsForFace(side);

            for (int k : aint)
            {
                ItemStack itemstack1 = isidedinventory.getStackInSlot(k);

                if (itemstack1.isEmptyStack() || itemstack1.func_190916_E() != itemstack1.getMaxStackSize())
                {
                    return false;
                }
            }
        }
        else
        {
            int i = inventoryIn.getSizeInventory();

            for (int j = 0; j < i; ++j)
            {
                ItemStack itemstack = inventoryIn.getStackInSlot(j);

                if (itemstack.isEmptyStack() || itemstack.func_190916_E() != itemstack.getMaxStackSize())
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns false if the specified IInventory contains any items
     */
    private static boolean isInventoryEmpty(IInventory inventoryIn, EnumFacing side)
    {
        if (inventoryIn instanceof ISidedInventory)
        {
            ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
            int[] aint = isidedinventory.getSlotsForFace(side);

            for (int i : aint)
            {
                if (!isidedinventory.getStackInSlot(i).isEmptyStack())
                {
                    return false;
                }
            }
        }
        else
        {
            int j = inventoryIn.getSizeInventory();

            for (int k = 0; k < j; ++k)
            {
                if (!inventoryIn.getStackInSlot(k).isEmptyStack())
                {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean captureDroppedItems(IHopper hopper)
    {
        IInventory iinventory = getHopperInventory(hopper);

        if (iinventory != null)
        {
            EnumFacing enumfacing = EnumFacing.DOWN;

            if (isInventoryEmpty(iinventory, enumfacing))
            {
                return false;
            }

            if (iinventory instanceof ISidedInventory)
            {
                ISidedInventory isidedinventory = (ISidedInventory)iinventory;
                int[] aint = isidedinventory.getSlotsForFace(enumfacing);

                for (int i : aint)
                {
                    if (pullItemFromSlot(hopper, iinventory, i, enumfacing))
                    {
                        return true;
                    }
                }
            }
            else
            {
                int j = iinventory.getSizeInventory();

                for (int k = 0; k < j; ++k)
                {
                    if (pullItemFromSlot(hopper, iinventory, k, enumfacing))
                    {
                        return true;
                    }
                }
            }
        }
        else
        {
            for (EntityItem entityitem : getCaptureItems(hopper.getWorld(), hopper.getXPos(), hopper.getYPos(), hopper.getZPos()))
            {
                if (putDropInInventoryAllSlots((IInventory)null, hopper, entityitem))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Pulls from the specified slot in the inventory and places in any available slot in the hopper. Returns true if
     * the entire stack was moved
     */
    private static boolean pullItemFromSlot(IHopper hopper, IInventory inventoryIn, int index, EnumFacing direction)
    {
        ItemStack itemstack = inventoryIn.getStackInSlot(index);

        if (!itemstack.isEmptyStack() && canExtractItemFromSlot(inventoryIn, itemstack, index, direction))
        {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = putStackInInventoryAllSlots(inventoryIn, hopper, inventoryIn.decrStackSize(index, 1), (EnumFacing)null);

            if (itemstack2.isEmptyStack())
            {
                inventoryIn.markDirty();
                return true;
            }

            inventoryIn.setInventorySlotContents(index, itemstack1);
        }

        return false;
    }

    /**
     * Attempts to place the passed EntityItem's stack into the inventory using as many slots as possible. Returns false
     * if the stackSize of the drop was not depleted.
     */
    public static boolean putDropInInventoryAllSlots(IInventory p_145898_0_, IInventory itemIn, EntityItem p_145898_2_)
    {
        boolean flag = false;

        if (p_145898_2_ == null)
        {
            return false;
        }
        else
        {
            ItemStack itemstack = p_145898_2_.getEntityItem().copy();
            ItemStack itemstack1 = putStackInInventoryAllSlots(p_145898_0_, itemIn, itemstack, (EnumFacing)null);

            if (itemstack1.isEmptyStack())
            {
                flag = true;
                p_145898_2_.setDead();
            }
            else
            {
                p_145898_2_.setEntityItemStack(itemstack1);
            }

            return flag;
        }
    }

    /**
     * Attempts to place the passed stack in the inventory, using as many slots as required. Returns leftover items
     */
    public static ItemStack putStackInInventoryAllSlots(IInventory inventoryIn, IInventory stack, ItemStack side, @Nullable EnumFacing p_174918_3_)
    {
        if (stack instanceof ISidedInventory && p_174918_3_ != null)
        {
            ISidedInventory isidedinventory = (ISidedInventory)stack;
            int[] aint = isidedinventory.getSlotsForFace(p_174918_3_);

            for (int k = 0; k < aint.length && !side.isEmptyStack(); ++k)
            {
                side = insertStack(inventoryIn, stack, side, aint[k], p_174918_3_);
            }
        }
        else
        {
            int i = stack.getSizeInventory();

            for (int j = 0; j < i && !side.isEmptyStack(); ++j)
            {
                side = insertStack(inventoryIn, stack, side, j, p_174918_3_);
            }
        }

        return side;
    }

    /**
     * Can this hopper insert the specified item from the specified slot on the specified side?
     */
    private static boolean canInsertItemInSlot(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side)
    {
        if (!inventoryIn.isItemValidForSlot(index, stack))
        {
            return false;
        }
        else
        {
            return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canInsertItem(index, stack, side);
        }
    }

    /**
     * Can this hopper extract the specified item from the specified slot on the specified side?
     */
    private static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side)
    {
        return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canExtractItem(index, stack, side);
    }

    /**
     * Insert the specified stack to the specified inventory and return any leftover items
     */
    private static ItemStack insertStack(IInventory inventoryIn, IInventory stack, ItemStack index, int side, EnumFacing p_174916_4_)
    {
        ItemStack itemstack = stack.getStackInSlot(side);

        if (canInsertItemInSlot(stack, index, side, p_174916_4_))
        {
            boolean flag = false;
            boolean flag1 = stack.func_191420_l();

            if (itemstack.isEmptyStack())
            {
                stack.setInventorySlotContents(side, index);
                index = ItemStack.field_190927_a;
                flag = true;
            }
            else if (canCombine(itemstack, index))
            {
                int i = index.getMaxStackSize() - itemstack.func_190916_E();
                int j = Math.min(index.func_190916_E(), i);
                index.func_190918_g(j);
                itemstack.func_190917_f(j);
                flag = j > 0;
            }

            if (flag)
            {
                if (flag1 && stack instanceof TileEntityHopper)
                {
                    TileEntityHopper tileentityhopper1 = (TileEntityHopper)stack;

                    if (!tileentityhopper1.mayTransfer())
                    {
                        int k = 0;

                        if (inventoryIn != null && inventoryIn instanceof TileEntityHopper)
                        {
                            TileEntityHopper tileentityhopper = (TileEntityHopper)inventoryIn;

                            if (tileentityhopper1.field_190578_g >= tileentityhopper.field_190578_g)
                            {
                                k = 1;
                            }
                        }

                        tileentityhopper1.setTransferCooldown(8 - k);
                    }
                }

                stack.markDirty();
            }
        }

        return index;
    }

    /**
     * Returns the IInventory that this hopper is pointing into
     */
    private IInventory getInventoryForHopperTransfer()
    {
        EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata());
        return getInventoryAtPosition(this.getWorld(), this.getXPos() + (double)enumfacing.getFrontOffsetX(), this.getYPos() + (double)enumfacing.getFrontOffsetY(), this.getZPos() + (double)enumfacing.getFrontOffsetZ());
    }

    /**
     * Returns the IInventory for the specified hopper
     */
    public static IInventory getHopperInventory(IHopper hopper)
    {
        return getInventoryAtPosition(hopper.getWorld(), hopper.getXPos(), hopper.getYPos() + 1.0D, hopper.getZPos());
    }

    public static List<EntityItem> getCaptureItems(World worldIn, double p_184292_1_, double p_184292_3_, double p_184292_5_)
    {
        return worldIn.<EntityItem>getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(p_184292_1_ - 0.5D, p_184292_3_, p_184292_5_ - 0.5D, p_184292_1_ + 0.5D, p_184292_3_ + 1.5D, p_184292_5_ + 0.5D), EntitySelectors.IS_ALIVE);
    }

    /**
     * Returns the IInventory (if applicable) of the TileEntity at the specified position
     */
    public static IInventory getInventoryAtPosition(World worldIn, double x, double y, double z)
    {
        IInventory iinventory = null;
        int i = MathHelper.floor(x);
        int j = MathHelper.floor(y);
        int k = MathHelper.floor(z);
        BlockPos blockpos = new BlockPos(i, j, k);
        Block block = worldIn.getBlockState(blockpos).getBlock();

        if (block.hasTileEntity())
        {
            TileEntity tileentity = worldIn.getTileEntity(blockpos);

            if (tileentity instanceof IInventory)
            {
                iinventory = (IInventory)tileentity;

                if (iinventory instanceof TileEntityChest && block instanceof BlockChest)
                {
                    iinventory = ((BlockChest)block).getContainer(worldIn, blockpos, true);
                }
            }
        }

        if (iinventory == null)
        {
            List<Entity> list = worldIn.getEntitiesInAABBexcluding((Entity)null, new AxisAlignedBB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntitySelectors.HAS_INVENTORY);

            if (!list.isEmpty())
            {
                iinventory = (IInventory)list.get(worldIn.rand.nextInt(list.size()));
            }
        }

        return iinventory;
    }

    private static boolean canCombine(ItemStack stack1, ItemStack stack2)
    {
        if (stack1.getItem() != stack2.getItem())
        {
            return false;
        }
        else if (stack1.getMetadata() != stack2.getMetadata())
        {
            return false;
        }
        else if (stack1.func_190916_E() > stack1.getMaxStackSize())
        {
            return false;
        }
        else
        {
            return ItemStack.areItemStackTagsEqual(stack1, stack2);
        }
    }

    /**
     * Gets the world X position for this hopper entity.
     */
    public double getXPos()
    {
        return (double)this.pos.getX() + 0.5D;
    }

    /**
     * Gets the world Y position for this hopper entity.
     */
    public double getYPos()
    {
        return (double)this.pos.getY() + 0.5D;
    }

    /**
     * Gets the world Z position for this hopper entity.
     */
    public double getZPos()
    {
        return (double)this.pos.getZ() + 0.5D;
    }

    private void setTransferCooldown(int ticks)
    {
        this.transferCooldown = ticks;
    }

    private boolean isOnTransferCooldown()
    {
        return this.transferCooldown > 0;
    }

    private boolean mayTransfer()
    {
        return this.transferCooldown > 8;
    }

    public String getGuiID()
    {
        return "minecraft:hopper";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        this.fillWithLoot(playerIn);
        return new ContainerHopper(playerInventory, this, playerIn);
    }

    protected NonNullList<ItemStack> func_190576_q()
    {
        return this.inventory;
    }
}
