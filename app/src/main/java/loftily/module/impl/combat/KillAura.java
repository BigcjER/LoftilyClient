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
import net.minecraft.item.Item;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

import java.util.*;

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
    private final ModeValue cpsMode = new ModeValue("CPSMode","Normal",this,
            new StringMode("Normal"),
            new StringMode("Gaussian"),
            new StringMode("Jitter")
    );
    private final RangeSelectionNumberValue jitterPercent = new RangeSelectionNumberValue("JitterPercent", 0.1, 0.5, 0.0, 1.0, 0.01);
    private final NumberValue gaussianSigma = new NumberValue("Gaussian-Sigma",2.5,0.0,10.0,0.01).setVisible(()->cpsMode.is("Gaussian"));
    private final RangeSelectionNumberValue cpsValue = new RangeSelectionNumberValue("CPS", 8, 15, 0, 20, 0.1);
    private final BooleanValue fastOnFirstHit = new BooleanValue("FastOnFirstHit", false);
    private final ModeValue noDoubleHit = new ModeValue("NoDoubleHit", "Cancel", this, new StringMode("Cancel"), new StringMode("NextHit"), new StringMode("None"));
    private final NumberValue hurtTime = new NumberValue("HurtTime", 0, 0, 20);
    private final BooleanValue rayCast = new BooleanValue("RayCast", false);
    private final BooleanValue rayCastThroughWalls = new BooleanValue("RayCastThroughWalls", false);
    private final BooleanValue rayCastOnlyTarget = new BooleanValue("RayCastOnlyTarget", false);
    private final ModeValue keepSprintMode = new ModeValue("KeepSprintMode", "None", this, new StringMode("None"), new StringMode("Always"), new StringMode("WhenNotHurt"));
    private final BooleanValue noInventory = new BooleanValue("NoInventory", false);
    private final BooleanValue noGui = new BooleanValue("NoGui", false);

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
    //Improved
    private final BooleanValue predictAimPlayer = new BooleanValue("PlayerPredict", false);
    private final RangeSelectionNumberValue horizontalMultiplierPlayer = new RangeSelectionNumberValue("HMultiplier-Player", 0.1, 0.8, -4.00, 4.00,0.01)
            .setVisible(predictAimPlayer::getValue);
    private final RangeSelectionNumberValue verticalMultiplierPlayer = new RangeSelectionNumberValue("VMultiplier-Player", 0.1, 0.8, -4.00, 4.00,0.01)
            .setVisible(predictAimPlayer::getValue);

    private final BooleanValue predictAimTarget = new BooleanValue("TargetPredict", false);
    private final RangeSelectionNumberValue horizontalMultiplierTarget = new RangeSelectionNumberValue("HMultiplier-Target", 0.1, 0.8, -4.00, 4.00,0.01)
            .setVisible(predictAimTarget::getValue);
    private final RangeSelectionNumberValue verticalMultiplierTarget = new RangeSelectionNumberValue("VMultiplier-Target", 0.1, 0.8, -4.00, 4.00,0.01)
            .setVisible(predictAimTarget::getValue);
    private final NumberValue maxHorizontalPredict = new NumberValue("MaxHorizontalPredict",1.50,0.0,6.00,0.1);
    private final NumberValue maxVerticalPredict = new NumberValue("MaxVerticalPredict",1.5,0.0,6.00,0.1);
    //Movement
    private final ModeValue moveFixMode = new ModeValue("MoveFixMode", "None", this,
            new StringMode("None"),
            new StringMode("Strict"),
            new StringMode("Silent"));
    //AutoBlock
    private final ModeValue autoBlockMode = new ModeValue("AutoBlockMode", "None", this,
            new StringMode("HoldKey"),
            new StringMode("MatrixDamage"),
            new StringMode("AfterTick"),
            new StringMode("Packet"),
            new StringMode("None"));
    private final ModeValue blockTiming = new ModeValue("AutoBlockTiming","Normal",this,
            new StringMode("Normal"),
            new StringMode("Tick"),
            new StringMode("Pre"),
            new StringMode("Post"),
            new StringMode("AfterAttack")
    );
    private final BooleanValue onlyWhileKeyBinding = new BooleanValue("OnlyWhileKeyBinding", false);
    private final BooleanValue sendInteractPacket= new BooleanValue("InteractPacket",false);

    private final List<EntityLivingBase> targets = new ArrayList<>();
    private final DelayTimer attackTimer = new DelayTimer();
    private final DelayTimer targetTimer = new DelayTimer();
    public EntityLivingBase target = null;
    private int attackDelay = 0;
    private int canAttackTimes = 0;
    private boolean blockingTick = false;
    private boolean blockingStatus = false;
    
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

    public AxisAlignedBB getTargetBox(EntityLivingBase target) {
        AxisAlignedBB basicBox = target.getBox();

        if(predictAimPlayer.getValue()){
            double horizontal = RandomUtils.randomDouble(horizontalMultiplierPlayer.getFirst(),horizontalMultiplierPlayer.getSecond());
            double vertical = RandomUtils.randomDouble(verticalMultiplierPlayer.getFirst(),verticalMultiplierPlayer.getSecond());
            basicBox = basicBox.offset(
                    mc.player.motionX * horizontal,
                    mc.player.motionY * vertical,
                    mc.player.motionZ * horizontal
            );
        }

        if(predictAimTarget.getValue()){
            double horizontal = RandomUtils.randomDouble(horizontalMultiplierTarget.getFirst(),horizontalMultiplierTarget.getSecond());
            double vertical = RandomUtils.randomDouble(verticalMultiplierTarget.getFirst(),verticalMultiplierTarget.getSecond());
            basicBox = basicBox.offset(
                    (target.posX - target.lastTickPosX) * horizontal,
                    (target.posY - target.lastTickPosY) * vertical,
                    (target.posZ - target.lastTickPosZ) * horizontal
            );
        }
        AxisAlignedBB xzExpandBox = target.getBox().expand(maxHorizontalPredict.getValue(),0.0,maxHorizontalPredict.getValue());
        AxisAlignedBB yExpandBox = target.getBox().expand(0.0,maxVerticalPredict.getValue(),0.0);
        if(!basicBox.intersectsWith(xzExpandBox)){
            if(basicBox.minX > xzExpandBox.maxX){
                basicBox = basicBox.offset(basicBox.minX - xzExpandBox.maxX,0,0);
            }else if(basicBox.maxX < xzExpandBox.minX){
                basicBox = basicBox.offset(basicBox.maxX - xzExpandBox.minX,0,0);
            }
            if(basicBox.minZ > xzExpandBox.maxZ){
                basicBox = basicBox.offset(0,0,basicBox.minZ - xzExpandBox.maxZ);
            }else if(basicBox.maxZ < xzExpandBox.minZ){
                basicBox = basicBox.offset(0,0,basicBox.maxZ - xzExpandBox.minZ);
            }
        }
        if(!basicBox.intersectsWith(yExpandBox)){
            if(basicBox.minY > xzExpandBox.maxY){
                basicBox = basicBox.offset(0,basicBox.minY - xzExpandBox.maxY,0);
            }else if(basicBox.maxY < xzExpandBox.minY){
                basicBox = basicBox.offset(0,basicBox.maxY - xzExpandBox.minY,0);
            }
        }

        return basicBox;
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
        AxisAlignedBB targetBox = getTargetBox(target);
        
        switch (rotationMode.getValue().getName()) {
            case "Advance":
            case "Advance2":
                break;
            case "LockCenter":
                center = targetBox.lerpWith(0.5, 0.5, 0.5);
                break;
            case "Lower":
                for (double x = 0.3; x <= 0.6; x += 0.1) {
                    for (double y = 0.1; y <= 0.6; y += 0.1) {
                        for (double z = 0.3; z <= 0.6; z += 0.1) {
                            Vec3d preCenter = targetBox.lerpWith(x, y, z);

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
                center = targetBox.lerpWith(0.5, 0.7, 0.5);
                break;
            case "NearestCenter":
                center = CalculateUtils.getClosestPoint(mc.player.getEyes(), targetBox);
                break;
            case "Normal":
                for (double x = 0.2; x <= 0.8; x += 0.1) {
                    for (double y = 0.2; y <= 0.8; y += 0.1) {
                        for (double z = 0.2; z <= 0.8; z += 0.1) {
                            Vec3d preCenter = targetBox.lerpWith(x, y, z);

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
        if (mc.player == null) return;

        target = getTarget();

        rotation(target);

        if (mc.player == null) return;

        if (autoBlockMode.is("MatrixDamage") && canBlock() && target != null) {
            if (canAttackTimes <= 0) {
            } else {
                mc.gameSettings.keyBindUseItem.setPressed(false);
            }
        }

        if(blockTiming.is("Tick")){
            runAutoBlock(target);
        }

        if (attackTimeMode.is("Tick")) {
            attackTarget(target);
        }

    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (mc.player == null) return;

        if ((blockTiming.is("Pre") && event.isPre())
                || (blockTiming.is("Post") && event.isPost())) {
            runAutoBlock(target);
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
        blockingTick = false;
        target = null;
        targets.clear();
        mc.gameSettings.keyBindUseItem.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem));
        if(blockingStatus){
            PacketUtils.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            blockingStatus = false;
        }
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

        if(target != null){
            if(!TargetsHandler.canAdd(target)) {
                target = null;
            }
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
                        RotationUtils.toRotation(getTargetBox(entityLivingBase).getCenter(),mc.player), new Rotation(mc.player.rotationYaw,mc.player.rotationPitch)
                ))));
                break;
            case "Random":
                targets.sort((Comparator.comparingInt(entityLivingBase -> RandomUtils.randomInt(-100,100))));
                break;
        }

        targets.sort((Comparator.comparingDouble(entityLivingBase -> Math.max(0,CalculateUtils.getClosetDistance(mc.player, entityLivingBase) - attackRange.getValue()))));
        
        for (EntityLivingBase entity : targets) {
            if (entity == mc.player || entity == null || !TargetsHandler.canAdd(entity)) continue;
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

    private void sendInteractPacket(Entity target){
        if(sendInteractPacket.getValue()){
            RayTraceResult raytrace = mc.player.rayTrace(rotationRange.getValue(),1f);
            if(raytrace != null) {
                mc.playerController.syncCurrentPlayItem();
                Vec3d vec3d = new Vec3d(raytrace.hitVec.xCoord - target.posX, raytrace.hitVec.yCoord - target.posY, raytrace.hitVec.zCoord - target.posZ);
                PacketUtils.sendPacket(new CPacketUseEntity(target,EnumHand.MAIN_HAND,vec3d));
                PacketUtils.sendPacket(new CPacketUseEntity(target,EnumHand.MAIN_HAND,vec3d));
            }
        }
    }

    private void blockingPacket(Entity target){
        sendInteractPacket(target);
        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
    }

    private void runAutoBlock(Entity target){
        if(target == null) return;
        if(onlyWhileKeyBinding.getValue() && !GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)){
            return;
        }
        if(canBlock()) {
            switch (autoBlockMode.getValueByName()) {
                case "HoldKey":
                    mc.gameSettings.keyBindUseItem.setPressed(true);
                    break;
                case "MatrixDamage":
                    break;
                case "AfterTick":
                    if (canAttackTimes > 0) {
                        if(blockingTick) {
                            Item handItem = mc.player.getHeldItem(mc.player.getActiveHand()).getItem();
                            if ((handItem instanceof ItemSword || handItem instanceof ItemShield)) {
                                PacketUtils.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                                blockingTick = false;
                            }
                        }
                    } else {
                        if (!blockingTick) {
                            sendInteractPacket(target);
                            PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
                            blockingTick = true;
                        }
                        mc.gameSettings.keyBindUseItem.setPressed(true);
                    }
                    break;
                case "Packet":
                    blockingPacket(target);
                    break;
            }
        }else {
            mc.gameSettings.keyBindUseItem.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem));
        }
    }

    private void attackTarget(EntityLivingBase target) {
        
        if (mc.player == null || target == null) return;

        if((noInventory.getValue() && mc.currentScreen instanceof GuiInventory) || (noGui.getValue() && mc.currentScreen != null)) {
            mc.gameSettings.keyBindUseItem.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem));
            return;
        }

        if (Objects.equals(noDoubleHit.getValue().getName(), "Cancel")) {
            if (canAttackTimes >= 2) {
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

        if(blockTiming.is("Normal")) {
            runAutoBlock(target);
        }

        if (bestTarget == null || (bestTarget != target && rayCastOnlyTarget.getValue())) return;

        if(!canBeSeenEntity(mc.player,bestTarget) && CalculateUtils.getClosetDistance(mc.player,(EntityLivingBase) bestTarget) > throughWallAttackRange.getValue()){
            return;
        }

        if (((EntityLivingBase)bestTarget).hurtTime > hurtTime.getValue()) return;

        if (!TargetsHandler.canAdd(bestTarget)) {
            this.target = null;
            canAttackTimes = 1;
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

        if(blockTiming.is("AfterAttack")){
            runAutoBlock(target);
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
        Random random = new Random();
        int cps = (int) Math.round(RandomUtils.randomDouble(cpsValue.getFirst(), cpsValue.getSecond()));
        int basicDelay = (1000 / (cps == 0 ? 1 : cps));
        switch (cpsMode.getValueByName()) {
            case "Normal":
                return basicDelay;
            case "Gaussian":
                return (int) (1000 / (Math.sqrt(gaussianSigma.getValue()) * random.nextGaussian() + (float)cps));
            case "Jitter":
                double jitterAmount = basicDelay * RandomUtils.randomDouble(jitterPercent.getFirst(),jitterPercent.getSecond());

                double jitter = (random.nextDouble() * 2 - 1) * jitterAmount;
                return (int) (basicDelay + (long) jitter);
        }
        return 200;
    }

    private boolean canBlock(){
        if(noInventory.getValue() && mc.currentScreen instanceof GuiInventory){
            return false;
        }
        return mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword && (mc.player.getHeldItem(EnumHand.OFF_HAND).isEmptyStack()
        || mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemShield);
    }
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
