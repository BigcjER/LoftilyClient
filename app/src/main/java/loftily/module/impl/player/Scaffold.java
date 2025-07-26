package loftily.module.impl.player;

import com.google.common.collect.Lists;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.player.motion.MoveEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.event.impl.render.Render2DEvent;
import loftily.event.impl.render.Render3DEvent;
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
import loftily.module.impl.player.scaffold.towers.MatrixTower;
import loftily.module.impl.player.scaffold.towers.NCPTower;
import loftily.module.impl.player.scaffold.towers.VanillaTower;
import loftily.utils.block.BlockUtils;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.math.Rotation;
import loftily.utils.player.*;
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
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
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
import static loftily.utils.player.PlayerUtils.nearAir;
import static loftily.utils.player.RotationUtils.getAngleDifference;

@ModuleInfo(name = "Scaffold", category = ModuleCategory.PLAYER)
public class Scaffold extends Module {
    
    //Mode
    private final ModeValue scaffoldMode = new ModeValue("Mode", "Normal", this,
            new StringMode("Normal"),
            new StringMode("Telly"),
            new StringMode("Snap"),
            new StringMode("AutoJump"),
            new StringMode("SlowJump"),
            new StringMode("TickJump"),
            new StringMode("MatrixHop"),
            new StringMode("GodBridge"));
    private final BooleanValue moveHelperGod = new BooleanValue("MoveHelper-GodBridge", false).setVisible(()->scaffoldMode.is("GodBridge"));
    private final BooleanValue matrixHopLow = new BooleanValue("MatrixHop-Low", false).setVisible(()->scaffoldMode.is("MatrixHop"));
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
    private final BooleanValue sneakCancelPlace = new BooleanValue("SneakCancelPlace",false);
    private final RangeSelectionNumberValue placeDelay = new RangeSelectionNumberValue("PlaceDelay", 40, 100, 0, 1000)
            .setVisible(() -> placeDelayMode.is("Normal"));
    private final BooleanValue placeDelayNoTower = new BooleanValue("PlaceDelay-NotTowering", false)
            .setVisible(() -> placeDelayMode.is("Normal"));
    private final BooleanValue extraClick = new BooleanValue("ExtraClick", false);
    private final RangeSelectionNumberValue extraClickDelay = new RangeSelectionNumberValue("ExtraClickDelay", 40, 100, 0, 200).setVisible(extraClick::getValue);
    private final BooleanValue extraClickNoTower = new BooleanValue("ExtraClick-NotTowering", false).setVisible(extraClick::getValue);
    private final BooleanValue extraClickOnlyData = new BooleanValue("ExtraClick-OnlyData",false).setVisible(extraClick::getValue);
    private final BooleanValue extraClickOnlyFaceBlock = new BooleanValue("ExtraClick-OnlyFaceBlock", false).setVisible(extraClick::getValue);
    private final BooleanValue extraClickOnlyMove = new BooleanValue("ExtraClick-OnlyMove", false).setVisible(extraClick::getValue);
    private final BooleanValue extraClickDoubleClickAble = new BooleanValue("ExtraClick-DoubleClickAble", false).setVisible(extraClick::getValue);
    private final ModeValue extraClickMode = new ModeValue("ExtraClickMode","OnlyFail",this,
            new StringMode("OnlyFail"),new StringMode("Always"));
    private final ModeValue extraClickTime = new ModeValue("ExtraClickTime", "BeforePlace", this,
            new StringMode("BeforePlace"),
            new StringMode("AfterPlace"),
            new StringMode("Legit")).setVisible(extraClick::getValue);
    private final NumberValue maxExtraClicks = new NumberValue("MaxExtraClicks",10,1,50).setVisible(extraClick::getValue);
    //Rotation
    private final ModeValue rotationMode = new ModeValue("RotationMode", "None", this,
            new StringMode("Normal"),
            new StringMode("Sexy"),
            new StringMode("StaticGodBridge"),
            new StringMode("StaticBack"),
            new StringMode("Advance"),
            new StringMode("None")
    );
    private final BooleanValue stableRotation = new BooleanValue("StableRotation", false);
    private final ModeValue advanceRotationYaw = new ModeValue("AdvanceRotationYaw", "Normal", this,
            new StringMode("Normal"),
            new StringMode("Other"),
            new StringMode("Static"),
            new StringMode("Back"))
            .setVisible(() -> rotationMode.is("Advance"));
    private final NumberValue staticYawValue = new NumberValue("StaticYaw", 180, -180, 180, 0.01)
            .setVisible(() -> rotationMode.is("Advance") && advanceRotationYaw.is("Static"));
    private final BooleanValue smartDirection = new BooleanValue("SmartDirection", false).setVisible(() -> staticYawValue.getVisible().get());
    private final ModeValue advanceRotationPitch = new ModeValue("AdvanceRotationPitch", "Normal", this,
            new StringMode("Normal"),
            new StringMode("Static"))
            .setVisible(() -> rotationMode.is("Advance"));
    private final NumberValue staticPitchValue = new NumberValue("StaticPitch", 80.34, -90, 90, 0.01)
            .setVisible(() -> rotationMode.is("Advance") && advanceRotationPitch.is("Static"));
    private final ModeValue rotationTiming = new ModeValue("RotationTiming", "Normal", this,
            new StringMode("Normal"), new StringMode("Always")
    );
    private final ModeValue centerMode = new ModeValue("CenterMode","Angle",this,
            new StringMode("Angle"),new StringMode("Distance"),new StringMode("Proportion")
    );
    private final RangeSelectionNumberValue distancePro = new RangeSelectionNumberValue("DistanceProportion",0.0,1.0,0,1.0,0.01).setVisible(()->centerMode.is("Proportion"));
    private final RangeSelectionNumberValue anglePro = new RangeSelectionNumberValue("AngleProportion",0.0,1.0,0,1.0,0.01).setVisible(()->centerMode.is("Proportion"));
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
            new StringMode("Ground"),
            new StringMode("Air"),
            new StringMode("OFF")
    );
    private final BooleanValue sprintNotTowering = new BooleanValue("Sprint-NoTower", false);
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
    @SuppressWarnings("unused")
    private final ModeValue towerMode = new ModeValue("TowerMode", "Jump", this,
            new MatrixTower("Matrix"),
            new VanillaTower("Vanilla"),
            new NCPTower("NCP"),
            new StringMode("Jump")
    );
    private final BooleanValue autoSwitchToBlock = new BooleanValue("AutoSwitchToBlock", true);
    private final BooleanValue switchBackOnDisable = new BooleanValue("SwitchBackOnDisable", true);
    
    private final BooleanValue blockCounter = new BooleanValue("BlockCounter", false);
    private final BooleanValue blockCounterCustomOpacity = new BooleanValue("CustomBackgroundOpacity", false)
            .setVisible(blockCounter::getValue);
    private final NumberValue blockCounterCustomOpacityValue = new NumberValue("CustomBackgroundOpacityValue", 255, 1, 255)
            .setVisible(() -> blockCounter.getValue() && blockCounterCustomOpacity.getValue());
    private final DelayTimer placeTimer = new DelayTimer();
    //Other
    private final BooleanValue useLargestStack = new BooleanValue("UseLargestStack", false);
    private final BooleanValue earlySwitch = new BooleanValue("EarlySwitch", false);
    private final NumberValue earlySwitchAmounts = new NumberValue("EarlySwitchAmounts",2,1,64).setVisible(earlySwitch::getValue);
    private final ModeValue swingMode = new ModeValue("Swing", "Vanilla", this,
            new StringMode("Vanilla"),
            new StringMode("Packet"),
            new StringMode("NoPacket"));
    private final DelayTimer extraClickTimer = new DelayTimer();
    private final Animation blockCountAnimation = new Animation(Easing.EaseOutExpo, 300);
    public boolean towerStatus = false;
    private boolean zitterDirection = false;
    private PlaceInfo placeInfo = null;
    private double lastY = 0.0;
    private int currentPlaceDelay = 0;
    private int prevSlot;
    private int curExtraClickDelay = 0;
    private int clickTimes = 0;
    private Runnable blockCounterRunnable;
    private boolean ableSneak = false;
    
    public boolean canExtraClick() {
        return (!extraClickNoTower.getValue() || !towerStatus) &&
                (!extraClickOnlyMove.getValue() || MoveUtils.isMoving()) &&
                (!extraClickOnlyFaceBlock.getValue() || performBlockRaytrace(RotationHandler.getCurrentRotation()).typeOfHit == RayTraceResult.Type.BLOCK);
    }
    
    @Override
    public void onDisable() {
        ableSneak = false;
        placeTimer.reset();
        currentPlaceDelay = RandomUtils.randomInt((int) placeDelay.getFirst(), (int) placeDelay.getSecond());
        placeInfo = null;
        if (switchBackOnDisable.getValue()) {
            mc.player.inventory.currentItem = prevSlot;
        }
        clickTimes = 0;
        if(blockCountAnimation != null && blockCounterRunnable != null) {
            AnimationHandler.add(blockCountAnimation, blockCounterRunnable);
        }
    }
    
    @Override
    public void onEnable() {
        if (scaffoldMode.is("MatrixHop") && mc.player.onGround) {
            mc.player.motionY = 0.42;
            MoveUtils.stop(false);
        }
        curExtraClickDelay = RandomUtils.randomInt((int) extraClickDelay.getFirst(), (int) extraClickDelay.getSecond());
        clickTimes = 0;
        lastY = mc.player.posY;
        prevSlot = mc.player.inventory.currentItem;
    }
    
    @EventHandler
    public void onMove(MoveEvent event) {
        if (event.getEntity() != mc.player) return;
        if ((onGround.getValue() && mc.player.onGround) || (inAir.getValue() && !mc.player.onGround)) {
            event.setSafeWalk(true);
        }
    }
    
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (!blockCounter.getValue()) return;
        
        int size = 50;
        
        int startX = event.getScaledResolution().getScaledWidth() / 2 - size / 2;
        
        blockCounterRunnable = () -> {
            blockCountAnimation.run(this.isToggled() ? 1 : 0);
            int opacity = blockCounterCustomOpacity.getValue() ? blockCounterCustomOpacityValue.getValue().intValue() : (int) (blockCountAnimation.getValuef() * 255);
            
            if (opacity <= 0) return;
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(startX, event.getScaledResolution().getScaledHeight() - (100 * blockCountAnimation.getValuef()), 0);
            
            RenderUtils.drawRoundedRect(0, 0, size, size, 3,
                    ColorUtils.colorWithAlpha(Colors.BackGround.color, (int) (blockCountAnimation.getValuef() * 255)));
            FontManager.NotoSans.of(16).drawCenteredString(String.valueOf(InventoryUtils.getBlocksInHotBar()), size / 2F, size - size / 3.5F,
                    ColorUtils.colorWithAlpha(Colors.Text.color, (int) (blockCountAnimation.getValuef() * 255)));
            
            //Draw stack
            ItemStack stack;
            if (InventoryUtils.findBlockInHotBar(useLargestStack.getValue()) != -1) {
                stack = mc.player.inventory.getStackInSlot(InventoryUtils.findBlockInHotBar(useLargestStack.getValue()));
            } else {
                stack = new ItemStack(Blocks.BARRIER);
            }
            
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            GlStateManager.color(1, 1, 1, blockCountAnimation.getValuef());
            
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
    
    public void runExtraClick(EnumHand hand) {
        RayTraceResult rayTraceResult = performBlockRaytrace(RotationHandler.getCurrentRotation());
        if(sneakCancelPlace.getValue() && mc.player.isSneaking()){
            clickTimes--;
            return;
        }
        while (clickTimes > 0) {
            BlockPos posIn = rayTraceResult.getBlockPos();
            ItemBlock itemBlock = (ItemBlock) mc.player.getHeldItem(hand).getItem();
            Vec3d facing = rayTraceResult.hitVec;
            BlockPos stack = rayTraceResult.getBlockPos();
            float f = (float)(facing.xCoord - (double)stack.getX());
            float f1 = (float)(facing.yCoord - (double)stack.getY());
            float f2 = (float)(facing.zCoord - (double)stack.getZ());
            if(rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                if ((!extraClickOnlyData.getValue() || (placeInfo != null && rayTraceResult.getBlockPos().equals(placeInfo.blockPos))) && (!itemBlock.canPlaceBlockOnSide(mc.world, posIn, rayTraceResult.sideHit, mc.player, mc.player.getHeldItem(hand)) || !extraClickMode.is("OnlyFail"))) {
                    PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(stack,rayTraceResult.sideHit,hand,f,f1,f2));

                }else {
                    break;
                }
            } else if(rayTraceResult.typeOfHit == RayTraceResult.Type.MISS){
                PacketUtils.sendPacket(new CPacketPlayerTryUseItem(hand));
            }else {
                break;
            }
            if (!extraClickDoubleClickAble.getValue()) {
                clickTimes = 0;
                break;
            }
            clickTimes--;
        }
    }
    
    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null) return;
        
        if (mc.player.onGround && MoveUtils.isMoving() && (scaffoldMode.is("TickJump") || scaffoldMode.is("MatrixHop"))) {
            mc.player.tryJump();
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
                        double yaw = toRadians(mc.player.rotationYaw + (zitterDirection ? 90.0 : -90.0));
                        
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
                        MoveHandler.setMovement(forward, strafe, 0);
                        zitterDirection = !zitterDirection;
                    }
                    break;
            }
        }
        
        if ((!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) && !(mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock))
        || (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock && mc.player.getHeldItemMainhand().getStackSize() <= earlySwitchAmounts.getValue())
        || (mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock && mc.player.getHeldItemOffhand().getStackSize() <= earlySwitchAmounts.getValue())) {
            if (autoSwitchToBlock.getValue()) {
                int slot = InventoryUtils.findBlockInHotBar(useLargestStack.getValue());
                
                if (slot != -1) {
                    mc.player.inventory.currentItem = slot;
                }else {
                    return;
                }
            }
        }
        
        if (extraClick.getValue() && extraClickTime.is("BeforePlace")) {
            if (canExtraClick() && clickTimes > 0) {
                EnumHand hand = mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                if (mc.player.getHeldItem(hand).getItem() instanceof ItemBlock) {
                    runExtraClick(hand);
                }
            }
        }

        if (placeInfo != null) {
            Rotation staRot = staticRotation(placeInfo.rotation);
            if (staRot != null) {
                setRotation(staRot);
            }
        } else {
            Rotation staRot = staticRotation(null);
            if (staRot != null) {
                setRotation(staRot);
            }
        }
        
        searchBlock();
        
        if (placeInfo == null)
            return;
        
        if (rotationTiming.getValueByName().equals("Always")) {
            setRotation(placeInfo.getRotation());
        }
        if (rotationMode.is("Sexy")) {
            setRotation(RotationUtils.toRotation(placeInfo.getHitVec(), mc.player));
        }

        if (staticRotation(placeInfo.rotation) != null) {
            Rotation staRot = staticRotation(placeInfo.rotation);
            setRotation(staRot);
        }
        
        if (keepRotation.getValue() && RotationHandler.clientRotation != null) {
            setRotation(RotationHandler.clientRotation);
        }
        
        switch (scaffoldMode.getValueByName()) {
            case "Telly":
                if (MoveUtils.isMoving() && mc.player.onGround) {
                    setRotation(new Rotation((float) (MoveUtils.getDirection() * 180 / PI), RotationHandler.getCurrentRotation().pitch));
                }
                break;
            case "Snap":
                if (!(mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ)).getBlock() instanceof BlockAir)) {
                    setRotation(new Rotation((float) (MoveUtils.getDirection() * 180 / PI), RotationHandler.getCurrentRotation().pitch));
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
        
        PlaceInfo targetInfo = null;
        RayTraceResult rayTraceResult = performBlockRaytrace(RotationHandler.getCurrentRotation());
        if (clickCheck.is("None")) {
            targetInfo = placeInfo;
        } else {
            if (rayTraceResult != null) {
                switch (clickCheck.getValueByName()) {
                    case "None":
                        break;
                    case "Simple":
                        if (rayTraceResult.getBlockPos().equals(placeInfo.blockPos)) {
                            targetInfo = new PlaceInfo(rayTraceResult.getBlockPos(), placeInfo.facing, placeInfo.hitVec, placeInfo.rotation);
                        }
                        break;
                    case "Strict":
                        if (rayTraceResult.getBlockPos().equals(placeInfo.blockPos) && rayTraceResult.sideHit == placeInfo.facing) {
                            targetInfo = new PlaceInfo(rayTraceResult.getBlockPos(), rayTraceResult.sideHit, rayTraceResult.hitVec, placeInfo.rotation);
                        }
                        break;
                }
            }
        }
        EnumHand hand = mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        if ((!click(targetInfo) && extraClickTime.is("Legit")) || extraClickTime.is("AfterPlace")) {
            if (extraClick.getValue()) {
                if (canExtraClick() && clickTimes > 0) {
                    if (mc.player.getHeldItem(hand).getItem() instanceof ItemBlock) {
                        runExtraClick(hand);
                    }
                }
            }
        }
    }

    public Rotation staticRotation(Rotation rot) {
        Rotation rotation = null;
        switch (rotationMode.getValueByName()) {
            case "StaticGodBridge":
                rotation = new Rotation(
                        PlayerUtils.isDiagonally() ? MoveUtils.getMovingYaw() - 180 : MoveUtils.getMovingYaw() + (
                                PlayerUtils.onRightSide(mc.player) ? 135F : -135F),
                        towerStatus && !MoveUtils.isMoving() ? 89.6F : PlayerUtils.isDiagonally() ? 75.6F : 73.5F);
                break;
            case "StaticBack":
                rotation = new Rotation(
                        MoveUtils.getMovingYaw() + 180,
                        rot == null ? 80.34F : rot.pitch
                );
                break;
            case "Advance":
                float yaw = 0;
                float pitch = 0;
                switch (advanceRotationYaw.getValueByName()) {
                    case "Normal":
                        yaw = rot == null ? MoveUtils.getMovingYaw() + 180F : rot.yaw;
                        break;
                    case "Other":
                        if (rot == null) {
                            yaw = MoveUtils.getMovingYaw() + 180F;
                        } else {
                            yaw = Math.round((rot.yaw - MoveUtils.getMovingYaw() + 180F) / 45) * 45 + MoveUtils.getMovingYaw() - 180F;
                        }
                        break;
                    case "Static":
                        yaw = (float) (MoveUtils.getMovingYaw() + (smartDirection.getValue() ? PlayerUtils.onRightSide(mc.player) ? staticYawValue.getValue() : -staticYawValue.getValue() : staticYawValue.getValue()));
                        break;
                    case "Back":
                        if (rot == null) {
                            yaw = MoveUtils.getMovingYaw() + 180F;
                        } else {
                            if (!PlayerUtils.isDiagonally()) {
                                float round = (Math.round(rot.yaw / 90) * 90);
                                float round2 = (Math.round((MoveUtils.getMotionYaw() + 180) / 45) * 45);
                                float difference = getAngleDifference(round, round2);
                                yaw = MoveUtils.getMovingYaw() + 180 + difference;
                            } else {
                                yaw = Math.round((rot.yaw - MoveUtils.getMovingYaw() + 180F) / 45) * 45 + MoveUtils.getMovingYaw() - 180F;
                            }
                        }
                        break;
                }
                switch (advanceRotationPitch.getValueByName()) {
                    case "Normal":
                        pitch = rot == null ? 80.34F : rot.pitch;
                        break;
                    case "Static":
                        pitch = staticPitchValue.getValue().floatValue();
                        break;
                }
                rotation = new Rotation(yaw, pitch);
                break;
        }
        if (stableRotation.getValue()) {
            if (rotation != null) {
                rotation.yaw = round(rotation.yaw / 45) * 45;
            }
        }
        return rotation;
    }
    
    @EventHandler
    public void onStrafe(StrafeEvent event) {
        if (motionModifier.getValue()) {
            if (!motionSpeedSetOnlyGround.getValue() || mc.player.onGround) {
                MoveUtils.setSpeed(motionSpeedSet.getValue(), true);
            }
        }

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
            case "MatrixHop":
                if (!MoveUtils.isMoving()) return;
                if (mc.player.onGround) {
                    mc.player.tryJump();
                }
                if (MoveUtils.getSpeed() <= 0.19 && !mc.player.isCollidedHorizontally && !mc.player.onGround && !towerStatus) {
                    MoveUtils.setSpeed(0.19,true);
                }
                if (mc.player.hurtTime <= 0 && matrixHopLow.getValue()) {
                    mc.player.motionY -= 0.0032;
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
            case "GodBridge":
                if (MoveUtils.isMoving() && mc.player.onGround && !mc.player.isSneaking() && !mc.gameSettings.keyBindSneak.isKeyDown() &&
                        mc.world.getCollisionBoxes(mc.player, mc.player.getBox()
                                .offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001)).isEmpty()
                        && (scaffoldMode.is("GodBridge"))) {
                    mc.player.tryJump();
                }
                if (moveHelperGod.getValue()) {
                    BlockPos blockPos = new BlockPos(mc.player).down();
                    boolean isDiagonally = PlayerUtils.isDiagonally();
                    double xMin = blockPos.getX() + 0.35;
                    double xMax = blockPos.getX() + 0.65;
                    double zMin = blockPos.getZ() + 0.35;
                    double zMax = blockPos.getZ() + 0.65;
                    double x = blockPos.getX();
                    double z = blockPos.getZ();
                    double playerX = mc.player.posX;
                    double playerZ = mc.player.posZ;
                    
                    if (isDiagonally) {
                        xMin = blockPos.getX() + 0.9;
                        xMax = blockPos.getX() + 1.0;
                        zMin = blockPos.getZ() + 0.9;
                        zMax = blockPos.getZ() + 1.0;
                    }
                    
                    if ((!isDiagonally && playerX >= xMin && playerX <= xMax && playerZ >= zMin && playerZ <= zMax) ||
                            (isDiagonally && ((playerX >= xMin && playerX <= xMax && playerZ >= zMin && playerZ <= zMax) ||
                                    (playerX >= x && playerX <= x + 0.1 && playerZ >= z && playerZ <= z + 0.1) ||
                                    (playerX >= x && playerX <= x + 0.1 && playerZ >= zMin && playerZ <= zMax) ||
                                    (playerX >= xMin && playerX <= xMax && playerZ >= z && playerZ <= z + 0.1)))) {
                        ableSneak = true;
                    }
                    
                    if ((ableSneak || (!MoveUtils.isMoving()) || (InventoryUtils.findBlockInHotBar(useLargestStack.getValue()) == -1)) && nearAir()) {
                        MoveHandler.setSneak(true, 20);
                    }
                    
                    if ((!isDiagonally && (playerX < xMin || playerX > xMax) && (playerZ < zMin || playerZ > zMax)) ||
                            (isDiagonally && !((playerX >= xMin || playerZ >= zMin) ||
                                    (playerX >= x && playerX <= x + 0.2 || playerZ >= z && playerZ <= z + 0.2) ||
                                    (playerX >= x && playerX <= x + 0.2 || playerZ >= zMin && playerZ <= zMax) ||
                                    (playerX >= xMin && playerX <= xMax || playerZ >= z && playerZ <= z + 0.2)))) {
                        ableSneak = false;
                    }
                }
                break;
            case "Normal":
            case "Snap":
                break;
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
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        towerStatus = mc.gameSettings.keyBindJump.isKeyDown() &&
                (!towerNoMove.getValue() || !MoveUtils.isMoving());

        if (placeInfo != null) {
            if ((event.isPre() && placeTiming.is("Pre")) || (event.isPost() && placeTiming.is("Post"))) {
                PlaceInfo targetInfo = null;
                RayTraceResult rayTraceResult = performBlockRaytrace(RotationHandler.getCurrentRotation());
                if (clickCheck.is("None")) {
                    targetInfo = placeInfo;
                } else {
                    if (rayTraceResult != null) {
                        switch (clickCheck.getValueByName()) {
                            case "None":
                                break;
                            case "Simple":
                                if (rayTraceResult.getBlockPos().equals(placeInfo.blockPos)) {
                                    targetInfo = new PlaceInfo(rayTraceResult.getBlockPos(), placeInfo.facing, placeInfo.hitVec, placeInfo.rotation);
                                }
                                break;
                            case "Strict":
                                if (rayTraceResult.getBlockPos().equals(placeInfo.blockPos) && rayTraceResult.sideHit == placeInfo.facing) {
                                    targetInfo = new PlaceInfo(rayTraceResult.getBlockPos(), rayTraceResult.sideHit, rayTraceResult.hitVec, placeInfo.rotation);
                                }
                                break;
                        }
                    }
                }
                EnumHand hand = mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                if ((!click(targetInfo) && extraClickTime.is("Legit")) || extraClickTime.is("AfterPlace")) {
                    if (extraClick.getValue()) {
                        if (canExtraClick() && clickTimes > 0) {
                            if (mc.player.getHeldItem(hand).getItem() instanceof ItemBlock) {
                                runExtraClick(hand);
                            }
                        }
                    }
                }
            }
        }
        
        if (towerStatus && allowJump.getValue()) {
            lastY = mc.player.posY;
        }
    }
    
    @EventHandler(priority = -1001)
    public void onCancelSprint(LivingUpdateEvent event) {
        if (sprintNotTowering.getValue() && towerStatus) {
            mc.player.setSprinting(false);
            mc.gameSettings.keyBindSprint.setPressed(false);
        }
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
            case "Ground":
                if (!mc.player.onGround) {
                    mc.player.setSprinting(false);
                    mc.gameSettings.keyBindSprint.setPressed(false);
                }
                break;
            case "Air":
                if (mc.player.onGround) {
                    mc.player.setSprinting(false);
                    mc.gameSettings.keyBindSprint.setPressed(false);
                }
                break;
        }
    }
    
    private BlockPos getOptimalPos() {
        BlockPos playerPos;
        if (mc.player.posY == round(mc.player.posY) + 0.5) {
            playerPos = new BlockPos((mc.player.posX), (mc.player.posY), (mc.player.posZ));
        } else {
            playerPos = new BlockPos((mc.player.posX), (mc.player.posY), (mc.player.posZ)).down();
        }
        if (sameY.getValue()) {
            if (((int) mc.player.posY - 1 > (int) lastY) || (allowJump.getValue() && GameSettings.isKeyDown(mc.gameSettings.keyBindJump))) {
                lastY = mc.player.posY;
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
        PlaceInfo dPlaceInfo = null;
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
            double dist = Math.abs(enumFacing.getAxis() == EnumFacing.Axis.X ? vec.xCoord : vec.zCoord);
            if (dist < distValue.getValue() && !mc.player.movementInput.sneak) {
                return null;
            }
        }
        rotation = RotationUtils.toRotation(center, mc.player);

        switch (rotationMode.getValueByName()) {
            case "None":
            case "Normal":
            case "Sexy":
                break;
            case "Advance":
            case "StaticGodBridge":
            case "StaticBack":
                rotation = staticRotation(RotationUtils.toRotation(center, mc.player));
                break;
        }

        if (stableRotation.getValue()) {
            rotation.yaw = round(rotation.yaw / 45) * 45;
        }

        if (!rayCastSearch.getValue()) {
            dPlaceInfo = new PlaceInfo(placePos, enumFacing.getOpposite(), center, rotation);
        } else {
            RayTraceResult serverRayTrace = performBlockRaytrace(RotationHandler.getRotation());
            if (serverRayTrace.getBlockPos().equals(placePos) && (!raycast || serverRayTrace.sideHit.equals(enumFacing.getOpposite()))) {
                dPlaceInfo = new PlaceInfo(serverRayTrace.getBlockPos(), enumFacing.getOpposite(), center, rotation);
            }
            RayTraceResult rotationRayTrace = performBlockRaytrace(rotation);
            if (rotationRayTrace.getBlockPos().equals(placePos) && (!raycast || rotationRayTrace.sideHit.equals(enumFacing.getOpposite()))) {
                dPlaceInfo = new PlaceInfo(rotationRayTrace.getBlockPos(), enumFacing.getOpposite(), center, rotation);
            }
        }

        return dPlaceInfo;
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
        
        PlaceInfo oldPlaceInfo = null;
        PlaceInfo newPlaceInfo;

        double d = RandomUtils.randomDouble(distancePro.getFirst(),distancePro.getSecond());
        double a = RandomUtils.randomDouble(anglePro.getFirst(),anglePro.getSecond());
        Vec3d oldRotationVec = new Vec3d(0,0,0);
        for (EnumFacing enumFacing : EnumFacing.VALUES) {
            BlockPos placePos = blockPos.offset(enumFacing);
            if (!BlockUtils.canBeClick(placePos)) continue;
            for (double x = 0.2; x <= 0.8; x += 0.1) {
                for (double y = 0.2; y <= 0.8; y += 0.1) {
                    for (double z = 0.2; z <= 0.8; z += 0.1) {
                        newPlaceInfo = getPlaceInfo(blockPos, placePos, new Vec3d(x, y, z), enumFacing, raycast);
                        if (newPlaceInfo == null) {
                            continue;
                        }
                        Vec3d newRotationVec = RayCastUtils.rayTraceWithCustomRotation(mc.player,mc.playerController.getBlockReachDistance(),newPlaceInfo.getRotation()).hitVec;
                        double distance = mc.player.getEyes().distanceTo(newRotationVec);
                        double angle = RotationUtils.getRotationDifference(newPlaceInfo.getRotation(), RotationHandler.getRotation());
                        if(oldPlaceInfo == null || (
                                (centerMode.is("Angle") && angle < RotationUtils.getRotationDifference(oldPlaceInfo.getRotation(), RotationHandler.getRotation()))
                                || (centerMode.is("Distance") && distance < mc.player.getEyes().distanceTo(oldRotationVec))
                                || (centerMode.is("Proportion") && (distance * d + angle * a) < (mc.player.getEyes().distanceTo(oldRotationVec) * d +
                                        RotationUtils.getRotationDifference(oldPlaceInfo.getRotation(), RotationHandler.getRotation()) * a))
                        )){
                            oldPlaceInfo = newPlaceInfo;
                            oldRotationVec = newRotationVec;
                        }
                    }
                }
            }
        }
        
        if (oldPlaceInfo == null) return false;
        
        this.placeInfo = oldPlaceInfo;
        if (rotationTiming.is("Normal")) {
            setRotation(placeInfo.rotation);
        }
        return true;
    }
    
    private void setRotation(Rotation rotation) {
        if (rotationMode.is("None")) return;
        
        float horizonSpeed = (float) RandomUtils.randomDouble(yawTurnSpeed.getFirst(), yawTurnSpeed.getSecond());
        float pitchSpeed = (float) RandomUtils.randomDouble(pitchTurnSpeed.getFirst(), pitchTurnSpeed.getSecond());
        
        int keepTicks = RandomUtils.randomInt((int) round(this.keepTicks.getFirst()), (int) round(this.keepTicks.getSecond()));
        int reverseTicks = RandomUtils.randomInt((int) round(this.backTicks.getFirst()), (int) round(this.backTicks.getSecond()));
        
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
    
    private boolean click(PlaceInfo placeInfo) {
        if(placeInfo == null)return false;

        BlockPos placePos = placeInfo.getBlockPos();
        EnumFacing facing = placeInfo.getFacing();
        Vec3d hitVec = placeInfo.hitVec;

        EnumHand hand = mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        
        ItemStack heldItem = mc.player.getHeldItem(hand);
        
        if (!(heldItem.getItem() instanceof ItemBlock)) {
            return false;
        }

        if(sneakCancelPlace.getValue() && mc.player.isSneaking()){
            return false;
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
            return false;
        }
        if (!Objects.requireNonNull(placePos.getState()).getBlock().canCollideCheck(placePos.getState(), false)) {
            placeTimer.reset();
            return false;
        }
        
        if (placeDelayMode.is("Normal")) {
            if (!placeTimer.hasTimeElapsed(currentPlaceDelay) && (!placeDelayNoTower.getValue() || !towerStatus)) {
                return false;
            }
        }
        if (mc.playerController.processRightClickBlock(mc.player, mc.world, placePos, facing, hitVec, hand) == EnumActionResult.SUCCESS) {
            if (swingMode.is("Vanilla")) {
                mc.player.swingArm(hand);
            } else if (swingMode.is("Packet")) {
                PacketUtils.sendPacket(new CPacketAnimation(hand));
            }
            placeTimer.reset();
            currentPlaceDelay = RandomUtils.randomInt((int) placeDelay.getFirst(), (int) placeDelay.getSecond());
            return true;
        }
        return false;
    }
    
    
    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (extraClickTimer.hasTimeElapsed(curExtraClickDelay) && canExtraClick() && clickTimes <= maxExtraClicks.getValue()) {
            extraClickTimer.reset();
            curExtraClickDelay = RandomUtils.randomInt((int) extraClickDelay.getFirst(), (int) extraClickDelay.getSecond());
            clickTimes++;
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
