package loftily.module.impl.combat;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import loftily.Client;
import loftily.event.impl.player.AttackEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.render.Render3DEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.handlers.impl.RotationHandler;
import loftily.handlers.impl.TargetsHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.CalculateUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.math.Rotation;
import loftily.utils.player.MoveUtils;
import loftily.utils.player.RayCastUtils;
import loftily.utils.player.RotationUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import lombok.NonNull;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@ModuleInfo(name = "KillAura", key = Keyboard.KEY_R, category = ModuleCategory.COMBAT)
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
    private final ModeValue swingMode = new ModeValue("SwingMode", "Vanilla", this,
            new StringMode("Vanilla"),
            new StringMode("Packet"),
            new StringMode("NoPacket")
    );
    private final RangeSelectionNumberValue CPSValue = new RangeSelectionNumberValue("CPS", 8, 15, 0, 20, 0.1);
    private final BooleanValue fastOnFirstHit = new BooleanValue("FastOnFirstHit", false);
    private final ModeValue noDoubleHit = new ModeValue("NoDoubleHit", "Cancel", this, new StringMode("Cancel"), new StringMode("NextHit"), new StringMode("None"));
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
    private final NumberValue throughWallAttackRange;
    //Target
    private final ModeValue targetSortingMode = new ModeValue("TargetSortingMode", "Distance", this,
            new StringMode("Distance"),
            new StringMode("HurtTime"),
            new StringMode("Health"),
            new StringMode("Angle"),
            new StringMode("Random")
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
            new StringMode("Lower"),
            new StringMode("Normal"),
            new StringMode("Advance"),
            new StringMode("None"));
    private final RangeSelectionNumberValue yawTurnSpeed = new RangeSelectionNumberValue("YawTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue pitchTurnSpeed = new RangeSelectionNumberValue("PitchTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue keepTicks = new RangeSelectionNumberValue("KeepRotationTicks", 1, 2, 0, 20);
    private final RangeSelectionNumberValue backTicks = new RangeSelectionNumberValue("ReverseTicks", 1, 2, 0, 20);
    private final BooleanValue silentRotation = new BooleanValue("SilentRotation", false);
    private final BooleanValue throughWallsAim = new BooleanValue("ThroughWallsAim", false);
    //Movement
    private final ModeValue moveFixMode = new ModeValue("MoveFixMode", "None", this,
            new StringMode("None"),
            new StringMode("Strict"),
            new StringMode("Silent"));
    //AutoBlock
    private final ModeValue autoBlockMode = new ModeValue("AutoBlockMode", "None", this,
            new StringMode("HoldKey"),
            new StringMode("None"),
            new StringMode("MatrixDamage"));

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
        throughWallAttackRange = new NumberValue("ThroughWallAttackRange", 6, 0, 10, 0.1)
                .setMaxWith(attackRange);

        attackRange.setMinWith(throughWallAttackRange);
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
                RotationHandler.getRotation(),
                calculateRotation(target),
                horizonSpeed,
                pitchSpeed
        ).fixedSensitivity(0);
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
            case "Advance":
            case "Advance2":
                break;
            case "LockCenter":
                center = target.getBox().lerpWith(0.5, 0.5, 0.5);
                break;
            case "Lower":
                for (double x = 0.4; x <= 0.6; x += 0.1) {
                    for (double y = 0.1; y <= 0.6; y += 0.1) {
                        for (double z = 0.4; z <= 0.6; z += 0.1) {
                            Vec3d preCenter = target.getBox().lerpWith(x, y, z);

                            if(rayCast.getValue() && !rayCastThroughWalls.getValue()){
                                Rotation rotation = RotationUtils.toRotation(preCenter,mc.player);
                                Entity entity = RayCastUtils.raycastEntity(attackRange.getValue(), rotation.yaw, rotation.pitch, rayCastThroughWalls.getValue(), (e -> e instanceof EntityLivingBase));
                                if(entity == null || (entity != target && rayCastOnlyTarget.getValue())) continue;
                            }
                            if (CalculateUtils.isVisible(preCenter) || throughWallsAim.getValue()) {
                                if (center == null || RotationUtils.getRotationDifference(RotationUtils.toRotation(preCenter, mc.player), RotationHandler.getRotation()) < RotationUtils.getRotationDifference(RotationUtils.toRotation(center, mc.player), RotationHandler.getRotation())
                                || mc.player.getEyes().distanceTo(preCenter) < mc.player.getEyes().distanceTo(center)) {
                                    center = preCenter;
                                }
                            }
                        }
                    }
                }
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

                            if(rayCast.getValue() && !rayCastThroughWalls.getValue()){
                                Rotation rotation = RotationUtils.toRotation(preCenter,mc.player);
                                Entity entity = RayCastUtils.raycastEntity(attackRange.getValue(), rotation.yaw, rotation.pitch, rayCastThroughWalls.getValue(), (e -> e instanceof EntityLivingBase));
                                if(entity == null || (entity != target && rayCastOnlyTarget.getValue())) continue;
                            }
                            if (CalculateUtils.isVisible(preCenter) || throughWallsAim.getValue()) {
                                if (center == null || RotationUtils.getRotationDifference(RotationUtils.toRotation(preCenter, mc.player), RotationHandler.getRotation()) < RotationUtils.getRotationDifference(RotationUtils.toRotation(center, mc.player), RotationHandler.getRotation())) {
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

        switch (rotationMode.getValue().getName()) {
            case "NearestCenter":
            case "Normal":
            case "LockHead":
            case "LockCenter":
                break;
            case "Advance":
                currentRotation = RotationUtils.findBestRotationSimulatedAnnealing(mc.player,target);
                break;
        }
        
        return currentRotation;
    }

    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.player == null || rotationMode.is("None")) return;

        rotation(target);

        if (mc.player == null) return;

        target = getTarget();

        if (autoBlockMode.is("MatrixDamage") && canBlock() && target != null) {
            if (canAttackTimes > 0)
                mc.gameSettings.keyBindUseItem.setPressed(false);
        }

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
        mc.gameSettings.keyBindUseItem.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem));
    }
    
    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null) return;

        if (attackTimer.hasTimeElapsed(attackDelay) && target != null && ((fastOnFirstHit.getValue() && canAttackTimes <= 1) || CalculateUtils.getClosetDistance(mc.player, target) <= swingRange.getValue() + 0.1)) {
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

        if (autoBlockMode.is("MatrixDamage") && canBlock() && target != null) {
            if (canAttackTimes <= 0) {
                if ((mc.player.hurtTime >= 2 && mc.player.hurtTime <= 10) || !MoveUtils.isMoving() || GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)) {
                    mc.gameSettings.keyBindUseItem.setPressed(true);
                }
            } else {
                mc.gameSettings.keyBindUseItem.setPressed(false);
            }
        }
    }
    
    private EntityLivingBase getTarget() {
        if (mc.player == null) return null;

        if(target != null && target instanceof EntityPlayer && ((EntityPlayer) target).isSpectator()){
            target = null;
        }

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
            case "Distance":
                targets.sort((Comparator.comparingDouble(entityLivingBase -> CalculateUtils.getClosetDistance(mc.player, entityLivingBase))));
                break;
            case "HurtTime":
                targets.sort((Comparator.comparingInt(entityLivingBase -> entityLivingBase.hurtTime)));
                break;
            case "Health":
                targets.sort((Comparator.comparingDouble(EntityLivingBase::getHealth)));
                break;
            case "Angle":
                targets.sort((Comparator.comparingDouble(entityLivingBase -> RotationUtils.getRotationDifference(
                        RotationUtils.toRotation(entityLivingBase.getBox().getCenter(),mc.player), new Rotation(mc.player.rotationYaw,mc.player.rotationPitch)
                ))));
                break;
            case "Random":
                targets.sort((Comparator.comparingInt(entityLivingBase -> RandomUtils.randomInt(-100,100))));
                break;
        }

        targets.sort((Comparator.comparingDouble(entityLivingBase -> Math.max(0,CalculateUtils.getClosetDistance(mc.player, entityLivingBase) - attackRange.getValue()))));
        
        for (EntityLivingBase entity : targets) {
            if (entity == mc.player || entity == null || (entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator())) continue;
            targetTimer.reset();
            return entity;
        }
        
        return null;
    }

    public static boolean canBeSeenEntity(Entity player, Entity target) {
        AxisAlignedBB targetBB = target.getEntityBoundingBox();

        Vec3d center = new Vec3d(
                (targetBB.minX + targetBB.maxX) / 2,
                (targetBB.minY + targetBB.maxY) / 2,
                (targetBB.minZ + targetBB.maxZ) / 2
        );

        Vec3d[] corners = new Vec3d[] {
                new Vec3d(targetBB.minX, targetBB.minY, targetBB.minZ),
                new Vec3d(targetBB.minX, targetBB.minY, targetBB.maxZ),
                new Vec3d(targetBB.minX, targetBB.maxY, targetBB.minZ),
                new Vec3d(targetBB.minX, targetBB.maxY, targetBB.maxZ),
                new Vec3d(targetBB.maxX, targetBB.minY, targetBB.minZ),
                new Vec3d(targetBB.maxX, targetBB.minY, targetBB.maxZ),
                new Vec3d(targetBB.maxX, targetBB.maxY, targetBB.minZ),
                new Vec3d(targetBB.maxX, targetBB.maxY, targetBB.maxZ)
        };

        Vec3d eyePos = player.getPositionEyes(1.0F);

        if (player.world.rayTraceBlocks(eyePos, center) == null) return true;

        for (Vec3d corner : corners) {
            if (player.world.rayTraceBlocks(eyePos, corner) == null) {
                return true;
            }
        }

        return false;
    }
    
    private void attackTarget(EntityLivingBase target) {
        
        if (mc.player == null || target == null) return;

        if(canBlock()) {
            switch (autoBlockMode.getValueByName()) {
                case "HoldKey":
                    mc.gameSettings.keyBindUseItem.setPressed(true);
                    break;
                case "MatrixDamage":
                    break;
            }
        }else {
            mc.gameSettings.keyBindUseItem.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem));
        }
        
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
        Entity bestTarget;
        Rotation rotation = RotationHandler.clientRotation == null ? RotationHandler.getRotation() : RotationHandler.clientRotation;
        if (!rayCast.getValue()) {
            bestTarget = target;
        } else {
            bestTarget = RayCastUtils.raycastEntity(attackRange.getValue(), rotation.yaw, rotation.pitch, rayCastThroughWalls.getValue(), (entity -> entity instanceof EntityLivingBase));
        }

        if (bestTarget == null || (bestTarget != target && rayCastOnlyTarget.getValue())) return;

        if(!canBeSeenEntity(mc.player,bestTarget) && CalculateUtils.getClosetDistance(mc.player,(EntityLivingBase) bestTarget) > throughWallAttackRange.getValue()){
            return;
        }

        while (canAttackTimes > 0) {
            canAttackTimes--;
            if (CalculateUtils.getClosetDistance(mc.player, (EntityLivingBase) bestTarget) <= attackRange.getValue()) {
                if(attackMode.is("Packet")) {
                    if(!ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)){
                        swing();
                    }
                    
                    AttackEvent event = new AttackEvent(target);
                    Client.INSTANCE.getEventManager().call(event);
                    if (event.isCancelled()) return;
                    
                    PacketUtils.sendPacket(new CPacketUseEntity(bestTarget));
                    if(ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)){
                        swing();
                    }
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
            if(noDoubleHit.is("NextHit")){
                break;
            }
        }
    }

    private void swing(){
        switch (swingMode.getValueByName()){
            case "Vanilla":
                mc.player.swingArm(EnumHand.MAIN_HAND);
                break;
            case "Packet":
                PacketUtils.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND),true);
                break;
            case "NoPacket":
                break;
        }
    }

    private int calculateDelay() {
        return  (1000 /  (int) Math.round(RandomUtils.randomDouble(CPSValue.getFirst(), CPSValue.getSecond())));
    }

    private boolean canBlock(){
        if(noInventory.getValue() && mc.currentScreen instanceof GuiInventory){
            return false;
        }
        return mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword && (mc.player.getHeldItem(EnumHand.OFF_HAND).func_190926_b()
        || mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemShield);
    }
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
