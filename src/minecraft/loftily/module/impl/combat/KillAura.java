package loftily.module.impl.combat;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.render.Render3DEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.impl.RotationHandler;
import loftily.handlers.impl.TargetsHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.CalculateUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.math.Rotation;
import loftily.utils.player.RayCastUtils;
import loftily.utils.player.RotationUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@ModuleInfo(name = "KillAura", key = Keyboard.KEY_R, category = ModuleCategory.Combat)
public class KillAura extends Module {
    
    //Attack
    private final ModeValue attackTimeMode = new ModeValue("AttackTimeMode", "Tick", this,
            new StringMode("Tick"),
            new StringMode("Pre"),
            new StringMode("Post")
    );
    private final ModeValue attackMode = new ModeValue("AttackMode", "Packet", this,
            new StringMode("Packet"),
            new StringMode("Legit")
    );
    private final RangeSelectionNumberValue CPSValue = new RangeSelectionNumberValue("CPS", 8, 15, 0, 20, 1);
    private final BooleanValue fastOnFirstHit = new BooleanValue("FastOnFirstHit", false);
    private final ModeValue noDoubleHit = new ModeValue("NoDoubleHit", "Cancel", this, new StringMode("Cancel"), new StringMode("NextHit"));
    private final NumberValue hurtTime = new NumberValue("HurtTime", 0, 0, 20);
    private final BooleanValue rayCast = new BooleanValue("RayCast", false);
    private final BooleanValue rayCastThroughWalls = new BooleanValue("RayCastThroughWalls", false);
    private final BooleanValue rayCastOnlyTarget = new BooleanValue("RayCastOnlyTarget", false);
    private final ModeValue keepSprintMode = new ModeValue("KeepSprintMode", "None", this, new StringMode("None"), new StringMode("Always"), new StringMode("WhenNotHurt"));
    private final BooleanValue noInventory = new BooleanValue("NoInventory", false);
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
    
    //Rotation
    private final ModeValue rotationMode = new ModeValue("RotationMode", "LockCenter", this,
            new StringMode("LockCenter"),
            new StringMode("LockHead"),
            new StringMode("NearestCenter"),
            new StringMode("Normal"),
            new StringMode("None"));
    private final RangeSelectionNumberValue yawTurnSpeed = new RangeSelectionNumberValue("YawTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue pitchTurnSpeed = new RangeSelectionNumberValue("PitchTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue keepTicks = new RangeSelectionNumberValue("KeepRotationTicks", 1, 2, 1, 20);
    private final RangeSelectionNumberValue backTicks = new RangeSelectionNumberValue("ReverseTicks", 1, 2, 1, 20);
    private final BooleanValue silentRotation = new BooleanValue("SilentRotation", false);
    private final ModeValue moveFixMode = new ModeValue("MoveFixMode", "None", this,
            new StringMode("None"),
            new StringMode("Strict"),
            new StringMode("Silent"));
    
    private final BooleanValue throughWallsAim = new BooleanValue("ThroughWallsAim", false);
    private final List<EntityLivingBase> targets = new ArrayList<>();
    private final DelayTimer attackTimer = new DelayTimer();
    private final DelayTimer targetTimer = new DelayTimer();
    public EntityLivingBase target = null;
    private int attackDelay = 0;
    private int canAttackTimes = 0;
    
    {
        rotationRange = new NumberValue("RotationRange", 6, 0, 10, 0.1);
        swingRange = new NumberValue("SwingRange", 6, 0, 10, 0.1)
                .setMaxWith(rotationRange);
        attackRange = new NumberValue("AttackRange", 6, 0, 10, 0.1)
                .setMaxWith(swingRange);
        
        swingRange.setMinWith(attackRange);
        rotationRange.setMinWith(swingRange);
    }

    public void rotation(EntityLivingBase target) {
        if (target == null) return;

        float horizonSpeed = (float) RandomUtils.randomDouble(yawTurnSpeed.getFirst(), yawTurnSpeed.getSecond());
        float pitchSpeed = (float) RandomUtils.randomDouble(pitchTurnSpeed.getFirst(), pitchTurnSpeed.getSecond());

        int keepTicks = RandomUtils.randomInt((int) Math.round(this.keepTicks.getFirst()), (int) Math.round(this.keepTicks.getSecond()));
        int reverseTicks = RandomUtils.randomInt((int) Math.round(this.backTicks.getFirst()), (int) Math.round(this.backTicks.getSecond()));

        Rotation calculateRot = RotationUtils.smoothRotation(
                RotationHandler.getCurrentRotation(),
                calculateRotation(target),
                horizonSpeed,
                pitchSpeed
        );
        if (silentRotation.getValue()) {
            RotationHandler.setClientRotation(calculateRot, keepTicks, reverseTicks,moveFixMode.getValue().getName());
        } else {
            mc.player.rotationYaw = calculateRot.yaw;
            mc.player.rotationPitch = calculateRot.pitch;
        }
    }

    public Rotation calculateRotation(EntityLivingBase target) {
        
        Vec3d center = null;
        Rotation currentRotation = null;
        
        switch (rotationMode.getValue().getName()) {
            case "LockCenter":
                center = target.getBox().lerpWith(0.5, 0.5, 0.5);
                break;
            case "LockHead":
                center = target.getBox().lerpWith(0.5, 0.7, 0.5);
                break;
            case "NearestCenter":
                center = CalculateUtils.getClosestPoint(mc.player.getEyes(), target.getBox());
                break;
            case "Normal":
                for (double x = 0.2; x <= 0.8; x += 0.1) {
                    for (double y = 0.2; y <= 0.8; y += 0.1) {
                        for (double z = 0.2; z <= 0.8; z += 0.1) {
                            Vec3d preCenter = target.getBox().lerpWith(x, y, z);

                            if (CalculateUtils.isVisible(preCenter) || throughWallsAim.getValue()) {
                                if (center == null || RotationUtils.getRotationDifference(RotationUtils.toRotation(preCenter, mc.player), RotationHandler.getCurrentRotation()) < RotationUtils.getRotationDifference(RotationUtils.toRotation(center, mc.player), RotationHandler.getCurrentRotation())) {
                                    center = preCenter;
                                }
                            }
                        }
                    }
                }
        }
        
        if (center != null) {
            if (throughWallsAim.getValue() || CalculateUtils.isVisible(center)) {
                currentRotation = RotationUtils.toRotation(center, mc.player);
            }
        }
        
        return currentRotation;
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
        
        if (event.isPost()) {
            rotation(target);
        }
        
        if ((attackTimeMode.is("Pre") && event.isPre())
                || (attackTimeMode.is("Post") && event.isPost())) {
            attackTarget(target);
        }
    }
    
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
            if (canAttackTimes > 1) {
                if (Objects.equals(noDoubleHit.getValue().getName(), "NextHit")) {
                    return;
                }
            }
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
            if (CalculateUtils.getClosetDistance(mc.player, target) <= attackRange.getValue()) {
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
                targets.sort((Comparator.comparingDouble(entityLivingBase -> CalculateUtils.getClosetDistance(mc.player, entityLivingBase))));
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

        if(noInventory.getValue() && mc.currentScreen instanceof GuiInventory) {
            return;
        }

        if (Objects.equals(noDoubleHit.getValue().getName(), "Cancel")) {
            if (canAttackTimes > 1) {
                canAttackTimes = 1;
            }
        }
        while (canAttackTimes > 0) {
            canAttackTimes--;
            Entity bestTarget;
            Rotation rotation = RotationHandler.clientRotation == null ? RotationHandler.getRotation() : RotationHandler.clientRotation;
            if (!rayCast.getValue()) {
                bestTarget = target;
            } else {
                bestTarget = RayCastUtils.raycastEntity(attackRange.getValue(), rotation.yaw, rotation.pitch, rayCastThroughWalls.getValue() ,(entity -> entity instanceof EntityLivingBase));
            }
            
            if (bestTarget == null || (bestTarget != target && rayCastOnlyTarget.getValue())) return;
            
            if (CalculateUtils.getClosetDistance(mc.player, (EntityLivingBase) bestTarget) <= attackRange.getValue()) {
                if(attackMode.is("Packet")) {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.playerController.attackEntity(mc.player, bestTarget);
                    if (!keepSprintMode.is("Always")) {
                        if (keepSprintMode.is("None") || mc.player.hurtTime > 0) {
                            mc.player.attackTargetEntityWithCurrentItem(target);
                        }
                    }
                }else if(attackMode.is("Legit")){
                    mc.clickMouse();
                }
            } else {
                if (CalculateUtils.getClosetDistance(mc.player, (EntityLivingBase) bestTarget) <= swingRange.getValue()) {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                }
            }
        }
    }
    
    private int calculateDelay() {
        return 1000 / (int) RandomUtils.randomDouble(CPSValue.getFirst(), CPSValue.getSecond());
    }
    
}
