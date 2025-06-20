package loftily.module.impl.combat.criticals;

import loftily.event.impl.player.AttackEvent;
import loftily.module.impl.combat.Criticals;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.EntityLivingBase;

public class LowJumpCriticals extends Mode<Criticals> {
    public LowJumpCriticals() {
        super("LowJump");
    }
    
    private final NumberValue lowJumpMotion = new NumberValue("LowJump-Motion", 0.3425, 0.1, 0.42, 0.01);
    
    @EventHandler
    public void onAttack(AttackEvent event) {
        if (event.getTarget() instanceof EntityLivingBase) {
            if (mc.player.onGround) {
                mc.player.motionY = lowJumpMotion.getValue();
            }
        }
    }
}
