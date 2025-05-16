package loftily.module.impl.combat;

import loftily.event.impl.player.MotionEvent;
import loftily.event.impl.render.Render3DEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.impl.TargetsHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.RandomUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "KillAura", key = Keyboard.KEY_R, category = ModuleCategory.Combat)
public class KillAura extends Module {
    private final ModeValue attackTimeMode = new ModeValue("AttackMode", "Tick", this,
            new StringMode("Tick"),
            new StringMode("Pre"),
            new StringMode("Post")
    );
    //Attack
    private final NumberValue maxCPS;
    private final NumberValue minCPS;
    private final BooleanValue fastOnFirstHit = new BooleanValue("FastOnFirstHit", false);
    private final BooleanValue noDoubleHit = new BooleanValue("NoDoubleHit", false);
    private final NumberValue hurtTime = new NumberValue("HurtTime", 0, 0, 20);
    //Range
    private final NumberValue rotationRange;
    private final NumberValue swingRange;
    private final NumberValue attackRange;
    //Target
    private final ModeValue targetSortingMode = new ModeValue("TargetSortingMode", "Range", this,
            new StringMode("Range"),
            new StringMode("HurtTime"),
            new StringMode("Health")
    );
    private final ModeValue mode = new ModeValue("Mode", "Single", this,
            new StringMode("Single"),
            new StringMode("Switch")
    );
    private final NumberValue switchDelay = new NumberValue("SwitchDelay", 200, 0, 2000)
            .setVisible(() -> mode.is("Switch"));
    private final List<EntityLivingBase> targets = new ArrayList<>();

    {
        minCPS = new NumberValue("MinCPS", 10, 0, 20);
        maxCPS = new NumberValue("MaxCPS", 15, 0, 20).setMinWith(minCPS);
        minCPS.setMaxWith(maxCPS);
    }

    {
        rotationRange = new NumberValue("RotationRange", 6, 0, 10, 0.1);
        swingRange = new NumberValue("SwingRange", 6, 0, 10, 0.1)
                .setMaxWith(rotationRange);
        attackRange = new NumberValue("AttackRange", 6, 0, 10, 0.1)
                .setMaxWith(swingRange);
        
        swingRange.setMinWith(attackRange);
        rotationRange.setMinWith(swingRange);
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null) return;
        
        target = getTarget();
        
        if (attackTimeMode.is("Tick")) {
            attackTarget(target);
        }
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if (mc.player == null) return;
        if ((attackTimeMode.is("Pre") && event.isPre())
                || (attackTimeMode.is("Post") && event.isPost())) {
            attackTarget(target);
        }
    }
    private final DelayTimer attackTimer = new DelayTimer();
    private final DelayTimer targetTimer = new DelayTimer();
    public EntityLivingBase target = null;
    
    private int attackDelay = 0;
    private int canAttackTimes = 0;
    
    @Override
    public void onEnable() {
        attackDelay = calculateDelay();
        canAttackTimes = 0;
        attackTimer.reset();
        targetTimer.reset();
    }
    
    @Override
    public void onDisable() {
        target = null;
        targets.clear();
    }
    
    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null) return;
        
        if (attackTimer.hasTimeElapsed(attackDelay) && (fastOnFirstHit.getValue() || target != null)) {
            canAttackTimes++;
            attackDelay = calculateDelay();
            attackTimer.reset();
        }
        
        if (target == null) {
            if (!fastOnFirstHit.getValue()) {
                canAttackTimes = 0;
            }
            targetTimer.reset();
        }
    }
    
    private EntityLivingBase getTarget() {
        if (mc.player == null) return null;
        
        if (target != null) {
            if (mc.player.getDistanceToEntity(target) <= rotationRange.getValue()) {
                if (mode.is("Single")) return target;
                
                if (mode.is("Switch")) {
                    if (!targetTimer.hasTimeElapsed((int) Math.round(switchDelay.getValue()))) {
                        return target;
                    }
                }
            }
        }
        
        List<EntityLivingBase> filteredTargets = TargetsHandler.getTargets(rotationRange.getValue());
        
        if (targets.size() != filteredTargets.size()) {
            targets.clear();
            targets.addAll(filteredTargets);
        }
        
        switch (targetSortingMode.getValue().getName()) {
            case "Range":
                targets.sort((Comparator.comparingDouble(entityLivingBase -> mc.player.getDistanceToEntity(entityLivingBase))));
                break;
            case "HurtTime":
                targets.sort((Comparator.comparingInt(entityLivingBase -> entityLivingBase.hurtTime)));
                break;
            case "Health":
                targets.sort((Comparator.comparingDouble(EntityLivingBase::getHealth)));
                break;
        }
        
        for (EntityLivingBase entity : targets) {
            if (entity == mc.player || entity == null) continue;
            targetTimer.reset();
            return entity;
        }
        
        return null;
    }
    
    private void attackTarget(EntityLivingBase target) {
        if (mc.player == null || target == null) return;
        
        if (target.hurtTime > hurtTime.getValue()) return;
        
        if (target.getHealth() <= 0) {
            this.target = null;
            canAttackTimes = 0;
            return;
        }
        
        if (noDoubleHit.getValue()) {
            if (canAttackTimes > 1) {
                canAttackTimes = 1;
            }
        }
        
        while (canAttackTimes > 0) {
            canAttackTimes--;
            if (mc.player.getDistanceToEntity(target) <= attackRange.getValue()) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.playerController.attackEntity(mc.player, target);
            } else {
                if (mc.player.getDistanceToEntity(target) <= swingRange.getValue()) {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                }
            }
        }
    }
    
    private int calculateDelay() {
        return 1000 / (int) RandomUtils.randomDouble(minCPS.getValue(), maxCPS.getValue());
    }
    
}
