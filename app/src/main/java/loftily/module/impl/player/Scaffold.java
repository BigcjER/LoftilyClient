package loftily.module.impl.player;

import com.google.common.collect.Lists;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.player.motion.MoveEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.event.impl.render.Render2DEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.event.impl.world.PreUpdateEvent;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.font.FontManager;
import loftily.handlers.impl.player.MoveHandler;
import loftily.handlers.impl.player.RotationHandler;
import loftily.handlers.impl.render.AnimationHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.module.impl.player.scaffold.towers.MatrixTowerMove;
import loftily.module.impl.player.scaffold.towers.NCPTowerMove;
import loftily.module.impl.player.scaffold.towers.VanillaTowerMove;
import loftily.utils.block.BlockUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.math.Rotation;
import loftily.utils.player.InventoryUtils;
import loftily.utils.player.MoveUtils;
import loftily.utils.player.RotationUtils;
import loftily.utils.render.ColorUtils;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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

import static java.lang.Math.*;
import static loftily.utils.math.CalculateUtils.getMoveFixForward;
import static loftily.utils.math.CalculateUtils.getVectorForRotation;

@ModuleInfo(name = "Scaffold", category = ModuleCategory.PLAYER)
public class Scaffold extends Module {
    
    //Mode
    private final ModeValue scaffoldMode = new ModeValue("Mode", "Normal", this,
            new StringMode("Normal"),
            new StringMode("Telly"),
            new StringMode("Snap"),
            new StringMode("AutoJump"),
            new StringMode("SlowJump"),
            new StringMode("TickJump"));
    //Place
    private final ModeValue placeTiming = new ModeValue("PlaceTiming", "Tick", this,
            new StringMode("Pre"),
            new StringMode("Post"),
            new StringMode("Tick"));
    private final ModeValue clickCheck = new ModeValue("ClickCheck", "Strict", this,
            new StringMode("None"),
            new StringMode("Simple"),
            new StringMode("Strict"));
    private final ModeValue placeDelayMode = new ModeValue("PlaceDelayMode", "None", this,
            new StringMode("None"),
            new StringMode("Normal")
    );
    private final RangeSelectionNumberValue placeDelay = new RangeSelectionNumberValue("PlaceDelay", 40, 100, 0, 1000)
            .setVisible(() -> placeDelayMode.is("Normal"));
    private final BooleanValue placeDelayNoTower = new BooleanValue("PlaceDelay-NotTowering", false)
            .setVisible(() -> placeDelayMode.is("Normal"));
    //Rotation
    private final ModeValue rotationMode = new ModeValue("RotationMode", "None", this,
            new StringMode("Normal"),
            new StringMode("Round45"),
            new StringMode("Sexy"),
            new StringMode("None")
    );
    private final ModeValue rotationTiming = new ModeValue("RotationTiming", "Normal", this,
            new StringMode("Normal"), new StringMode("Always")
    );
    private final NumberValue distValue = new NumberValue("Dist", 0.0, 0.0, 0.3, 0.01);
    private final BooleanValue rayCastSearch = new BooleanValue("RayCast", false);
    private final BooleanValue keepRotation = new BooleanValue("KeepRotation", false);
    private final RangeSelectionNumberValue yawTurnSpeed = new RangeSelectionNumberValue("YawTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue pitchTurnSpeed = new RangeSelectionNumberValue("PitchTurnSpeed", 100, 150, 0, 360, 0.1);
    private final RangeSelectionNumberValue keepTicks = new RangeSelectionNumberValue("KeepRotationTicks", 1, 2, 0, 20);
    private final RangeSelectionNumberValue backTicks = new RangeSelectionNumberValue("ReverseTicks", 1, 2, 0, 20);
    private final BooleanValue silentRotation = new BooleanValue("SilentRotation", false);
    //Movement
    private final ModeValue eagleMode = new ModeValue("EagleMode", "None", this,
            new StringMode("None"),
            new StringMode("Prediction"),
            new StringMode("Simple")
    );
    private final NumberValue sneakTime = new NumberValue("SneakTime", 50, 0, 500, 1).setVisible(() -> !eagleMode.is("None"));
    private final NumberValue predictMotion = new NumberValue("Predict", 0, -1.0, 1.0, 0.01).setVisible(() -> eagleMode.is("Prediction"));
    
    private final ModeValue moveFixMode = new ModeValue("MoveFixMode", "None", this,
            new StringMode("None"),
            new StringMode("Silent"),
            new StringMode("45Angle"));
    private final ModeValue sprintMode = new ModeValue("SprintMode", "Default", this,
            new StringMode("Default"),
            new StringMode("Legit"),
            new StringMode("OFF")
    );
    @SuppressWarnings("unused")
    private final ModeValue towerMode = new ModeValue("TowerMode", "Jump", this,
            new MatrixTowerMove("Matrix"),
            new VanillaTowerMove("Vanilla"),
            new NCPTowerMove("NCP"),
            new StringMode("Jump")
    );
    private final BooleanValue towerFakeJump = new BooleanValue("TowerFakeJump", false);
    private final BooleanValue towerNoMove = new BooleanValue("TowerNoMove", false);
    
    private final BooleanValue onGround = new BooleanValue("OnGroundSafeWalk", false);
    private final BooleanValue inAir = new BooleanValue("InAirSafeWalk", false);
    
    private final BooleanValue motionModifier = new BooleanValue("MotionModifier", false);
    private final NumberValue motionSpeedSet = new NumberValue("MotionSpeed", 0.1, 0.0, 1.0, 0.01).setVisible(motionModifier::getValue);
    private final BooleanValue motionSpeedSetOnlyGround = new BooleanValue("MotionSpeedOnlyGround", false).setVisible(motionModifier::getValue);
    
    private final ModeValue adStrafeMode = new ModeValue("ADStrafe", "None", this, new StringMode("Teleport"),
            new StringMode("Legit"), new StringMode("None"));
    private final NumberValue adStrafeDelay = new NumberValue("ADStrafeDelay", 1, 1, 20).setVisible(() -> !adStrafeMode.is("None"));
    private final BooleanValue teleportMotion = new BooleanValue("TeleportMotion", false);
    private final BooleanValue teleportMotionOnlyGround = new BooleanValue("TeleportMotionOnlyGround", false).setVisible(() -> adStrafeMode.is("Teleport") && teleportMotion.getValue());
    private final NumberValue teleportSpeed = new NumberValue("TeleportSpeed", 0.1, 0.0, 0.4, 0.01).setVisible(() -> adStrafeMode.is("Teleport") && teleportMotion.getValue());
    private final NumberValue teleportStrength = new NumberValue("TeleportStrength", 0.1, 0.0, 0.4, 0.01).setVisible(() -> adStrafeMode.is("Teleport"));
    //SameY
    private final BooleanValue sameY = new BooleanValue("SameY", false);
    private final BooleanValue allowJump = new BooleanValue("AllowJump", false);
    
    //Other
    private final BooleanValue autoSwitchToBlock = new BooleanValue("AutoSwitchToBlock", true);
    private final BooleanValue switchBackOnDisable = new BooleanValue("SwitchBackOnDisable", true);
    private final BooleanValue blockCounter = new BooleanValue("BlockCounter", false);
    private final BooleanValue blockCounterCustomOpacity = new BooleanValue("CustomBackgroundOpacity", false)
            .setVisible(blockCounter::getValue);
    private final NumberValue blockCounterCustomOpacityValue = new NumberValue("CustomBackgroundOpacityValue", 255, 1, 255)
            .setVisible(() -> blockCounter.getValue() && blockCounterCustomOpacity.getValue());
    
    private boolean zitterDirection = false;
    private final DelayTimer placeTimer = new DelayTimer();
    private PlaceInfo placeInfo = null;
    private double lastY = 0.0;
    public boolean towerStatus = false;
    private int currentPlaceDelay = 0;
    private int prevSlot;
    
    
    private final Animation blockCountAnimation = new Animation(Easing.EaseOutExpo, 300);
    private Runnable blockCounterRunnable;
    
    @Override
    public void onDisable() {
        placeTimer.reset();
        currentPlaceDelay = RandomUtils.randomInt((int) placeDelay.getFirst(), (int) placeDelay.getSecond());
        placeInfo = null;
        if (switchBackOnDisable.getValue()) {
            mc.player.inventory.currentItem = prevSlot;
        }
        
        if (blockCounter.getValue()) {
            if (blockCounterRunnable != null)
                AnimationHandler.add(blockCountAnimation, blockCounterRunnable);
        }
    }
    
    @Override
    public void onEnable() {
        lastY = mc.player.posY;
        prevSlot = mc.player.inventory.currentItem;
    }
    
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (!blockCounter.getValue()) return;
        
        int size = 50;
        
        int startX = event.getScaledResolution().getScaledWidth() / 2 - size / 2;
        
        
        blockCounterRunnable = () -> {
            blockCountAnimation.run(this.isToggled() ? 1 : 0);
            int opacity = blockCounterCustomOpacity.getValue() ? blockCounterCustomOpacityValue.getValue().intValue() : (int) (blockCountAnimation.getValuef() * 255);
            
            if(opacity <= 0) return;
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(startX, event.getScaledResolution().getScaledHeight() - (100 * blockCountAnimation.getValuef()), 0);
            
            RenderUtils.drawRoundedRect(0, 0, size, size, 3,
                    ColorUtils.colorWithAlpha(Colors.BackGround.color, opacity));
            FontManager.NotoSans.of(16).drawCenteredString(String.valueOf(InventoryUtils.getBlocksInHotBar()), size / 2F, size - size / 3.5F + 1,
                    ColorUtils.colorWithAlpha(Colors.Text.color, 255));
            
            //Draw stack
            ItemStack stack;
            if (InventoryUtils.findBlockInHotBar() != -1) {
                stack = mc.player.inventory.getStackInSlot(InventoryUtils.findBlockInHotBar());
            } else {
                stack = new ItemStack(Blocks.BARRIER);
            }
            
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            GlStateManager.color(1F, 1F, 1F, opacity / 255F);
            
            mc.getRenderItem().zLevel = -150.0f;
            GlStateManager.scale(1.4F, 1.4F, 1.4F);
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 10, 5);
            mc.getRenderItem().zLevel = 0.0f;
            
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
            
            GlStateManager.popMatrix();
            
        };
        
        blockCounterRunnable.run();
    }
    
    @EventHandler
    public void onMove(MoveEvent event) {
        if (event.getEntity() != mc.player) return;
        if ((onGround.getValue() && mc.player.onGround) || (inAir.getValue() && !mc.player.onGround)) {
            event.setSafeWalk(true);
        }
    }
    
    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null) return;
        
        if (mc.player.onGround && MoveUtils.isMoving() && scaffoldMode.is("TickJump")) {
            mc.player.tryJump();
        }
        
        if (motionModifier.getValue()) {
            if (!motionSpeedSetOnlyGround.getValue() || mc.player.onGround) {
                MoveUtils.setSpeed(motionSpeedSet.getValue(), true);
            }
        }
        
        if (mc.player.ticksExisted % adStrafeDelay.getValue() == 0) {
            switch (adStrafeMode.getValueByName()) {
                case "None":
                    break;
                case "Teleport":
                    if (MoveUtils.isMoving()) {
                        if (teleportMotion.getValue()) {
                            if (!teleportMotionOnlyGround.getValue() || mc.player.onGround) {
                                MoveUtils.setSpeed(teleportSpeed.getValue(), true);
                            }
                        }
                        double yaw = Math.toRadians(mc.player.rotationYaw + (zitterDirection ? 90.0 : -90.0));
                        
                        mc.player.motionX -= sin(yaw) * teleportStrength.getValue();
                        mc.player.motionZ += cos(yaw) * teleportStrength.getValue();
                        zitterDirection = !zitterDirection;
                    }
                    break;
                case "Legit":
                    if (MoveUtils.isMoving()) {
                        float forward = mc.player.movementInput.moveForward;
                        float strafe;
                        if (zitterDirection) {
                            strafe = forward > 0.0f ? 0.98f : -0.98f;
                        } else {
                            strafe = forward > 0.0f ? -0.98f : 0.98f;
                        }
                        MoveHandler.setMovement(forward, strafe);
                        zitterDirection = !zitterDirection;
                    }
                    break;
            }
        }
    }
    
    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.player == null) return;
        
        searchBlock();
        
        if (placeInfo == null)
            return;
        
        if (rotationTiming.getValueByName().equals("Always")) {
            setRotation(placeInfo.getRotation());
        }
        if (rotationMode.is("Sexy")) {
            setRotation(RotationUtils.toRotation(placeInfo.getHitVec(), mc.player));
        }
        
        
        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) && !(mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock)) {
            if (autoSwitchToBlock.getValue()) {
                int slot = InventoryUtils.findBlockInHotBar();
                
                if (slot != -1)
                    mc.player.inventory.currentItem = slot;
            }
            
            return;
        }
        
        if (keepRotation.getValue() && RotationHandler.clientRotation != null) {
            setRotation(RotationHandler.clientRotation);
        }
        
        switch (scaffoldMode.getValueByName()) {
            case "Telly":
                if (MoveUtils.isMoving() && mc.player.onGround) {
                    setRotation(new Rotation((float) (MoveUtils.getDirection() * 180 / Math.PI), RotationHandler.getCurrentRotation().pitch));
                }
                break;
            case "Snap":
                if (!(mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ)).getBlock() instanceof BlockAir)) {
                    setRotation(new Rotation((float) (MoveUtils.getDirection() * 180 / Math.PI), RotationHandler.getCurrentRotation().pitch));
                }
                break;
        }
        
        switch (eagleMode.getValueByName()) {
            case "Prediction":
                IBlockState iBlockState = mc.world.getBlockState(new BlockPos(mc.player.posX + mc.player.motionX * predictMotion.getValue(),
                        mc.player.posY - 1.0,
                        mc.player.posZ + mc.player.motionZ * predictMotion.getValue()));
                
                if (iBlockState.getBlock() instanceof BlockAir && mc.player.onGround) {
                    MoveHandler.setSneak(true, sneakTime.getValue().intValue());
                }
                break;
            case "Simple":
                IBlockState blockState = mc.world.getBlockState(new BlockPos(mc.player.posX,
                        mc.player.posY - 1.0,
                        mc.player.posZ));
                if (blockState.getBlock() instanceof BlockAir && mc.player.onGround) {
                    MoveHandler.setSneak(true, sneakTime.getValue().intValue());
                }
                break;
        }
        
        click(placeInfo.blockPos, placeInfo.facing, placeInfo.hitVec);
    }
    
    @EventHandler
    public void onStrafe(StrafeEvent event) {
        switch (scaffoldMode.getValueByName()) {
            case "Telly":
                if (MoveUtils.isMoving()) {
                    if (!mc.player.isSprinting() && mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.gameSettings.keyBindJump.setPressed(false);
                    }
                    if (mc.player.isSprinting()) {
                        if (mc.player.onGround && MoveUtils.isMoving()) {
                            mc.player.tryJump();
                        }
                        mc.gameSettings.keyBindJump.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindJump));
                    }
                } else {
                    mc.gameSettings.keyBindJump.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindJump));
                }
                break;
            case "AutoJump":
                if (mc.player.onGround && MoveUtils.isMoving()) {
                    mc.player.tryJump();
                }
                break;
            case "SlowJump":
                if (mc.player.onGround && MoveUtils.isMoving()) {
                    mc.player.motionY = 0.42;
                }
                break;
            case "Normal":
            case "Snap":
                break;
        }
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        towerStatus = mc.gameSettings.keyBindJump.isKeyDown() &&
                (!towerNoMove.getValue() || !MoveUtils.isMoving());
        
        if (placeInfo != null) {
            if ((event.isPre() && placeTiming.is("Pre")) || (event.isPost() && placeTiming.is("Post"))) {
                click(placeInfo.blockPos, placeInfo.facing, placeInfo.hitVec);
            }
        }
        
        if (towerStatus && allowJump.getValue()) {
            lastY = mc.player.posY;
        }
    }
    
    public void fakeJump() {
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
    
    private BlockPos getOptimalPos() {
        BlockPos playerPos;
        if (mc.player.posY == Math.round(mc.player.posY) + 0.5) {
            playerPos = new BlockPos((mc.player.posX), (mc.player.posY), (mc.player.posZ));
        } else {
            playerPos = new BlockPos((mc.player.posX), (mc.player.posY), (mc.player.posZ)).down();
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
        
        if (!iBlockState.getMaterial().isReplaceable() ||
                calculateCenter(playerPos, true)) {
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
        
        Vec3d reach = eyes.add(new Vec3d(rotationVec.xCoord * maxReach, rotationVec.yCoord * maxReach, rotationVec.zCoord * maxReach));
        
        return world.rayTraceBlocks(eyes, reach, false, false, true);
    }
    
    private PlaceInfo getPlaceInfo(BlockPos blockPos, BlockPos placePos, Vec3d vec3, EnumFacing enumFacing, Boolean raycast) {
        Vec3d eyes = mc.player.getEyes();
        Vec3d center;
        Vec3d blockVec = new Vec3d(blockPos);
        
        Rotation rotation;
        PlaceInfo dPlaceInfo;
        Vec3d directionVec = new Vec3d(enumFacing.getDirectionVec());
        
        center = (blockVec.addVector(vec3.xCoord, vec3.yCoord, vec3.zCoord)).addVector(
                directionVec.xCoord * vec3.xCoord, directionVec.yCoord * vec3.yCoord, directionVec.zCoord * vec3.zCoord
        );
        double reach = mc.playerController.getBlockReachDistance();
        double distance = eyes.distanceTo(center);
        if (raycast && (distance > reach || mc.world.rayTraceBlocks(eyes, center, false, true, false) != null)) {
            return null;
        }
        Vec3d vec = center.subtract(eyes);
        if (enumFacing.getAxis() != EnumFacing.Axis.Y) {
            double dist = abs(enumFacing.getAxis() == EnumFacing.Axis.X ? vec.xCoord : vec.zCoord);
            if (dist < distValue.getValue() && !mc.player.movementInput.sneak) {
                return null;
            }
        }
        rotation = RotationUtils.toRotation(center, mc.player);
        
        switch (rotationMode.getValueByName()) {
            case "None":
            case "Normal":
                break;
            case "Round45":
                rotation = new Rotation(
                        Math.round(rotation.yaw / 45) * 45,
                        rotation.pitch
                );
                break;
        }
        
        if (!rayCastSearch.getValue()) {
            dPlaceInfo = new PlaceInfo(placePos, enumFacing.getOpposite(), center, rotation);
            return dPlaceInfo;
        } else {
            RayTraceResult serverRayTrace = performBlockRaytrace(RotationHandler.getRotation());
            if (serverRayTrace.getBlockPos().equals(placePos) && (!raycast || serverRayTrace.sideHit.equals(enumFacing.getOpposite()))) {
                dPlaceInfo = new PlaceInfo(placePos, serverRayTrace.sideHit, center, rotation);
                return dPlaceInfo;
            }
            RayTraceResult rotationRayTrace = performBlockRaytrace(rotation);
            println("New: " + rotationRayTrace.sideHit + " R: " + enumFacing);
            if (rotationRayTrace.getBlockPos().equals(placePos) && (!raycast || rotationRayTrace.sideHit.equals(enumFacing.getOpposite()))) {
                dPlaceInfo = new PlaceInfo(placePos, rotationRayTrace.sideHit, center, rotation);
                return dPlaceInfo;
            }
        }
        return null;
    }
    
    private PlaceInfo compareDifferences(
            PlaceInfo newPlaceRotation, PlaceInfo oldPlaceRotation, Rotation rotation) {
        if (oldPlaceRotation == null || RotationUtils.getRotationDifference(newPlaceRotation.getRotation(), rotation) <
                RotationUtils.getRotationDifference(oldPlaceRotation.getRotation(), rotation)
        ) {
            return newPlaceRotation;
        }
        
        return oldPlaceRotation;
    }
    
    private Boolean calculateCenter(BlockPos blockPos, Boolean raycast) {
        
        if (!Objects.requireNonNull(blockPos.getState()).getBlock().blockMaterial.isReplaceable()) {
            return false;
        }
        
        PlaceInfo dPlaceInfo = null;
        PlaceInfo ePlaceInfo;
        
        for (EnumFacing enumFacing : EnumFacing.VALUES) {
            BlockPos placePos = blockPos.offset(enumFacing);
            if (!BlockUtils.canBeClick(placePos)) continue;
            for (double x = 0.2; x <= 0.8; x += 0.1) {
                for (double y = 0.2; y <= 0.8; y += 0.1) {
                    for (double z = 0.2; z <= 0.8; z += 0.1) {
                        ePlaceInfo = getPlaceInfo(blockPos, placePos, new Vec3d(x, y, z), enumFacing, raycast);
                        if (ePlaceInfo == null) {
                            continue;
                        }
                        dPlaceInfo = compareDifferences(ePlaceInfo, dPlaceInfo, RotationHandler.getRotation());
                    }
                }
            }
        }
        
        if (dPlaceInfo == null) return false;
        
        this.placeInfo = dPlaceInfo;
        if (rotationTiming.is("Normal")) {
            setRotation(placeInfo.rotation);
        }
        return true;
    }
    
    private void setRotation(Rotation rotation) {
        if (rotationMode.is("None")) return;
        
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
            RotationHandler.setClientRotation(calculateRot, keepTicks, reverseTicks, moveFixMode.getValue().getName());
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
                facing,
                mc.player,
                heldItem
        )
        ) {
            placeTimer.reset();
            return;
        }
        if (!Objects.requireNonNull(placePos.getState()).getBlock().canCollideCheck(placePos.getState(), false)) {
            placeTimer.reset();
            return;
        }
        
        if (!clickCheck.is("None")) {
            RayTraceResult rayTraceResult = performBlockRaytrace(RotationHandler.getCurrentRotation());
            if (rayTraceResult == null || !rayTraceResult.getBlockPos().equals(placePos) || (rayTraceResult.sideHit.equals(facing.getOpposite()) && clickCheck.is("Strict"))) {
                return;
            }
        }
        
        if (placeDelayMode.is("Normal")) {
            if (!placeTimer.hasTimeElapsed(currentPlaceDelay) && (!placeDelayNoTower.getValue() || !towerStatus)) {
                return;
            }
        }
        
        if (mc.playerController.processRightClickBlock(mc.player, mc.world, placePos, facing, hitVec, hand) == EnumActionResult.SUCCESS) {
            mc.player.swingArm(hand);
            placeTimer.reset();
            currentPlaceDelay = RandomUtils.randomInt((int) placeDelay.getFirst(), (int) placeDelay.getSecond());
        }
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    public static class PlaceInfo {
        public BlockPos blockPos;
        public EnumFacing facing;
        public Vec3d hitVec;
        public Rotation rotation;
    }
}