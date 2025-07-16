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

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (event.getTarget() instanceof EntityLivingBase) {
            if (mc.player.onGround) {
                mc.player.tryJump();
            }
        }
    }
}
