package loftily.module.impl.player;

import loftily.event.impl.client.DisplayScreenEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.ItemUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.RangeSelectionNumberValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ChestStealer", key = Keyboard.KEY_U, category = ModuleCategory.PLAYER)
public class ChestStealer extends Module {
    private final RangeSelectionNumberValue openDelay = new RangeSelectionNumberValue("OpenDelay", 50, 100, 0, 1000);
    private final RangeSelectionNumberValue clickDelay = new RangeSelectionNumberValue("ClickDelay", 50, 100, 0, 1000);
    private final RangeSelectionNumberValue autoCloseDelay = new RangeSelectionNumberValue("AutoCloseDelay", 50, 100, 0, 1000);
    
    private final DelayTimer openTimer = new DelayTimer();
    private final DelayTimer clickTimer = new DelayTimer();
    private final DelayTimer autoCloseTimer = new DelayTimer();
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        //Check screen
        if (!(mc.currentScreen instanceof GuiChest) || mc.player.openContainer == null) return;
        Container containerInventory = mc.player.openContainer;
        
        if (!(containerInventory instanceof ContainerChest)) return;
        ContainerChest containerChest = (ContainerChest) mc.player.openContainer;
        
        
        //Close screen if no useful item
        boolean hasUsefulItems = false;
        for (Slot slot : containerChest.inventorySlots) {
            //Ensure item is in chest
            if (slot.inventory != containerChest.getLowerChestInventory()) continue;
            
            if (ItemUtils.isItemUsefulInContainer(containerChest, slot.getStack())) {
                hasUsefulItems = true;
                break;
            }
        }
        
        
        if (!hasUsefulItems) {
            if(autoCloseTimer.hasTimeElapsed(RandomUtils.randomInt((int) autoCloseDelay.getFirst(), (int) autoCloseDelay.getSecond())))
                mc.player.closeScreen();
            return;
        }
        
        
        //Check open timer
        if (!openTimer.hasTimeElapsed(RandomUtils.randomInt((int) openDelay.getFirst(), (int) openDelay.getSecond())))
            return;
        
        
        //Do steal
        for (Slot slot : containerChest.inventorySlots) {
            //Ensure item is in chest
            if (slot.inventory != containerChest.getLowerChestInventory()) continue;
            
            if (slot.getStack().isEmptyStack() ||
                    !clickTimer.hasTimeElapsed(RandomUtils.randomInt((int) clickDelay.getFirst(), (int) clickDelay.getSecond())) ||
                    !ItemUtils.isItemUsefulInContainer(containerChest, slot.getStack())) {
                continue;
            }
            
            mc.playerController.windowClick(containerChest.windowId, slot.slotNumber, 0, ClickType.QUICK_MOVE, mc.player);
            clickTimer.reset();
            autoCloseTimer.reset();
        }
    }
    
    @EventHandler
    public void onDisplayScreen(DisplayScreenEvent event) {
        if (event.getNewScreen() instanceof GuiChest) {
            openTimer.reset();
        }
    }
}