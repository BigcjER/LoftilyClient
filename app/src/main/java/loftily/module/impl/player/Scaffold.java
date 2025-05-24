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
import net.minecraft.block.BlockAir;
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
            new StringMode("Normal"),new StringMode("Round45") , new StringMode("None")
    );
    private final ModeValue rotationTiming = new ModeValue("RotationTiming","Normal",this,
            new StringMode("Normal"),new StringMode("Bug")
    );
    private final NumberValue distValue = new NumberValue("Dist",0.0,0.0,0.3,0.01);
    private final BooleanValue keepRotation = new BooleanValue("KeepRotation",false);
    private final BooleanValue rayCast = new BooleanValue("RayCast",false);
    private final RangeSelectionNumberValue yawTurnSpeed = new RangeSelectionNumberValue("YawTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue pitchTurnSpeed = new RangeSelectionNumberValue("PitchTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue keepTicks = new RangeSelectionNumberValue("KeepRotationTicks", 1, 2, 0, 20);
    private final RangeSelectionNumberValue backTicks = new RangeSelectionNumberValue("ReverseTicks", 1, 2, 0, 20);
    private final BooleanValue silentRotation = new BooleanValue("SilentRotation", false);
    private final ModeValue moveFixMode = new ModeValue("MoveFixMode", "None", this,
            new StringMode("None"),
            new StringMode("Strict"),
            new StringMode("Silent"),
            new StringMode("45Angle"))
    ;

    @Override
    public void onDisable(){
        placeInfo = null;
    }

    private PlaceInfo<BlockPos,EnumFacing,Vec3d> placeInfo = null;

    @Getter
    @Setter
    @AllArgsConstructor
    public class PlaceInfo<placePos,enumFacing,center>{
        public BlockPos blockPos;
        public EnumFacing facing;
        public Vec3d hitVec;
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
        Iterable<BlockPos> blockPosIterable = BlockPos.getAllInBox(
                playerPos.add(-4, 0, -4), playerPos.add(4, -2, 4)
        );
        List<BlockPos> blocks = Lists.newArrayList(blockPosIterable);

        blocks.sort(Comparator.comparingDouble(b -> mc.player.getDistance(b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5)));
        for (BlockPos blockPos : blocks) {
            if (!mc.world.getBlockState(blockPos).getBlock().blockMaterial.isReplaceable()) {
                continue;
            }
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

    private Boolean calculateCenter(BlockPos blockPos,Boolean raycast){
        if(!mc.world.getBlockState(blockPos).getMaterial().isReplaceable()){
            return false;
        }

        Vec3d eyes = mc.player.getEyes();
        Vec3d center;
        Vec3d blockVec = new Vec3d(blockPos);
        double reach = mc.playerController.getBlockReachDistance();

        Rotation rotation = null;
        Rotation lastRotation;

        PlaceInfo<BlockPos,EnumFacing,Vec3d> dPlaceInfo = null;

        for (EnumFacing enumFacing : EnumFacing.values()) {
            BlockPos placePos = blockPos.offset(enumFacing);
            if (!BlockUtils.canBeClick(placePos) || mc.world.getBlockState(placePos).getBlock() instanceof BlockAir) continue;
            Vec3d directionVec = new Vec3d(enumFacing.getDirectionVec());
            for (double x = 0.1; x <= 0.9; x += 0.1) {
                for (double y = 0.1; y <= 0.9; y += 0.1) {
                    for (double z = 0.1; z <= 0.9; z += 0.1) {
                        center = (blockVec.addVector(x, y, z)).addVector(
                                directionVec.xCoord * x, directionVec.yCoord * y, directionVec.zCoord * z
                        );

                        double distance = eyes.distanceTo(center);
                        if (distance > reach || mc.world.rayTraceBlocks(eyes,center,false,true,false) != null) {
                            continue;
                        }
                        Vec3d vec = center.subtract(eyes);
                        if(enumFacing.getAxis() != EnumFacing.Axis.Y){
                            double dist = abs(enumFacing.getAxis() == EnumFacing.Axis.X ? vec.xCoord : vec.zCoord);
                            if(dist < distValue.getValue()){
                                continue;
                            }
                        }
                        lastRotation = RotationUtils.toRotation(center,mc.player);

                        if(rotation == null || RotationUtils.getRotationDifference(lastRotation,RotationHandler.getRotation()) <
                        RotationUtils.getRotationDifference(rotation,RotationHandler.getRotation())) {
                            rotation = RotationUtils.toRotation(center, mc.player);
                        }

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

                        if (!rayCast.getValue()) {
                            dPlaceInfo = new PlaceInfo<>(blockPos, enumFacing, center);
                            break;
                        } else {
                            RayTraceResult serverRayTrace = performBlockRaytrace(RotationHandler.getCurrentRotation());
                            if (serverRayTrace.getBlockPos().equals(placePos) && (!raycast || serverRayTrace.sideHit == enumFacing.getOpposite())) {
                                dPlaceInfo = new PlaceInfo<>(blockPos, enumFacing, center);
                                break;
                            }
                            RayTraceResult rotationRayTrace = performBlockRaytrace(rotation);
                            if (rotationRayTrace.getBlockPos().equals(placePos) || (!raycast || rotationRayTrace.sideHit == enumFacing.getOpposite())) {
                                dPlaceInfo = new PlaceInfo<>(blockPos, enumFacing, center);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if(dPlaceInfo != null){
            setRotation(rotation);
            this.placeInfo = dPlaceInfo;
            return true;
        }
        return false;
    }

    private void setRotation(Rotation rotation){
        if(rotationMode.is("None"))return;

        float horizonSpeed = (float) RandomUtils.randomDouble(yawTurnSpeed.getFirst(), yawTurnSpeed.getSecond());
        float pitchSpeed = (float) RandomUtils.randomDouble(pitchTurnSpeed.getFirst(), pitchTurnSpeed.getSecond());

        int keepTicks = RandomUtils.randomInt((int) Math.round(this.keepTicks.getFirst()), (int) Math.round(this.keepTicks.getSecond()));
        int reverseTicks = RandomUtils.randomInt((int) Math.round(this.backTicks.getFirst()), (int) Math.round(this.backTicks.getSecond()));

        Rotation calculateRot = RotationUtils.smoothRotation(
                RotationHandler.getCurrentRotation(),
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

        if (placeInfo != null) {
            click(placeInfo.blockPos,placeInfo.facing,placeInfo.hitVec);
        }

        switch (rotationTiming.getValueByName()) {
            case "Normal":
                if(keepRotation.getValue() && RotationHandler.clientRotation != null){
                    setRotation(RotationHandler.clientRotation);
                }
                break;
            case "Bug":
                if(placeInfo != null){
                    Rotation rotation = RotationUtils.toRotation(placeInfo.hitVec, mc.player);
                    setRotation(rotation);
                }
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

        if(rayCast.getValue()){
            RayTraceResult rayTraceResult = performBlockRaytrace(RotationHandler.getCurrentRotation());
            if(rayTraceResult == null || !rayTraceResult.getBlockPos().equals(placePos.offset(facing)) || rayTraceResult.sideHit != facing.getOpposite()){
                println("1");
                return;
            }
        }

        if (!(mc.player.getHeldItem(hand).getItem() instanceof ItemBlock)) return;
        if(!iBlockState.getBlock().blockMaterial.isReplaceable()) { return; }

        mc.playerController.processRightClickBlock(mc.player, mc.world, placePos, facing, hitVec, hand);
        mc.player.swingArm(hand);
    }
}
