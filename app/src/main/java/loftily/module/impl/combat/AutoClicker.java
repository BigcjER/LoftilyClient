package loftily.module.impl.combat;

import loftily.event.impl.render.Render3DEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.RandomUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.RangeSelectionNumberValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSword;

@ModuleInfo(name = "AutoClicker", category = ModuleCategory.COMBAT)
public class AutoClicker extends Module {
    private final BooleanValue leftClick = new BooleanValue("LeftClick", false);
    private final BooleanValue rightClick = new BooleanValue("RightClick", false);
    private final RangeSelectionNumberValue leftSpeed = new RangeSelectionNumberValue("LeftCPS", 5, 15, 0, 60, 1)
            .setVisible(leftClick::getValue);
    private final RangeSelectionNumberValue rightSpeed = new RangeSelectionNumberValue("RightCPS", 5, 15, 0, 60, 1)
            .setVisible(rightClick::getValue);
    private final BooleanValue onlySword = new BooleanValue("LeftClick-OnlySword", false).setVisible(leftClick::getValue);
    private final BooleanValue breaking = new BooleanValue("LeftClick-WhileBreaking", false).setVisible(leftClick::getValue);
    
    private final BooleanValue onlyBlock = new BooleanValue("RightClick-OnlyBlock", false).setVisible(rightClick::getValue);
    
    private final DelayTimer leftTimer = new DelayTimer();
    private final DelayTimer rightTimer = new DelayTimer();
    
    private int leftDelay = 0;
    private int rightDelay = 0;
    
    private void calLeftDelay() {
        leftDelay = Math.round((float) 1000 / Math.round(RandomUtils.randomDouble(leftSpeed.getFirst(), leftSpeed.getSecond())));
    }
    
    
    private void calRightDelay() {
        rightDelay = Math.round((float) 1000 / Math.round(RandomUtils.randomDouble(rightSpeed.getFirst(), rightSpeed.getSecond())));
    }
    
    @Override
    public void onEnable() {
        calLeftDelay();
        calRightDelay();
    }
    
    @EventHandler
    public void onClick(Render3DEvent event) {
        if (leftClick.getValue()) {
            if (!onlySword.getValue() || mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
                if (breaking.getValue() || mc.playerController.curBlockDamageMP == 0F) {
                    if (mc.gameSettings.keyBindAttack.isKeyDown() && leftTimer.hasTimeElapsed(leftDelay)) {
                        leftTimer.reset();
                        KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                        calLeftDelay();
                    }
                }
            }
        }
        
        if (rightClick.getValue()) {
            if (!onlyBlock.getValue() || mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
                if (mc.gameSettings.keyBindUseItem.isKeyDown() && rightTimer.hasTimeElapsed(rightDelay)) {
                    rightTimer.reset();
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                    calRightDelay();
                }
            }
        }
    }
}
