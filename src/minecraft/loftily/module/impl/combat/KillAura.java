package loftily.module.impl.combat;

import loftily.event.impl.player.MotionEvent;
import loftily.event.impl.render.Render2DEvent;
import loftily.event.impl.world.UpdateEvent;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "KillAura", key = Keyboard.KEY_R, category = ModuleCategory.Combat)
public class KillAura extends Module {
    //Attack
    private final NumberValue minCPS = new NumberValue("MinCPS",10.0,0.0,20.0,1);
    private final NumberValue maxCPS = new NumberValue("MaxCPS",15.0,0.0,20.0,1);
    private final ModeValue attackTimeMode = new ModeValue("AttackMode","Tick",this,
            new StringMode("Tick"),
            new StringMode("Pre"),
            new StringMode("Post")
    );
    private final BooleanValue fastOnFirstHit = new BooleanValue("Fast On First Hit",false);
    private final BooleanValue noDoubleHit = new BooleanValue("No Double Hit",false);
    private final NumberValue hurtTime = new NumberValue("Hurt Time",0.0,0.0,20.0,1);
    //Range
    private final NumberValue rotationRange = new NumberValue("RotationRange",6.0,0.0,10.0,0.1);
    private final NumberValue swingRange = new NumberValue("SwingRange",6.0,0.0,10.0,0.1);
    private final NumberValue attackRange = new NumberValue("AttackRange",6.0,0.0,10.0,0.1);
    //Target
    private final ModeValue targetMode = new ModeValue("TargetMode","Range",this,
            new StringMode("Range"),
            new StringMode("HurtTime"),
            new StringMode("Health")
    );
    private final ModeValue mode = new ModeValue("Mode","Single",this,
            new StringMode("Single"),
            new StringMode("Switch")
    );
    private final NumberValue switchDelay = new NumberValue("SwitchDelay",200,0.0,2000.0,1);


    private final DelayTimer attackTimer = new DelayTimer();
    private final DelayTimer targetTimer = new DelayTimer();
    public EntityLivingBase target = null;
    private List<EntityLivingBase> targets = new ArrayList<>();
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
    public void onUpdate(UpdateEvent event) {
        if(mc.player == null) return;
        target = getTarget();
        if(attackTimeMode.is("Tick")){
            attackTarget(target);
        }
    }

    @EventHandler
    public void onMotionEvent(MotionEvent event) {
        if(mc.player == null) return;
        if((attackTimeMode.is("Pre") && event.isPre())
        || (attackTimeMode.is("Post") && event.isPost())){
            attackTarget(target);
        }
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if(mc.player == null) return;
        if(attackTimer.hasTimeElapsed(attackDelay) && (fastOnFirstHit.getValue() || target != null)){
            canAttackTimes++;
            attackDelay = calculateDelay();
            attackTimer.reset();
        }
        if(target == null) {
            if(!fastOnFirstHit.getValue()) {
                canAttackTimes = 0;
            }
            targetTimer.reset();
        }
    }

    private int calculateDelay(){
        return 1000 / (int) RandomUtils.randomDouble(minCPS.getValue(),maxCPS.getValue());
    }

    private EntityLivingBase getTarget(){
        if(mc.player == null) return null;

        if(target != null) {
            if (mc.player.getDistanceToEntity(target) <= rotationRange.getValue()) {
                if (mode.is("Single")) return target;

                if (mode.is("Switch")) {
                    if (!targetTimer.hasTimeElapsed((int) Math.round(switchDelay.getValue()))) {
                        return target;
                    }
                }
            }
        }

        for (Entity entity : mc.world.loadedEntityList){
            if(entity == mc.player)continue;
            if(!(entity instanceof EntityLivingBase))continue;
            if(mc.player.getDistanceToEntity(entity) <= rotationRange.getValue()) {
                targets.add((EntityLivingBase) entity);
            }
        }

        switch (targetMode.getValue().getName()){
            case "Range":
                targets.sort((Comparator.comparingDouble(a -> mc.player.getDistanceToEntity(a))));
                break;
            case "HurtTime":
                targets.sort((Comparator.comparingInt(a-> a.hurtTime)));
                break;
            case "Health":
                targets.sort((Comparator.comparingDouble(EntityLivingBase::getHealth)));
                break;
        }

        for(EntityLivingBase entity : targets){
            if (entity == mc.player) continue;
            if(entity == null) continue;
            targetTimer.reset();
            return entity;
        }

        return null;
    }

    private void attackTarget(EntityLivingBase target){
        if(mc.player == null || target == null) return;

        if(target.hurtTime > hurtTime.getValue())return;

        if(target.getHealth() <= 0){
            this.target = null;
            canAttackTimes = 0;
            return;
        }

        if(noDoubleHit.getValue()){
            if(canAttackTimes > 1){
                canAttackTimes = 1;
            }
        }

        while (canAttackTimes > 0) {
            canAttackTimes--;
            if (mc.player.getDistanceToEntity(target) <= attackRange.getValue()) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.playerController.attackEntity(mc.player, target);
            }else {
                if(mc.player.getDistanceToEntity(target) <= swingRange.getValue())
                {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                }
            }
        }
    }
}
