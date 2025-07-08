package loftily.module.impl.player;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import loftily.event.impl.client.DisplayScreenEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.ItemUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.player.InventoryUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import lombok.Getter;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Arrays;
import java.util.function.Predicate;

@ModuleInfo(name = "InvManager", category = ModuleCategory.PLAYER, key = Keyboard.KEY_I)
public class InvManager extends Module {
    //Delay value
    private final ModeValue modeValue = new ModeValue("Mode","Packet",this,
            new StringMode("Packet"),
            new StringMode("Mouse")
    );
    private final RangeSelectionNumberValue mouseXZMoveSpeed = new RangeSelectionNumberValue("MouseSpeedXZ",20,50,0,360)
            .setVisible(()->modeValue.is("Mouse"));
    private final RangeSelectionNumberValue mouseYMoveSpeed = new RangeSelectionNumberValue("MouseSpeedY",20,50,0,360)
            .setVisible(()->modeValue.is("Mouse"));;

    private final RangeSelectionNumberValue openDelay = new RangeSelectionNumberValue("OpenDelay", 50, 100, 0, 1000);
    private final RangeSelectionNumberValue clickDelay = new RangeSelectionNumberValue("ClickDelay", 50, 100, 0, 1000);
    private final BooleanValue autoClose = new BooleanValue("AutoClose", true);
    private final RangeSelectionNumberValue autoCloseDelay = new RangeSelectionNumberValue("AutoCloseDelay", 50, 100, 0, 1000)
            .setVisible(autoClose::getValue);
    private final BooleanValue requiresInvOpened = new BooleanValue("RequiresInvOpened", true);
    private final BooleanValue throwUselessItems = new BooleanValue("ThrowUselessItems", true);

    //Slots
    private final ModeValue slot1 = createSlotMode("Slot1", "Sword");
    private final ModeValue slot2 = createSlotMode("Slot2", "Block");
    private final ModeValue slot3 = createSlotMode("Slot3", "Pickaxe");
    private final ModeValue slot4 = createSlotMode("Slot4", "Axe");
    private final ModeValue slot5 = createSlotMode("Slot5", "GoldenApple");
    private final ModeValue slot6 = createSlotMode("Slot6", "Shovel");
    private final ModeValue slot7 = createSlotMode("Slot7", "EnderPearl");
    private final ModeValue slot8 = createSlotMode("Slot8", "Food");
    private final ModeValue slot9 = createSlotMode("Slot9", "WaterBucket");
    private final ModeValue offHand = createSlotMode("OffHand", "Empty")
            .setVisible(() -> ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8));
    
    //Timer
    private final DelayTimer clickTimer = new DelayTimer();
    private final DelayTimer openTimer = new DelayTimer();
    private final DelayTimer autoCloseTimer = new DelayTimer();
    //Delay
    private int currentOpenDelay, currentClickDelay, currentAutoCloseDelay;
    //Other
    private boolean canBeClose;
    
    @Override
    public void onToggle() {
        super.onToggle();
        currentClickDelay = getRandomDelay(clickDelay.getFirst(), clickDelay.getSecond());
    }
    
    @EventHandler
    public void onDisplayScreen(DisplayScreenEvent event) {
        if (event.getNewScreen() instanceof GuiInventory) {
            openTimer.reset();
            currentOpenDelay = RandomUtils.randomInt((int) openDelay.getFirst(), (int) openDelay.getSecond());
        } else {
            clickTimer.reset();
        }
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!(mc.currentScreen instanceof GuiInventory) && requiresInvOpened.getValue()) return;
        if (!openTimer.hasTimeElapsed(currentOpenDelay)) return;
        canBeClose = true;
        
        //Throw useless items
        if (throwUselessItems.getValue()) {
            for (Slot slot : mc.player.inventoryContainer.inventorySlots) {
                ItemStack itemStack = slot.getStack();
                
                if (itemStack.isEmptyStack()) {
                    continue;
                }
                
                if (!ItemUtils.isItemUsefulInContainer(mc.player.inventoryContainer, itemStack)) {
                    if(modeValue.is("Mouse")){
                        GuiScreen gui = mc.currentScreen;
                        if (gui instanceof GuiInventory) {
                            int guiLeft = (gui.width - 176) / 2;
                            int guiTop = (gui.height - 166) / 2;
                            ScaledResolution scaledResolution = new ScaledResolution(mc);
                            int scaleFactor = scaledResolution.getScaleFactor();
                            int x = (slot.xDisplayPosition + 8) * scaleFactor + guiLeft * scaleFactor;
                            int y = ((slot.yDisplayPosition + 8) * -scaleFactor + guiTop * scaleFactor) + 166 * scaleFactor;
                            int n1 = RandomUtils.randomInt((int) mouseXZMoveSpeed.getFirst(), (int) mouseXZMoveSpeed.getSecond());
                            int n2 = RandomUtils.randomInt((int) mouseYMoveSpeed.getFirst(), (int) mouseYMoveSpeed.getSecond());
                            int diffX = Math.max(-n1, Math.min(x - Mouse.getX(), n1));
                            int diffY = Math.max(-n2, Math.min(y - Mouse.getY(), n2));
                            Mouse.setCursorPosition(diffX + Mouse.getX(), diffY + Mouse.getY());
                            if (Mouse.getX() != x || Mouse.getY() != y) {
                                return;
                            }
                        }
                    }
                    this.throwItem(slot.slotNumber,slot);
                    if(modeValue.is("Mouse") && !canBeClose){
                        return;
                    }
                }
            }
        }
        
        //Wear armor
        for (Slot slot : mc.player.inventoryContainer.inventorySlots) {
            ItemStack itemStack = slot.getStack();
            if (itemStack.isEmptyStack()) {
                continue;
            }
            
            Item item = itemStack.getItem();
            if (item instanceof ItemArmor) {
                int slotNumber = slot.slotNumber;
                int targetSlot = -1;
                
                ItemArmor itemArmor = (ItemArmor) item;
                EntityEquipmentSlot armorType = itemArmor.armorType;
                
                //Compare with current armor
                ItemStack currentArmor = mc.player.inventory.armorInventory.get(armorType.getIndex());
                if (!currentArmor.isEmptyStack() && ItemUtils.getArmorWeight(currentArmor) >= ItemUtils.getArmorWeight(itemStack)) {
                    continue;
                }
                
                switch (armorType) {
                    case HEAD:
                        targetSlot = 5;
                        break;
                    case CHEST:
                        targetSlot = 6;
                        break;
                    case LEGS:
                        targetSlot = 7;
                        break;
                    case FEET:
                        targetSlot = 8;
                        break;
                }
                
                if (targetSlot != -1) {
                    if(modeValue.is("Mouse")){
                        GuiScreen gui = mc.currentScreen;
                        if (gui instanceof GuiInventory) {
                            int guiLeft = (gui.width - 176) / 2;
                            int guiTop = (gui.height - 166) / 2;
                            ScaledResolution scaledResolution = new ScaledResolution(mc);
                            int scaleFactor = scaledResolution.getScaleFactor();
                            int x = (slot.xDisplayPosition + 8) * scaleFactor + guiLeft * scaleFactor;
                            int y = ((slot.yDisplayPosition + 8) * -scaleFactor + guiTop * scaleFactor) + 166 * scaleFactor;
                            int n1 = RandomUtils.randomInt((int) mouseXZMoveSpeed.getFirst(), (int) mouseXZMoveSpeed.getSecond());
                            int n2 = RandomUtils.randomInt((int) mouseYMoveSpeed.getFirst(), (int) mouseYMoveSpeed.getSecond());
                            int diffX = Math.max(-n1, Math.min(x - Mouse.getX(), n1));
                            int diffY = Math.max(-n2, Math.min(y - Mouse.getY(), n2));
                            Mouse.setCursorPosition(diffX + Mouse.getX(), diffY + Mouse.getY());
                            if (Mouse.getX() != x || Mouse.getY() != y) {
                                return;
                            }
                        }
                    }
                    moveItem(slotNumber, targetSlot,slot);
                    if(modeValue.is("Mouse") && !canBeClose){
                        return;
                    }
                }
            }
        }
        
        //Sorting items
        //Search from hotbar
        for (int hotBarSlot : InventoryUtils.HOTBAR_SLOT_IDS) {
            ModeValue modeValue = getModeValueForSlot(hotBarSlot);
            if (modeValue == null) continue;
            ItemCategory category = ItemCategory.get(modeValue.getValueByName());
            if (category == ItemCategory.EMPTY) continue;
            
            ItemStack hotbarStack = mc.player.inventoryContainer.getSlot(hotBarSlot).getStack();
            int fromSlot = -1;
            //TODO:Better sorting item
            for (int i = 9; i < 45; i++) {
                ItemStack inventoryStack = mc.player.inventoryContainer.getSlot(i).getStack();
                if (inventoryStack.isEmptyStack() || !category.is(inventoryStack)) continue;
                
                fromSlot = i;
            }
            
            if (fromSlot != -1 && hotbarStack.isEmptyStack()) {
                if(modeValue.is("Mouse")){
                    GuiScreen gui = mc.currentScreen;
                    Slot slot = mc.player.inventoryContainer.getSlot(fromSlot);
                    if (gui instanceof GuiInventory) {
                        int guiLeft = (gui.width - 176) / 2;
                        int guiTop = (gui.height - 166) / 2;
                        ScaledResolution scaledResolution = new ScaledResolution(mc);
                        int scaleFactor = scaledResolution.getScaleFactor();
                        int x = (slot.xDisplayPosition + 8) * scaleFactor + guiLeft * scaleFactor;
                        int y = ((slot.yDisplayPosition + 8) * -scaleFactor + guiTop * scaleFactor) + 166 * scaleFactor;
                        int n1 = RandomUtils.randomInt((int) mouseXZMoveSpeed.getFirst(), (int) mouseXZMoveSpeed.getSecond());
                        int n2 = RandomUtils.randomInt((int) mouseYMoveSpeed.getFirst(), (int) mouseYMoveSpeed.getSecond());
                        int diffX = Math.max(-n1, Math.min(x - Mouse.getX(), n1));
                        int diffY = Math.max(-n2, Math.min(y - Mouse.getY(), n2));
                        Mouse.setCursorPosition(diffX + Mouse.getX(), diffY + Mouse.getY());
                        if (Mouse.getX() != x || Mouse.getY() != y) {
                            return;
                        }
                    }
                }
                moveItem(fromSlot, hotBarSlot,mc.player.inventoryContainer.getSlot(fromSlot));
                if(modeValue.is("Mouse") && !canBeClose){
                    return;
                }
            }
        }
        
        //AutoClose
        if (canBeClose && autoClose.getValue() && autoCloseTimer.hasTimeElapsed(currentAutoCloseDelay)) {
            mc.player.closeScreen();
            autoCloseTimer.reset();
        }
    }
    
    
    private void moveItem(int from, int to,Slot slot) {
        canBeClose = false;
        if (clickTimer.hasTimeElapsed(currentClickDelay)) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, from, 0, ClickType.SWAP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, to, 0, ClickType.SWAP, mc.player);
            currentClickDelay = getRandomDelay(clickDelay.getFirst(), clickDelay.getSecond());
            currentAutoCloseDelay = getRandomDelay(autoCloseDelay.getFirst(), autoCloseDelay.getSecond());
            clickTimer.reset();
            autoCloseTimer.reset();
        }
    }
    
    private void throwItem(int slotNumber,Slot slot) {
        canBeClose = false;
        if (clickTimer.hasTimeElapsed(currentClickDelay)) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotNumber, 1, ClickType.THROW, mc.player);
            currentClickDelay = getRandomDelay(clickDelay.getFirst(), clickDelay.getSecond());
            currentAutoCloseDelay = getRandomDelay(autoCloseDelay.getFirst(), autoCloseDelay.getSecond());
            clickTimer.reset();
            autoCloseTimer.reset();
        }
    }
    
    private ModeValue getModeValueForSlot(int i) {
        switch (i) {
            case 36:
                return slot1;
            case 37:
                return slot2;
            case 38:
                return slot3;
            case 39:
                return slot4;
            case 40:
                return slot5;
            case 41:
                return slot6;
            case 42:
                return slot7;
            case 43:
                return slot8;
            case 44:
                return slot9;
            case 45:
                return offHand;
            default:
                return null;
        }
    }
    
    private ModeValue createSlotMode(String name, String value) {
        return new ModeValue(name, value, this, ItemCategory.getAsStringModes());
    }
    
    private int getRandomDelay(double first, double second) {
        return RandomUtils.randomInt((int) first, (int) second);
    }
    
    @Getter
    private enum ItemCategory {
        SWORD("Sword", item -> item instanceof ItemSword),
        BLOCK("Block", item -> item instanceof ItemBlock),
        PICKAXE("Pickaxe", item -> item instanceof ItemPickaxe),
        AXE("Axe", item -> item instanceof ItemAxe),
        GOLDEN_APPLE("GoldenApple", item -> item == Items.GOLDEN_APPLE),
        SHOVEL("Shovel", item -> item instanceof ItemSpade),
        ENDER_PEARL("EnderPearl", item -> item == Items.ENDER_PEARL),
        FOOD("Food", item -> item instanceof ItemFood),
        SHIELD("Shield", item -> item instanceof ItemShield),
        WATER_BUCKET("WaterBucket", item -> item == Items.WATER_BUCKET),
        EMPTY("Empty", item -> false);
        
        private final String displayName;
        private final Predicate<Item> matching;
        
        ItemCategory(String displayName, Predicate<Item> matching) {
            this.displayName = displayName;
            this.matching = matching;
        }
        
        public static StringMode[] getAsStringModes() {
            return Arrays.stream(values())
                    .map(category -> new StringMode(category.getDisplayName()))
                    .toArray(StringMode[]::new);
        }
        
        public static ItemCategory get(String displayName) {
            for (ItemCategory category : values()) {
                if (category.getDisplayName().equalsIgnoreCase(displayName)) {
                    return category;
                }
            }
            return EMPTY;
        }
        
        public boolean is(ItemStack itemStack) {
            if (itemStack.isEmptyStack()) return this == EMPTY;
            
            if (this == EMPTY) {
                return itemStack.isEmptyStack();
            }
            return matching.test(itemStack.getItem());
        }
    }
}