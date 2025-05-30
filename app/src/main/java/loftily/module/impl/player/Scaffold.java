package loftily.module.impl.player;

import com.google.common.collect.Lists;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.handlers.impl.RotationHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.block.BlockUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.math.Rotation;
import loftily.utils.player.RotationUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static loftily.utils.math.CalculateUtils.getVectorForRotation;

@ModuleInfo(name = "Scaffold", category = ModuleCategory.Player)
public class Scaffold extends Module {

    private final ModeValue placeTiming = new ModeValue("PlaceTiming","Tick",this,
            new StringMode("Pre"),
            new StringMode("Post"),
            new StringMode("Tick"));
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
    private final ModeValue clickCheck = new ModeValue("ClickCheck","Strict",this,
            new StringMode("None"),
            new StringMode("Simple"),
            new StringMode("Strict"));
    private final BooleanValue keepRotation = new BooleanValue("KeepRotation",false);
    private final RangeSelectionNumberValue yawTurnSpeed = new RangeSelectionNumberValue("YawTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue pitchTurnSpeed = new RangeSelectionNumberValue("PitchTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue keepTicks = new RangeSelectionNumberValue("KeepRotationTicks", 1, 2, 0, 20);
    private final RangeSelectionNumberValue backTicks = new RangeSelectionNumberValue("ReverseTicks", 1, 2, 0, 20);
    private final BooleanValue silentRotation = new BooleanValue("SilentRotation", false);
    private final ModeValue moveFixMode = new ModeValue("MoveFixMode", "None", this,
            new StringMode("None"),
            new StringMode("Silent"),
            new StringMode("45Angle"))
    ;

    @Override
    public void onDisable(){
        placeInfo = null;
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

    private BlockPos getOptimalPos(){
        BlockPos playerPos;
        if (mc.player.posY == Math.round(mc.player.posY) + 0.5) {
            playerPos = new BlockPos((mc.player.posX),(mc.player.posY),(mc.player.posZ));
        } else {
            playerPos = new BlockPos((mc.player.posX),(mc.player.posY),(mc.player.posZ)).down();
        }
        return playerPos;
    }


    private void searchBlock() {
        BlockPos playerPos = getOptimalPos();

        IBlockState iBlockState = mc.world.getBlockState(playerPos);

        if (!iBlockState.getMaterial().isReplaceable()){
            return;
        }
        if ((!iBlockState.getMaterial().isReplaceable() ||
                calculateCenter(playerPos,true))) {
            return;
        }
        Iterable<BlockPos> blockPosIterable = BlockPos.getAllInBox(
                playerPos.add(-4, 0, -4), playerPos.add(4, -2, 4)
        );
        List<BlockPos> blocks = Lists.newArrayList(blockPosIterable);

        blocks.sort(Comparator.comparingDouble(b -> mc.player.getDistance(b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5)));
        for (BlockPos blockPos : blocks) {
            if (calculateCenter(blockPos, true)) {
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

    private PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> getPlaceInfo(BlockPos blockPos,Vec3d vec3,EnumFacing enumFacing,Boolean raycast) {
        Vec3d eyes = mc.player.getEyes();
        Vec3d center;
        Vec3d blockVec = new Vec3d(blockPos);
        double reach = mc.playerController.getBlockReachDistance();

        Rotation rotation;
        PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> dPlaceInfo = null;
        Vec3d directionVec = new Vec3d(enumFacing.getDirectionVec());
        BlockPos placePos = blockPos.offset(enumFacing);

        center = (blockVec.addVector(vec3.xCoord, vec3.yCoord, vec3.zCoord)).addVector(
                directionVec.xCoord * vec3.xCoord, directionVec.yCoord * vec3.yCoord, directionVec.zCoord * vec3.zCoord
        );

        double distance = eyes.distanceTo(center);
        if (distance > reach || mc.world.rayTraceBlocks(eyes,center,false,true,false) != null) {
            return null;
        }
        Vec3d vec = center.subtract(eyes);
        if(enumFacing.getAxis() != EnumFacing.Axis.Y){
            double dist = abs(enumFacing.getAxis() == EnumFacing.Axis.X ? vec.xCoord : vec.zCoord);
            if(dist < distValue.getValue()){
                return null;
            }
        }

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
            dPlaceInfo = new PlaceInfo<>(blockPos, enumFacing, center, rotation);
        } else {
            RayTraceResult serverRayTrace = performBlockRaytrace(RotationHandler.getCurrentRotation());
            if (serverRayTrace.getBlockPos().equals(placePos) && (!raycast || serverRayTrace.sideHit == enumFacing.getOpposite())) {
                dPlaceInfo = new PlaceInfo<>(blockPos, enumFacing, center, rotation);
            }
            RayTraceResult rotationRayTrace = performBlockRaytrace(rotation);
            if (rotationRayTrace.getBlockPos().equals(placePos) || (!raycast || rotationRayTrace.sideHit == enumFacing.getOpposite())) {
                dPlaceInfo = new PlaceInfo<>(blockPos, enumFacing, center, rotation);
            }
        }
        return dPlaceInfo;
    }

    private PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> compareDifferences(
            PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> newPlaceRotation, PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> oldPlaceRotation, Rotation rotation) {
        if (oldPlaceRotation == null || RotationUtils.getRotationDifference(newPlaceRotation.getRotation(), rotation) <
                RotationUtils.getRotationDifference(oldPlaceRotation.getRotation(), rotation)) {
            return newPlaceRotation;
        }

        return oldPlaceRotation;
    }

    private Boolean calculateCenter(BlockPos blockPos,Boolean raycast){
        if (!mc.world.getBlockState(blockPos).getBlock().blockMaterial.isReplaceable()) {
            return false;
        }

        PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> dPlaceInfo = null;
        PlaceInfo<BlockPos,EnumFacing,Vec3d,Rotation> ePlaceInfo;

        for (EnumFacing enumFacing : EnumFacing.values()) {
            BlockPos placePos = blockPos.offset(enumFacing);
            if (!BlockUtils.canBeClick(placePos)) continue;
            for (double x = 0.1; x <= 0.9; x += 0.1) {
                for (double y = 0.1; y <= 0.9; y += 0.1) {
                    for (double z = 0.1; z <= 0.9; z += 0.1) {
                        ePlaceInfo = getPlaceInfo(blockPos, new Vec3d(x,y,z), enumFacing, raycast);
                        if(ePlaceInfo == null) {
                            continue;
                        }
                        dPlaceInfo = compareDifferences(ePlaceInfo,dPlaceInfo,RotationHandler.getCurrentRotation());
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
        );
        if (silentRotation.getValue()) {
            RotationHandler.setClientRotation(calculateRot, keepTicks, reverseTicks,moveFixMode.getValue().getName());
        } else {
            mc.player.rotationYaw = calculateRot.yaw;
            mc.player.rotationPitch = calculateRot.pitch;
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

        click(placeInfo.blockPos,placeInfo.facing,placeInfo.hitVec);

        if(keepRotation.getValue() && RotationHandler.clientRotation != null){
            setRotation(RotationHandler.clientRotation);
        }
    }

    @EventHandler
    public void onMotion(MotionEvent event) {

        if (placeInfo != null) {
            if((event.isPre() && placeTiming.is("Pre")) || (event.isPost() && placeTiming.is("Post"))) {
                click(placeInfo.blockPos, placeInfo.facing, placeInfo.hitVec);
            }
        }
    }

    private void click(BlockPos placePos, EnumFacing facing, Vec3d hitVec) {
        EnumHand hand = mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;

        IBlockState iBlockState = mc.world.getBlockState(placePos);

        if(!clickCheck.is("None")){
            RayTraceResult rayTraceResult = performBlockRaytrace(RotationHandler.getCurrentRotation());
            if(rayTraceResult == null || !rayTraceResult.getBlockPos().equals(placePos.offset(facing)) || (rayTraceResult.sideHit != facing.getOpposite() && clickCheck.is("Strict"))){
                return;
            }
        }

        if (!(mc.player.getHeldItem(hand).getItem() instanceof ItemBlock)) return;
        if(!iBlockState.getBlock().blockMaterial.isReplaceable()) { return; }

        mc.playerController.processRightClickBlock(mc.player, mc.world, placePos, facing, hitVec, hand);
        mc.player.swingArm(hand);
    }
}
