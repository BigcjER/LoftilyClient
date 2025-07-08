package loftily.module.impl.player;

import loftily.event.impl.client.DisplayScreenEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.ItemUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "ChestStealer", key = Keyboard.KEY_U, category = ModuleCategory.PLAYER)
public class ChestStealer extends Module {
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
                    !ItemUtils.isItemUsefulInContainer(containerChest, slot.getStack())) {
                continue;
            }

            if(modeValue.is("Mouse")){
                GuiScreen gui = mc.currentScreen;
                if(gui instanceof GuiChest) {
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
                        break;
                    }
                }
            }

            if(!clickTimer.hasTimeElapsed(RandomUtils.randomInt((int) clickDelay.getFirst(), (int) clickDelay.getSecond()))){
                break;
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