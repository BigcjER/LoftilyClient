package loftily.module.impl.combat.criticals;


import loftily.event.impl.player.AttackEvent;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.module.impl.combat.Criticals;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.EntityLivingBase;

public class JumpCriticals extends Mode<Criticals> {
    public JumpCriticals() {
        super("Jump");
    }

    public boolean jump = false;

    @EventHandler
    public void onJump(JumpEvent event) {
        jump = true;
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (event.getTarget() instanceof EntityLivingBase) {
            if (!jump) {
                if (mc.player.onGround) {
                    mc.player.tryJump();
                }
            } else {
                jump = false;
            }
        }
    }
}
