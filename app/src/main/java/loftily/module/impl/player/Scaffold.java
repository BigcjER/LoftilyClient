package loftily.module.impl.player;

import com.google.common.collect.Lists;
import loftily.Client;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.player.motion.MoveEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.handlers.impl.MoveHandler;
import loftily.handlers.impl.RotationHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.module.impl.other.RayTraceFixer;
import loftily.utils.block.BlockUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.math.Rotation;
import loftily.utils.player.MoveUtils;
import loftily.utils.player.RotationUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.abs;
import static loftily.utils.math.CalculateUtils.getMoveFixForward;
import static loftily.utils.math.CalculateUtils.getVectorForRotation;

@ModuleInfo(name = "Scaffold", category = ModuleCategory.PLAYER)
public class Scaffold extends Module {

    //Mode
    private final ModeValue scaffoldMode = new ModeValue("Mode","Normal",this,
            new StringMode("Normal"),
            new StringMode("Telly"),
            new StringMode("Snap"),
            new StringMode("AutoJump"));
    //Place
    private final ModeValue placeTiming = new ModeValue("PlaceTiming","Tick",this,
            new StringMode("Pre"),
            new StringMode("Post"),
            new StringMode("Tick"));
    private final ModeValue clickCheck = new ModeValue("ClickCheck","Strict",this,
            new StringMode("None"),
            new StringMode("Simple"),
            new StringMode("Strict"));
    private final ModeValue placeDelayMode = new ModeValue("PlaceDelayMode","None",this,
            new StringMode("None"),
            new StringMode("Normal")
    );
    private final RangeSelectionNumberValue placeDelay = new RangeSelectionNumberValue("PlaceDelay",40,100,0,1000)
            .setVisible(()->placeDelayMode.is("Normal"));
    //Rotation
    private final ModeValue rotationMode = new ModeValue("RotationMode","None",this,
            new StringMode("Normal"),
            new StringMode("Round45") ,
            new StringMode("Sexy"),
            new StringMode("None")
    );
    private final ModeValue rotationTiming = new ModeValue("RotationTiming","Normal",this,
            new StringMode("Normal"),new StringMode("Always")
    );
    private final NumberValue distValue = new NumberValue("Dist",0.0,0.0,0.3,0.01);
    private final BooleanValue rayCastSearch = new BooleanValue("RayCast",false);
    private final BooleanValue keepRotation = new BooleanValue("KeepRotation",false);
    private final RangeSelectionNumberValue yawTurnSpeed = new RangeSelectionNumberValue("YawTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue pitchTurnSpeed = new RangeSelectionNumberValue("PitchTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue keepTicks = new RangeSelectionNumberValue("KeepRotationTicks", 1, 2, 0, 20);
    private final RangeSelectionNumberValue backTicks = new RangeSelectionNumberValue("ReverseTicks", 1, 2, 0, 20);
    private final BooleanValue silentRotation = new BooleanValue("SilentRotation", false);
    //Movement
    private final ModeValue eagleMode = new ModeValue("EagleMode", "None", this,
            new StringMode("None"),
            new StringMode("Prediction")
    );

    private final NumberValue sneakTime = new NumberValue("SneakTime", 50, 0, 500, 1).setVisible(() -> eagleMode.is("Prediction"));
    private final NumberValue predictMotion = new NumberValue("Predict", 0, -1.0, 1.0, 0.01).setVisible(() -> eagleMode.is("Prediction"));

    private final ModeValue moveFixMode = new ModeValue("MoveFixMode", "None", this,
            new StringMode("None"),
            new StringMode("Silent"),
            new StringMode("45Angle"))
    ;
    private final ModeValue sprintMode = new ModeValue("SprintMode", "Default", this,
            new StringMode("Default"),
            new StringMode("Legit"),
            new StringMode("OFF")
    );
    private final ModeValue towerMode = new ModeValue("TowerMode", "Jump", this,
            new StringMode("Matrix"),
            new StringMode("Vanilla"),
            new StringMode("Jump")
    );
    private final BooleanValue towerFakeJump = new BooleanValue("TowerFakeJump", false);
    private final BooleanValue towerNoMove = new BooleanValue("TowerNoMove", false);

    private final BooleanValue onGround = new BooleanValue("OnGroundSafeWalk",false);
    private final BooleanValue inAir = new BooleanValue("InAirSafeWalk",false);

    //SameY
    private final BooleanValue sameY = new BooleanValue("SameY", false);
    private final BooleanValue allowJump = new BooleanValue("AllowJump", false);

    private int onGroundTimes = 0;
    private double lastY = 0.0;
    private boolean towerStatus = false;
    private DelayTimer placeTimer = new DelayTimer();
    private int currentPlaceDelay = 0;

    @Override
    public void onDisable(){
        placeTimer.reset();
        currentPlaceDelay = RandomUtils.randomInt((int)placeDelay.getFirst(),(int)placeDelay.getSecond());
        placeInfo = null;
        onGroundTimes = 0;
    }

    @Override
    public void onEnable() {
        lastY = mc.player.posY;
    }

    private PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> placeInfo = null;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PlaceInfo<placePos,enumFacing,center,rotation>{
        public BlockPos blockPos;
        public EnumFacing facing;
        public Vec3d hitVec;
        public Rotation rotation;
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if(event.getEntity() != mc.player)return;
        if((onGround.getValue() && mc.player.onGround) || (inAir.getValue() && !mc.player.onGround)) {
            event.setSafeWalk(true);
        }
    }

    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.player == null) return;

        searchBlock();

        if(placeInfo == null)
            return;

        if (rotationTiming.getValueByName().equals("Always")) {
            setRotation(placeInfo.getRotation());
        }
        if(rotationMode.is("Sexy")) {
            setRotation(RotationUtils.toRotation(placeInfo.getHitVec(),mc.player));
        }

        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) && !(mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock)) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack.getItem() instanceof ItemBlock) {
                    mc.player.inventory.currentItem = i;
                    break;
                }
            }
            return;
        }

        if(keepRotation.getValue() && RotationHandler.clientRotation != null){
            setRotation(RotationHandler.clientRotation);
        }

        switch (scaffoldMode.getValueByName()){
            case "Telly":
                if(MoveUtils.isMoving() && mc.player.onGround){
                    setRotation(new Rotation((float) (MoveUtils.getDirection() * 180 / Math.PI), RotationHandler.getCurrentRotation().pitch));
                }
                break;
            case "Snap":
                if(!(mc.world.getBlockState(new BlockPos(mc.player.posX,mc.player.posY-1,mc.player.posZ)).getBlock() instanceof BlockAir)){
                    setRotation(new Rotation((float) (MoveUtils.getDirection() * 180 / Math.PI), RotationHandler.getCurrentRotation().pitch));
                }
                break;
        }

        if (eagleMode.is("Prediction")) {
            IBlockState iBlockState = mc.world.getBlockState(new BlockPos(mc.player.posX + mc.player.motionX * predictMotion.getValue(),
                    mc.player.posY - 1.0,
                    mc.player.posZ + mc.player.motionZ * predictMotion.getValue()));

            if (iBlockState.getBlock() instanceof BlockAir && mc.player.onGround) {
                MoveHandler.setSneak(true, sneakTime.getValue().intValue());
            }
        }

        click(placeInfo.blockPos,placeInfo.facing,placeInfo.hitVec);
    }

    @EventHandler
    public void onStrafe(StrafeEvent event){
        switch (scaffoldMode.getValueByName()){
            case "Telly":
                if(MoveUtils.isMoving()) {
                    if (!mc.player.isSprinting() && mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.gameSettings.keyBindJump.setPressed(false);
                    }
                    if (mc.player.isSprinting()) {
                        if (mc.player.onGround && MoveUtils.isMoving()) {
                            mc.player.tryJump();
                        }
                        mc.gameSettings.keyBindJump.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindJump));
                    }
                }else {
                    mc.gameSettings.keyBindJump.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindJump));
                }
                break;
            case "AutoJump":
                if(mc.player.onGround && MoveUtils.isMoving()){
                    mc.player.tryJump();
                }
                break;
            case "Normal":
            case "Snap":
                break;
        }
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        towerStatus = false;
        towerStatus = mc.gameSettings.keyBindJump.isKeyDown();

        if (placeInfo != null) {
            if((event.isPre() && placeTiming.is("Pre")) || (event.isPost() && placeTiming.is("Post"))) {
                click(placeInfo.blockPos, placeInfo.facing, placeInfo.hitVec);
            }
        }

        if (towerStatus) {
            if (allowJump.getValue()) {
                lastY = mc.player.posY;
            }
            if (towerNoMove.getValue() && MoveUtils.isMoving()) return;
            switch (towerMode.getValueByName()) {
                case "Jump":
                    break;
                case "Vanilla":
                    fakeJump();
                    mc.player.motionY = 0.42;
                    break;
                case "Matrix":
                    if (!mc.player.onGround && onGroundTimes == 0) {
                        onGroundTimes = 1;
                    }
                    if (mc.player.onGround && onGroundTimes == 1) {
                        onGroundTimes = 2;
                    }
                    if (onGroundTimes == 2) {
                        if (mc.player.onGround) {
                            fakeJump();
                            mc.player.motionY = 0.42;
                        } else if (mc.player.motionY < 0.19) {
                            event.setOnGround(true);
                            mc.player.motionY = 0.42;
                        }
                    }
                    break;
            }
        } else {
            onGroundTimes = 0;
        }
    }

    private void fakeJump() {
        if (!towerFakeJump.getValue()) {
            return;
        }
        mc.player.isAirBorne = true;
        mc.player.addStat(StatList.JUMP);
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (towerStatus && towerFakeJump.getValue()) {
            if (towerNoMove.getValue() && MoveUtils.isMoving()) return;
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = -99999)
    public void onSprint(LivingUpdateEvent event) {
        switch (sprintMode.getValueByName()) {
            case "Default":
                break;
            case "OFF":
                mc.player.setSprinting(false);
                mc.gameSettings.keyBindSprint.setPressed(false);
                break;
            case "Legit":
                if (RotationHandler.clientRotation != null) {
                    if (getMoveFixForward(RotationHandler.clientRotation) < 0.8) {
                        mc.player.setSprinting(false);
                    }
                }
                break;
        }
    }

    private BlockPos getOptimalPos(){
        BlockPos playerPos;
        if (mc.player.posY == Math.round(mc.player.posY) + 0.5) {
            playerPos = new BlockPos((mc.player.posX),(mc.player.posY),(mc.player.posZ));
        } else {
            playerPos = new BlockPos((mc.player.posX),(mc.player.posY),(mc.player.posZ)).down();
        }
        if (sameY.getValue()) {
            if ((int) mc.player.posY - 1.2 > (int) lastY) {
                lastY = (int) mc.player.posY;
            }
            playerPos = new BlockPos((mc.player.posX), lastY, (mc.player.posZ)).down();
        }
        return playerPos;
    }


    private void searchBlock() {
        BlockPos playerPos = getOptimalPos();

        IBlockState iBlockState = mc.world.getBlockState(playerPos);

        if (!iBlockState.getMaterial().isReplaceable()){
            return;
        }
        if (!iBlockState.getMaterial().isReplaceable() ||
                calculateCenter(playerPos,true)) {
            return;
        }
        Iterable<BlockPos> blockPosIterable = BlockPos.getAllInBox(
                playerPos.add(-5, 0, -5), playerPos.add(5, -3, 5)
        );
        List<BlockPos> blocks = Lists.newArrayList(blockPosIterable);

        blocks.sort(Comparator.comparingDouble(b -> mc.player.getDistance(b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5)));
        for (BlockPos blockPos : blocks) {
            if (BlockUtils.canBeClick(blockPos) || calculateCenter(blockPos, true)) {
                return;
            }
        }
    }

    private RayTraceResult performBlockRaytrace(Rotation rotation) {
        EntityPlayer player = mc.player;
        World world = mc.world;
        double maxReach = mc.playerController.getBlockReachDistance();

        Vec3d eyes = player.getEyes();
        Vec3d rotationVec = getVectorForRotation(rotation);

        Vec3d reach = eyes.add(new Vec3d(rotationVec.xCoord * maxReach,rotationVec.yCoord * maxReach,rotationVec.zCoord * maxReach));

        return world.rayTraceBlocks(eyes, reach, false, false, true);
    }

    private PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> getPlaceInfo(BlockPos blockPos,BlockPos placePos,Vec3d vec3,EnumFacing enumFacing,Boolean raycast) {
        Vec3d eyes = mc.player.getEyes();
        Vec3d center;
        Vec3d blockVec = new Vec3d(blockPos);

        Rotation rotation;
        PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> dPlaceInfo = null;
        Vec3d directionVec = new Vec3d(enumFacing.getDirectionVec());

        center = (blockVec.addVector(vec3.xCoord, vec3.yCoord, vec3.zCoord)).addVector(
                directionVec.xCoord * vec3.xCoord, directionVec.yCoord * vec3.yCoord, directionVec.zCoord * vec3.zCoord
        );
        double reach = mc.playerController.getBlockReachDistance();
        double distance = eyes.distanceTo(center);
        if (distance > reach || mc.world.rayTraceBlocks(eyes,center,false,true,false) != null) {
            return null;
        }
        Vec3d vec = center.subtract(eyes);
        if(enumFacing.getAxis() != EnumFacing.Axis.Y){
            double dist = abs(enumFacing.getAxis() == EnumFacing.Axis.X ? vec.xCoord : vec.zCoord);
            if(dist < distValue.getValue() && !mc.player.movementInput.sneak){
                return null;
            }
        }
        Rotation basicRotation = RotationUtils.toRotation(center,mc.player);
        rotation = RotationUtils.toRotation(center,mc.player);

        switch (rotationMode.getValueByName()) {
            case "None":
            case "Normal":
                break;
            case "Round45":
                rotation = new Rotation(
                        Math.round(rotation.yaw / 45f) * 45f,
                        rotation.pitch
                );
                break;
        }

        if (!rayCastSearch.getValue()) {
            dPlaceInfo = new PlaceInfo<>(placePos, enumFacing, center, rotation);
        } else {
            RayTraceResult serverRayTrace = performBlockRaytrace(RotationHandler.getCurrentRotation());
            if (serverRayTrace.getBlockPos().equals(placePos) && (!raycast || serverRayTrace.sideHit == enumFacing)) {
                dPlaceInfo = new PlaceInfo<>(placePos, enumFacing, center, rotation);
            }
            RayTraceResult rotationRayTrace = performBlockRaytrace(basicRotation);
            if (rotationRayTrace.getBlockPos().equals(placePos) || (!raycast || rotationRayTrace.sideHit == enumFacing)) {
                dPlaceInfo = new PlaceInfo<>(placePos, enumFacing, center, rotation);
            }
        }

        return dPlaceInfo;
    }

    private PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> compareDifferences(
            PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> newPlaceRotation, PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> oldPlaceRotation, Rotation rotation) {
        if (oldPlaceRotation == null || RotationUtils.getRotationDifference(newPlaceRotation.getRotation(), rotation) <
                RotationUtils.getRotationDifference(oldPlaceRotation.getRotation(), rotation)
        ) {
            return newPlaceRotation;
        }

        return oldPlaceRotation;
    }

    private Boolean calculateCenter(BlockPos blockPos,Boolean raycast){

        if (!Objects.requireNonNull(blockPos.getState()).getBlock().blockMaterial.isReplaceable()) {
            return false;
        }

        PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> dPlaceInfo = null;
        PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> ePlaceInfo;

        for (EnumFacing enumFacing : EnumFacing.values()) {
            BlockPos placePos = blockPos.offset(enumFacing);
            if (!BlockUtils.canBeClick(placePos)) continue;
            for (double x = 0.3; x <= 0.7; x += 0.1) {
                for (double y = 0.3; y <= 0.7; y += 0.1) {
                    for (double z = 0.3; z <= 0.7; z += 0.1) {
                        ePlaceInfo = getPlaceInfo(blockPos,placePos,new Vec3d(x,y,z), enumFacing, raycast);
                        if(ePlaceInfo == null) {
                            continue;
                        }
                        dPlaceInfo = compareDifferences(ePlaceInfo,dPlaceInfo,RotationHandler.getRotation());
                    }
                }
            }
        }

        if(dPlaceInfo == null)return false;

        this.placeInfo = dPlaceInfo;
        if(rotationTiming.is("Normal")) {
            setRotation(placeInfo.rotation);
        }
        return true;
    }

    private void setRotation(Rotation rotation){
        if(rotationMode.is("None"))return;

        float horizonSpeed = (float) RandomUtils.randomDouble(yawTurnSpeed.getFirst(), yawTurnSpeed.getSecond());
        float pitchSpeed = (float) RandomUtils.randomDouble(pitchTurnSpeed.getFirst(), pitchTurnSpeed.getSecond());

        int keepTicks = RandomUtils.randomInt((int) Math.round(this.keepTicks.getFirst()), (int) Math.round(this.keepTicks.getSecond()));
        int reverseTicks = RandomUtils.randomInt((int) Math.round(this.backTicks.getFirst()), (int) Math.round(this.backTicks.getSecond()));

        Rotation calculateRot = RotationUtils.smoothRotation(
                RotationHandler.getRotation(),
                rotation,
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

    private void click(BlockPos placePos, EnumFacing facing, Vec3d hitVec) {
        EnumHand hand = mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;

        ItemStack heldItem = mc.player.getHeldItem(hand);

        if (!(heldItem.getItem() instanceof ItemBlock)) {
            return;
        }

        if (!((ItemBlock) heldItem.getItem()).canPlaceBlockOnSide(
                mc.world,
                placePos,
                facing.getOpposite(),
                mc.player,
                heldItem
        )
        ) {
            placeTimer.reset();
            return;
        }
        if(!Objects.requireNonNull(placePos.getState()).getBlock().canCollideCheck(placePos.getState(), false)){
            placeTimer.reset();
            return;
        }


        if(!clickCheck.is("None")){
            RayTraceResult rayTraceResult = performBlockRaytrace(RotationHandler.getCurrentRotation());
            if(rayTraceResult == null || !rayTraceResult.getBlockPos().equals(placePos) || (rayTraceResult.sideHit != facing.getOpposite() && clickCheck.is("Strict"))){
                return;
            }
        }

        if(placeDelayMode.is("Normal")){
            if(!placeTimer.hasTimeElapsed(currentPlaceDelay)){
                return;
            }
        }

        if(mc.playerController.processRightClickBlock(mc.player, mc.world, placePos, facing.getOpposite(), hitVec, hand) == EnumActionResult.SUCCESS) {
            mc.player.swingArm(hand);
            placeTimer.reset();
            currentPlaceDelay = RandomUtils.randomInt((int)placeDelay.getFirst(),(int)placeDelay.getSecond());
        }
    }
}
