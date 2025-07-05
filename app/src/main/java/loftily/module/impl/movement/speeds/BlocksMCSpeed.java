package loftily.module.impl.movement.speeds;

import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.movement.Speed;
import loftily.utils.player.MoveUtils;
import loftily.utils.player.PlayerUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Objects;

public class BlocksMCSpeed extends Mode<Speed> {
    public BlocksMCSpeed() {
        super("BlocksMC");
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.player.isInWater() || mc.player.isInWeb || mc.player.isCollidedHorizontally || (PlayerUtils.isUsingItem() && !PlayerUtils.isBlocking()))
            return;
        
        if (!MoveUtils.isMoving()) {
            MoveUtils.stop(false);
            return;
        } else if (mc.player.onGround) {
            mc.player.tryJump();
            return;
        }
        
        if (mc.player.offGroundTicks >= 6) {
            MoveUtils.strafe();
        }
        
        PotionEffect speedEffect = mc.player.getActivePotionEffect(Objects.requireNonNull(Potion.getPotionById(1)));
        int speedAmplifier = (speedEffect != null) ? speedEffect.getAmplifier() + 1 : 0;
        
        if (speedAmplifier > 1 && mc.player.offGroundTicks == 3) {
            mc.player.motionX *= 1.12;
            mc.player.motionZ *= 1.12;
        }
        
        if (mc.player.offGroundTicks == 4) {
            mc.player.motionY = -0.09800000190734863;
        }
        
        if (mc.player.hurtTime == 9) {
            MoveUtils.setSpeed(Math.max(MoveUtils.getSpeed(), 0.5f), true);
        }
    }
}
