package loftily.module.impl.combat;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import loftily.Client;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.AttackEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.render.Render3DEvent;
import loftily.event.impl.world.PreUpdateEvent;
import loftily.handlers.impl.client.TargetsHandler;
import loftily.handlers.impl.player.RotationHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.CalculateUtils;
import loftily.utils.math.MathUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.math.Rotation;
import loftily.utils.player.MoveUtils;
import loftily.utils.player.PlayerUtils;
import loftily.utils.player.RayCastUtils;
import loftily.utils.player.RotationUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.MultiBooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import lombok.Getter;
import lombok.NonNull;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final ModeValue cpsMode = new ModeValue("CPSMode", "Normal", this,
            new StringMode("Normal"),
            new StringMode("Gaussian"),
            new StringMode("Jitter")
    );
    private final RangeSelectionNumberValue jitterPercent = new RangeSelectionNumberValue("JitterPercent", 0.1, 0.5, 0.0, 1.0, 0.01);
    private final NumberValue gaussianSigma = new NumberValue("Gaussian-Sigma", 2.5, 0.0, 10.0, 0.01).setVisible(() -> cpsMode.is("Gaussian"));
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
            new StringMode("Test"),
            new StringMode("Proportion"),
            new StringMode("None"));
    private final RangeSelectionNumberValue distancePro = new RangeSelectionNumberValue("DistanceProportion",0.0,1.0,0,1.0,0.01).setVisible(()->rotationMode.is("Proportion"));
    private final RangeSelectionNumberValue anglePro = new RangeSelectionNumberValue("AngleProportion",0.0,1.0,0,1.0,0.01).setVisible(()->rotationMode.is("Proportion"));
    private final RangeSelectionNumberValue yawTurnSpeed = new RangeSelectionNumberValue("YawTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue pitchTurnSpeed = new RangeSelectionNumberValue("PitchTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue keepTicks = new RangeSelectionNumberValue("KeepRotationTicks", 1, 2, 0, 20);
    private final RangeSelectionNumberValue backTicks = new RangeSelectionNumberValue("ReverseTicks", 1, 2, 0, 20);
    private final BooleanValue silentRotation = new BooleanValue("SilentRotation", false);
    private final BooleanValue throughWallsAim = new BooleanValue("ThroughWallsAim", false);
    private final BooleanValue noRotationWhenFaceTarget = new BooleanValue("NoRotationWhenFaceTarget", false);
    private final BooleanValue delayRotation = new BooleanValue("DelayWhenTargetCantBeSeen", false);
    private final RangeSelectionNumberValue rotationDelay = new RangeSelectionNumberValue("RotationDelay", 100, 150, 0, 1000).setVisible(delayRotation::getValue);
    //Improve TargetBox
    private final BooleanValue predictAimPlayer = new BooleanValue("PlayerPredict", false);
    private final RangeSelectionNumberValue horizontalMultiplierPlayer = new RangeSelectionNumberValue("PlayerHMultiplier", 0.1, 0.8, -4.00, 4.00, 0.01)
            .setVisible(predictAimPlayer::getValue);
    private final RangeSelectionNumberValue verticalMultiplierPlayer = new RangeSelectionNumberValue("PlayerVMultiplier", 0.1, 0.8, -4.00, 4.00, 0.01)
            .setVisible(predictAimPlayer::getValue);
    
    private final BooleanValue predictAimTarget = new BooleanValue("TargetPredict", false);
    private final RangeSelectionNumberValue horizontalMultiplierTarget = new RangeSelectionNumberValue("TargetHMultiplier", 0.1, 0.8, -4.00, 4.00, 0.01)
            .setVisible(predictAimTarget::getValue);
    private final RangeSelectionNumberValue verticalMultiplierTarget = new RangeSelectionNumberValue("TargetVMultiplier", 0.1, 0.8, -4.00, 4.00, 0.01)
            .setVisible(predictAimTarget::getValue);
    private final BooleanValue boxExpand = new BooleanValue("TargetBoxExpand", false);
    private final NumberValue expandSizeH = new NumberValue("BoxHExpandSize", 0.1, -1.5, 1.5, 0.01).setVisible(boxExpand::getValue);
    private final NumberValue expandSizeV = new NumberValue("BoxVExpandSize", 0.1, -1.5, 1.5, 0.01).setVisible(boxExpand::getValue);
    //Randomize
    private final BooleanValue randomize = new BooleanValue("Randomize", false);
    private final MultiBooleanValue randomizeTime = new MultiBooleanValue("RandomizeTime")
            .add("Always",true)
            .add("Attack",false)
            .add("Movement",false)
            .add("Rotation",false);
    private final ModeValue randomizeMode = new ModeValue("RandomizeMode","Normal",this,
            new StringMode("Normal"),
            new StringMode("Gaussian"),
            new StringMode("Noise")
    ).setVisible(randomize::getValue);
    private final RangeSelectionNumberValue randomizeDelay = new RangeSelectionNumberValue("RandomizeDelay", 0, 50, 0, 1000).setVisible(randomize::getValue);
    private final RangeSelectionNumberValue randomizeYaw = new RangeSelectionNumberValue("RandomizeYaw",0,10,-60,60,0.01)
            .setVisible(randomize::getValue);
    private final RangeSelectionNumberValue randomizePitch = new RangeSelectionNumberValue("RandomizePitch",0,10,-60,60,0.01)
            .setVisible(randomize::getValue);
    private final NumberValue gaussianAmplitude = new NumberValue("GaussianAmplitude", 1, 0, 5, 0.1).setVisible(()->randomizeMode.is("Gaussian"));
    private final NumberValue gaussianMean = new NumberValue("GaussianMean", 0, 0, 5, 0.1).setVisible(()->randomizeMode.is("Gaussian"));
    private final NumberValue gaussianDeviation = new NumberValue("GaussianDeviation", 1, 0, 5, 0.1).setVisible(()->randomizeMode.is("Gaussian"));
    private final NumberValue noiseSigma = new NumberValue("Noise-Sigma", 2.5, 0.0, 10.0, 0.01).setVisible(() -> randomizeMode.is("Noise"));
    //Movement
    private final ModeValue moveFixMode = new ModeValue("MoveFixMode", "None", this,
            new StringMode("None"),
            new StringMode("Strict"),
            new StringMode("Silent"));
    //AutoBlock
    private final ModeValue autoBlockMode = new ModeValue("AutoBlockMode", "None", this,
            new StringMode("HoldKey"),
            new StringMode("AfterTick"),
            new StringMode("Packet"),
            new StringMode("Fake"),
            new StringMode("Matrix"),
            new StringMode("Verus"),
            new StringMode("Attack"),
            new StringMode("None"));
    private final ModeValue blockTiming = new ModeValue("AutoBlockTiming", "Normal", this,
            new StringMode("Normal"),
            new StringMode("Tick"),
            new StringMode("Pre"),
            new StringMode("Post"),
            new StringMode("AfterAttack")
    );
    private final BooleanValue noPacketShielded = new BooleanValue("NoPacketWhileShielding", false).setVisible(() -> autoBlockMode.is("AfterTick"));
    private final BooleanValue onlyWhileKeyBinding = new BooleanValue("OnlyWhileKeyBinding", false);
    private final BooleanValue sendInteractPacket = new BooleanValue("InteractPacket", false);
    
    private final List<EntityLivingBase> targets = new ArrayList<>();
    private final DelayTimer attackTimer = new DelayTimer();
    private final DelayTimer targetTimer = new DelayTimer();
    private final DelayTimer randomizeTimer = new DelayTimer();
    private int randomDelay = 0;
    public EntityLivingBase target = null;
    private int attackDelay = 0;
    private int canAttackTimes = 0;
    private int curRotationDelay = 0;
    private DelayTimer rotationTimer = new DelayTimer();
    @Getter
    public boolean blockingTick = false;
    @Getter
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
        if (boxExpand.getValue()) {
            basicBox = basicBox.expand(expandSizeH.getValue(), expandSizeV.getValue(), expandSizeH.getValue());
        }
        if (predictAimPlayer.getValue()) {
            double horizontal = RandomUtils.randomDouble(horizontalMultiplierPlayer.getFirst(), horizontalMultiplierPlayer.getSecond());
            double vertical = RandomUtils.randomDouble(verticalMultiplierPlayer.getFirst(), verticalMultiplierPlayer.getSecond());
            basicBox = basicBox.offset(
                    mc.player.motionX * horizontal,
                    mc.player.motionY * vertical,
                    mc.player.motionZ * horizontal
            );
        }
        if (predictAimTarget.getValue()) {
            double horizontal = RandomUtils.randomDouble(horizontalMultiplierTarget.getFirst(), horizontalMultiplierTarget.getSecond());
            double vertical = RandomUtils.randomDouble(verticalMultiplierTarget.getFirst(), verticalMultiplierTarget.getSecond());
            basicBox = basicBox.offset(
                    (target.posX - target.lastTickPosX) * horizontal,
                    (target.posY - target.lastTickPosY) * vertical,
                    (target.posZ - target.lastTickPosZ) * horizontal
            );
        }
        
        return basicBox;
    }
    
    public void rotation(EntityLivingBase target) {
        if(target == null) {
            rotationTimer.reset();
            return;
        }
        float horizonSpeed = (float) RandomUtils.randomDouble(yawTurnSpeed.getFirst(), yawTurnSpeed.getSecond());
        float pitchSpeed = (float) RandomUtils.randomDouble(pitchTurnSpeed.getFirst(), pitchTurnSpeed.getSecond());

        int keepTicks = RandomUtils.randomInt((int) Math.round(this.keepTicks.getFirst()), (int) Math.round(this.keepTicks.getSecond()));
        int reverseTicks = RandomUtils.randomInt((int) Math.round(this.backTicks.getFirst()), (int) Math.round(this.backTicks.getSecond()));

        if (!rotationMode.is("None")) {
            Rotation rotation = calculateRotation(target);
            Rotation calculateRot = RotationUtils.smoothRotation(
                    RotationHandler.getRotation(),
                    rotation == null ? new Rotation(mc.player.rotationYaw,mc.player.rotationPitch) : rotation,
                    horizonSpeed,
                    pitchSpeed
            ).fixedSensitivity(0);
            if (silentRotation.getValue()) {
                RotationHandler.setClientRotation(calculateRot, keepTicks, reverseTicks, moveFixMode.getValue().getName());
            } else {
                mc.player.rotationYaw = calculateRot.yaw;
                mc.player.rotationPitch = calculateRot.pitch;
            }
        }
    }

    public Rotation calculateRotation(EntityLivingBase target) {
        
        Vec3d center = null;
        Rotation currentRotation = null;
        AxisAlignedBB targetBox = getTargetBox(target);
        
        switch (rotationMode.getValue().getName()) {
            case "Advance":
                break;
            case "LockCenter":
                center = targetBox.lerpWith(0.5, 0.5, 0.5);
                break;
            case "Lower":
                for (double x = 0.4; x <= 0.6; x += 0.025) {
                    for (double y = 0.2; y <= 0.6; y += 0.1) {
                        for (double z = 0.4; z <= 0.6; z += 0.025) {
                            Vec3d preCenter = targetBox.lerpWith(x, y, z);
                            
                            if (rayCast.getValue() && !rayCastThroughWalls.getValue()) {
                                Rotation rotation = RotationUtils.toRotation(preCenter, mc.player);
                                Entity entity = RayCastUtils.raycastEntity(rotationRange.getValue(), rotation.yaw, rotation.pitch, rayCastThroughWalls.getValue(), (e -> e instanceof EntityLivingBase));
                                if (entity == null || (entity != target && rayCastOnlyTarget.getValue())) continue;
                            }
                            if (CalculateUtils.isVisible(preCenter) || throughWallsAim.getValue()) {
                                if (center == null || RotationUtils.getRotationDifference(RotationUtils.toRotation(preCenter, mc.player), RotationHandler.getRotation()) < RotationUtils.getRotationDifference(RotationUtils.toRotation(center, mc.player), RotationHandler.getRotation())) {
                                    center = preCenter;
                                }
                            }
                        }
                    }
                }
                break;
            case "LockHead":
                for (double x = 0.3; x <= 0.8; x += 0.1) {
                    for (double z = 0.3; z <= 0.8; z += 0.1) {
                        Vec3d preCenter = targetBox.lerpWith(x, 0.9, z);
                        
                        if (rayCast.getValue() && !rayCastThroughWalls.getValue()) {
                            Rotation rotation = RotationUtils.toRotation(preCenter, mc.player);
                            Entity entity = RayCastUtils.raycastEntity(rotationRange.getValue(), rotation.yaw, rotation.pitch, rayCastThroughWalls.getValue(), (e -> e instanceof EntityLivingBase));
                            if (entity == null || (entity != target && rayCastOnlyTarget.getValue())) continue;
                        }
                        if (CalculateUtils.isVisible(preCenter) || throughWallsAim.getValue()) {
                            if (center == null || RotationUtils.getRotationDifference(RotationUtils.toRotation(preCenter, mc.player), RotationHandler.getRotation()) < RotationUtils.getRotationDifference(RotationUtils.toRotation(center, mc.player), RotationHandler.getRotation())) {
                                center = preCenter;
                            }
                        }
                    }
                }
                break;
            case "Test":
                center = CalculateUtils.getClosestPoint(mc.player.getPositionEyes(2.1478F).addVector(0.0,-0.199,0.0),targetBox.offset(
                        -mc.player.motionX-mc.player.posX+mc.player.prevPosX,
                        -mc.player.motionY-mc.player.posY+mc.player.prevPosY,
                        -mc.player.motionZ-mc.player.posZ+mc.player.prevPosZ));
                break;
            case "NearestCenter":
                center = CalculateUtils.getClosestPoint(mc.player.getEyes(), targetBox);
                break;
            case "Normal":
                for (double x = 0.2; x <= 0.8; x += 0.1) {
                    for (double y = 0.2; y <= 0.8; y += 0.1) {
                        for (double z = 0.2; z <= 0.8; z += 0.1) {
                            Vec3d preCenter = targetBox.lerpWith(x, y, z);
                            
                            if (rayCast.getValue()) {
                                Rotation rotation = RotationUtils.toRotation(preCenter, mc.player);
                                Entity entity = RayCastUtils.raycastEntity(rotationRange.getValue(), rotation.yaw, rotation.pitch, rayCastThroughWalls.getValue(), (e -> e instanceof EntityLivingBase));
                                if (entity == null || (entity != target && rayCastOnlyTarget.getValue())) continue;
                            }
                            if (CalculateUtils.isVisible(preCenter) || throughWallsAim.getValue()) {
                                if (center == null || RotationUtils.getRotationDifference(RotationUtils.toRotation(preCenter, mc.player), RotationHandler.getRotation()) < RotationUtils.getRotationDifference(RotationUtils.toRotation(center, mc.player), RotationHandler.getRotation())) {
                                    center = preCenter;
                                }
                            }
                        }
                    }
                }
                break;
            case "Proportion":
                double d = RandomUtils.randomDouble(distancePro.getFirst(),distancePro.getSecond());
                double a = RandomUtils.randomDouble(anglePro.getFirst(),anglePro.getSecond());
                double lastD = 0D;
                double lastA = 0D;
                for (double x = 0.2; x <= 0.8; x += 0.1) {
                    for (double y = 0.2; y <= 0.8; y += 0.1) {
                        for (double z = 0.2; z <= 0.8; z += 0.1) {
                            Vec3d preCenter = targetBox.lerpWith(x, y, z);
                            if (rayCast.getValue()) {
                                Rotation rotation = RotationUtils.toRotation(preCenter, mc.player);
                                Entity entity = RayCastUtils.raycastEntity(rotationRange.getValue(), rotation.yaw, rotation.pitch, rayCastThroughWalls.getValue(), (e -> e instanceof EntityLivingBase));
                                if (entity == null || (entity != target && rayCastOnlyTarget.getValue())) continue;
                            }
                            double distance = mc.player.getEyes().distanceTo(preCenter) * d;
                            double angle = RotationUtils.getRotationDifference(RotationUtils.toRotation(preCenter, mc.player), RotationHandler.getRotation()) * a;
                            if (CalculateUtils.isVisible(preCenter) || throughWallsAim.getValue()) {
                                if (center == null || ((distance + angle) < (lastD + lastA))) {
                                    center = preCenter;
                                    lastD = distance;
                                    lastA = angle;
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
                currentRotation = RotationUtils.findBestRotationSimulatedAnnealing(mc.player, target);
                break;
        }

        if(currentRotation != null) {
            randomizeRotation(currentRotation);
            if(noRotationWhenFaceTarget.getValue()){
                Entity entity = RayCastUtils.raycastEntity(rotationRange.getValue(),RotationHandler.getCurrentRotation().yaw,RotationHandler.getCurrentRotation().pitch, rayCastThroughWalls.getValue(), (e -> e instanceof EntityLivingBase));
                if(entity != null && (!rayCastOnlyTarget.getValue() || entity == target)){
                    currentRotation = RotationHandler.getCurrentRotation();
                }
            }
            if(delayRotation.getValue()){
                boolean canBeSeen = RotationUtils.getRotationDifference(currentRotation,RotationHandler.getCurrentRotation()) <= mc.gameSettings.fovSetting;
                if(!canBeSeen){
                    if(!rotationTimer.hasTimeElapsed(curRotationDelay)){
                        currentRotation = RotationHandler.getCurrentRotation();
                    }else {
                        rotationTimer.reset();
                        curRotationDelay = RandomUtils.randomInt((int) rotationDelay.getFirst(),(int)rotationDelay.getSecond());
                    }
                }else {
                    rotationTimer.reset();
                }
            }
        }
        return currentRotation;
    }


    public void randomizeRotation(Rotation rotation){
        if(!randomize.getValue() || !randomizeTimer.hasTimeElapsed(randomDelay)){
            return;
        }
        AtomicBoolean canRandomize = new AtomicBoolean(false);
        randomizeTime.getValue().forEach(
                (key,value)->{
                    if(value) {
                        switch (key) {
                            case "Always":
                                canRandomize.set(true);
                                break;
                            case "Attack":
                                if(canAttackTimes > 0){
                                    canRandomize.set(true);
                                }
                                break;
                            case "Movement":
                                if(MoveUtils.isMoving()){
                                    canRandomize.set(true);
                                }
                                break;
                            case "Rotation":
                                if(RotationUtils.getRotationDifference(RotationHandler.getCurrentRotation(),RotationHandler.getRotation()) > 3F){
                                    canRandomize.set(true);
                                }
                                break;
                        }
                    }
                }
        );
        if(!canRandomize.get()){
            return;
        }
        float yawBase = (float) RandomUtils.randomDouble(randomizeYaw.getFirst(),randomizeYaw.getSecond());
        float pitchBase = (float) RandomUtils.randomDouble(randomizePitch.getFirst(),randomizePitch.getSecond());
        switch (randomizeMode.getValueByName()){
            case "Normal":
                rotation.yaw += yawBase;
                rotation.pitch = MathHelper.clamp(rotation.pitch+pitchBase, -90, 90);
                break;
            case "Gaussian":
                float yaw = (float) MathUtils.gaussian(yawBase,gaussianAmplitude.getValue(),gaussianMean.getValue(),gaussianDeviation.getValue());
                float pitch = (float) MathUtils.gaussian(pitchBase,gaussianAmplitude.getValue(),gaussianMean.getValue(),gaussianDeviation.getValue());
                rotation.yaw += yaw;
                rotation.pitch = MathHelper.clamp(rotation.pitch+pitch, -90, 90);
                break;
            case "Noise":
                Random random = new Random();
                float yaw1 = (float) (Math.sqrt(noiseSigma.getValue()) * random.nextGaussian()) + yawBase;
                float pitch1 = (float) (Math.sqrt(noiseSigma.getValue()) * random.nextGaussian()) + pitchBase;
                rotation.yaw += yaw1;
                rotation.pitch = MathHelper.clamp(rotation.pitch+pitch1, -90, 90);
                break;
        }
        randomDelay = RandomUtils.randomInt((int) randomizeDelay.getFirst(),(int) randomizeDelay.getSecond());
        randomizeTimer.reset();
    }
    
    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null) return;

        if (!TargetsHandler.canAdd(target)) {
            target = null;
        }

        target = getTarget();
        
        rotation(target);

        if (blockTiming.is("Tick")) {
            runStopBlock();
        }

        if (attackTimeMode.is("Tick")) {
            attackTarget(target);
        }
        
        if (blockTiming.is("Tick")) {
            runAutoBlock(target);
        }
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if (mc.player == null) return;

        if ((blockTiming.is("Pre") && event.isPre())
                || (blockTiming.is("Post") && event.isPost())) {
            runStopBlock();
        }

        if ((attackTimeMode.is("Pre") && event.isPre())
                || (attackTimeMode.is("Post") && event.isPost())) {
            attackTarget(target);
        }

        if ((blockTiming.is("Pre") && event.isPre())
                || (blockTiming.is("Post") && event.isPost())) {
            runAutoBlock(target);
        }
    }
    
    @Override
    public void onEnable() {
        attackDelay = calculateDelay();
        attackTimer.reset();
        targetTimer.reset();
        curRotationDelay = RandomUtils.randomInt((int) rotationDelay.getFirst(),(int)rotationDelay.getSecond());
    }
    
    @Override
    public void onDisable() {
        target = null;
        targets.clear();
        if (blockingStatus && blockingTick) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }
        blockingStatus = false;
        blockingTick = false;
        mc.gameSettings.keyBindUseItem.setPressed(false);
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
    }
    
    private EntityLivingBase getTarget() {
        if (mc.player == null) return null;
        
        if (TargetsHandler.canAdd(target)) {
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
        
        targets.clear();
        targets.addAll(filteredTargets);
        
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
                        RotationUtils.toRotation(getTargetBox(entityLivingBase).getCenter(), mc.player), new Rotation(mc.player.rotationYaw, mc.player.rotationPitch)
                ))));
                break;
            case "Random":
                targets.sort((Comparator.comparingInt(entityLivingBase -> RandomUtils.randomInt(-100, 100))));
                break;
        }
        
        targets.sort((Comparator.comparingDouble(entityLivingBase -> Math.max(0, CalculateUtils.getClosetDistance(mc.player, entityLivingBase) - attackRange.getValue()))));
        
        for (EntityLivingBase entity : targets) {
            if (entity == mc.player || !TargetsHandler.canAdd(entity)) continue;
            targetTimer.reset();
            return entity;
        }
        
        if (blockingTick) {
            PacketUtils.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            mc.gameSettings.keyBindUseItem.setPressed(false);
            blockingStatus = false;
            blockingTick = false;
        }
        
        return null;
    }
    
    private void sendInteractPacket(Entity target) {
        if (sendInteractPacket.getValue()) {
            RayTraceResult raytrace = mc.player.rayTrace(rotationRange.getValue(), 1f);
            if (raytrace != null) {
                mc.playerController.syncCurrentPlayItem();
                Vec3d vec3d = new Vec3d(raytrace.hitVec.xCoord - target.posX, raytrace.hitVec.yCoord - target.posY, raytrace.hitVec.zCoord - target.posZ);
                PacketUtils.sendPacket(new CPacketUseEntity(target, EnumHand.MAIN_HAND, vec3d));
                PacketUtils.sendPacket(new CPacketUseEntity(target, EnumHand.MAIN_HAND, vec3d));
            }
        }
    }
    
    private void blockingPacket(Entity target) {
        sendInteractPacket(target);
        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
    }

    public void runStopBlock(){
        if(canBlock() && !autoBlockMode.is("None")) {
            if (blockingTick) {
                switch (autoBlockMode.getValue().getName()) {
                    case "Verus":
                        PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 8 + 1));
                        PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                        blockingTick = false;
                        break;
                    case "Attack":
                        if(canAttackTimes > 0){
                            PacketUtils.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                            blockingTick = false;
                        }
                        break;

                    case "HoldKey":
                    case "Packet":
                    case "AfterTick":
                    case "Matrix":
                        break;

                    default:
                        PacketUtils.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        blockingTick = false;
                        break;
                }
            }
        }
    }

    private void runAutoBlock(Entity target) {
        if (target == null) return;
        if (autoBlockMode.is("None")) return;
        if(!canBlock()) {
            mc.gameSettings.keyBindUseItem.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem));
            return;
        }
        if(autoBlockMode.is("AfterTick")){
            if (canAttackTimes > 0) {
                if (blockingTick) {
                    PacketUtils.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    blockingTick = false;
                }
            } else {
                if (!onlyWhileKeyBinding.getValue() || GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)) {
                    if (!noPacketShielded.getValue()) {
                        blockingPacket(target);
                    }
                    if (!blockingTick) {
                        if (noPacketShielded.getValue()) {
                            blockingPacket(target);
                        }
                        blockingTick = true;
                    }
                }
            }
        }

        if (onlyWhileKeyBinding.getValue() && !GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)) {
            return;
        }
        switch (autoBlockMode.getValueByName()) {
            case "Fake":
            case "AfterTick":
                break;
            case "HoldKey":
                mc.gameSettings.keyBindUseItem.setPressed(true);
                blockingTick = true;
                break;
            case "Packet":
                if (!blockingStatus) {
                    blockingPacket(target);
                    blockingTick = true;
                }
                break;
            case "Matrix":
                mc.rightClickMouse();
                blockingTick = true;
                break;
            case "Verus":
            case "Attack":
                if (!blockingTick) {
                    blockingPacket(target);
                    blockingTick = true;
                }
                break;
        }
        blockingStatus = true;
    }
    
    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof CPacketUseEntity && ((CPacketUseEntity) packet).getAction() == CPacketUseEntity.Action.ATTACK) {
            if (autoBlockMode.is("Matrix") && canBlock() && blockingTick) {
                event.setCancelled(true);
                PacketUtils.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                PacketUtils.sendPacket(packet, false);
                blockingTick = false;
                blockingStatus = false;
                runAutoBlock(target);
            }
        }
    }
    
    private void attackTarget(EntityLivingBase target) {
        
        if (mc.player == null || target == null) return;
        
        if ((noInventory.getValue() && mc.currentScreen instanceof GuiInventory) || (noGui.getValue() && mc.currentScreen != null)) {
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
        
        if (bestTarget == null || (bestTarget != target && rayCastOnlyTarget.getValue())) return;
        
        if (!PlayerUtils.canBeSeenEntity(mc.player, bestTarget) && CalculateUtils.getClosetDistance(mc.player, (EntityLivingBase) bestTarget) > throughWallAttackRange.getValue()) {
            return;
        }
        
        if (((EntityLivingBase) bestTarget).hurtTime > hurtTime.getValue()) return;

        if (blockTiming.is("Normal")) {
            runAutoBlock(target);
            runStopBlock();
        }

        if(blockTiming.is("AfterAttack")){
            runStopBlock();
        }

        while (canAttackTimes > 0) {
            canAttackTimes--;
            if (CalculateUtils.getClosetDistance(mc.player, (EntityLivingBase) bestTarget) <= attackRange.getValue()) {
                if (attackMode.is("Packet")) {
                    if (!ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)) {
                        swing();
                    }
                    
                    AttackEvent event = new AttackEvent(target);
                    Client.INSTANCE.getEventManager().call(event);
                    if (event.isCancelled()) return;

                    PacketUtils.sendPacket(new CPacketUseEntity(bestTarget));
                    if (ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)) {
                        swing();
                    }
                    if (!keepSprintMode.is("Always")) {
                        if (keepSprintMode.is("None") || mc.player.hurtTime > 0) {
                            mc.player.attackTargetEntityWithCurrentItem(target);
                        }
                    }
                } else if (attackMode.is("Legit")) {
                    if (blockTiming.is("Normal")) {
                        runStopBlock();
                    }
                    mc.clickMouse();
                }
            } else {
                if (CalculateUtils.getClosetDistance(mc.player, (EntityLivingBase) bestTarget) <= swingRange.getValue()) {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                }
            }
            if (noDoubleHit.is("NextHit")) {
                break;
            }
        }

        if(blockTiming.is("AfterAttack")){
            runAutoBlock(bestTarget);
        }
    }
    
    private void swing() {
        switch (swingMode.getValueByName()) {
            case "Vanilla":
                mc.player.swingArm(EnumHand.MAIN_HAND);
                break;
            case "Packet":
                PacketUtils.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND), true);
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
                return (int) (1000 / (Math.sqrt(gaussianSigma.getValue()) * random.nextGaussian() + (float) cps));
            case "Jitter":
                double jitterAmount = basicDelay * RandomUtils.randomDouble(jitterPercent.getFirst(), jitterPercent.getSecond());
                
                double jitter = (random.nextDouble() * 2 - 1) * jitterAmount;
                return (int) (basicDelay + (long) jitter);
        }
        return 200;
    }
    
    private boolean canBlock() {
        if (noInventory.getValue() && mc.currentScreen instanceof GuiInventory) {
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
