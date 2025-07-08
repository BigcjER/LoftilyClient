package loftily.module.impl.movement.speeds;

import loftily.Client;
import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.exploit.disablers.WatchDogMovementDisabler;
import loftily.module.impl.movement.Speed;
import loftily.utils.block.BlockUtils;
import loftily.utils.player.MoveUtils;
import loftily.utils.player.PlayerUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.math.BlockPos;

public class HypixelTick7Speed extends Mode<Speed> {
    public NumberValue friction = new NumberValue("FrictionMultiplier", 1.0, 1.0, 1.3, 0.01);
    
    public NumberValue speedSetting = new NumberValue("Speed", 2.0, 0.8, 1.2, 0.01);
    public BooleanValue liquidDisable = new BooleanValue("LiquidDisable", true);
    public BooleanValue sneakDisable = new BooleanValue("SneakDisable", true);
    public BooleanValue strafe = new BooleanValue("EnableDirectionStrafe", false);
    public NumberValue strafeDegrees = new NumberValue("StrafeDegrees", 80.0, 50.0, 90.0, 5.0);
    public BooleanValue disablerOnly = new BooleanValue("RequireDisabler", false);
    private boolean ldmg;
    
    public HypixelTick7Speed() {
        super("Hypixel7TickLowHop");
    }
    
    public static boolean noSlowingBackWithBow() {
        return Client.INSTANCE.getModuleManager().get("NoSlow").isToggled() && mc.player.isHandActive() && bowBackwards();
    }
    
    public static boolean holdingBow() {
        return !mc.player.getHeldItemMainhand().isEmptyStack() && mc.player.getHeldItemMainhand().getItem() instanceof ItemBow;
    }
    
    public static boolean bowBackwards() {
        return holdingBow() && mc.player.moveStrafing == 0.0F && mc.player.moveForward <= 0.0F && MoveUtils.isMoving();
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if ((!mc.player.isInWater() && !mc.player.isInLava() || !liquidDisable.getValue())
                && (!mc.player.isSneaking() || !sneakDisable.getValue())) {
            if (true/*!Client.INSTANCE.getModuleManager().get("Scaffold").isToggled() && !scaffold.lowhop*/) { // TODO scaffold lowhop
                if (!Client.INSTANCE.getModuleManager().get("LongJump").isToggled()) {
                    if (!(Client.INSTANCE.getModuleManager().get("InvMove").isToggled() && mc.currentScreen instanceof GuiContainer)) {
                        handleLowHop();
                        if (mc.player.onGround && MoveUtils.isMoving()) {
                            mc.player.jump();
                            
                            double speed = speedSetting.getValue() - 0.52;
                            double speedModifier = speed;
                            int speedAmplifier = MoveUtils.getSpeedAmplifier();
                            switch (speedAmplifier) {
                                case 1:
                                    speedModifier = speed + 0.02;
                                    break;
                                case 2:
                                    speedModifier = speed + 0.04;
                                    break;
                                case 3:
                                    speedModifier = speed + 0.1;
                            }
                            
                            if (MoveUtils.isMoving()) {
                                if (!noSlowingBackWithBow()) {
                                    MoveUtils.setSpeed(speedModifier * applyFrictionMulti(), false);
                                } else {
                                    MoveUtils.setSpeed(speedModifier - 0.3, false);
                                }
                                
                            }
                        }
                        
                        if (strafe.getValue()) {
                            airStrafe();
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onReceivePacketsAll(PacketReceiveEvent e) {
        if (PlayerUtils.nullCheck()) {
            if (!e.isCancelled()) {
                if (e.getPacket() instanceof SPacketEntityVelocity && ((SPacketEntityVelocity) e.getPacket()).getEntityID() == mc.player.getEntityId()) {
                    ldmg = true;
                }
            }
        }
    }
    
    private void handleLowHop() {
        Block block = BlockUtils.getBlock(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ));
        int simpleY = (int) Math.round(mc.player.posY % 1.0 * 10000.0);
        if (!disablerOnly.getValue() || WatchDogMovementDisabler.disablerLoaded) {
            if (mc.player.isCollidedVertically || ldmg || block instanceof BlockSlab) {
                return;
            }
            
            switch (simpleY) {
                case 1138:
                    mc.player.motionY -= 0.13;
                    break;
                case 2031:
                    mc.player.motionY -= 0.2;
                    break;
                case 4200:
                    mc.player.motionY = 0.39;
            }
        }
        
        
        ldmg = false;
    }
    
    private void airStrafe() {
        if (!mc.player.onGround && mc.player.hurtTime < 3 && (mc.player.motionX != 0.0 || mc.player.motionZ != 0.0)) {
            float moveDir = moveDirection(mc.player.rotationYaw);
            float currentMotionDir = strafeDirection();
            float diff = Math.abs(moveDir - currentMotionDir);
            int range = strafeDegrees.getValue().intValue();
            if (diff > 180 - range && diff < 180 + range) {
                mc.player.motionX = -(mc.player.motionX * 0.85);
                mc.player.motionZ = -(mc.player.motionZ * 0.85);
            }
        }
    }
    
    private float moveDirection(float rawYaw) {
        float yaw = (rawYaw % 360.0F + 360.0F) % 360.0F > 180.0F ? (rawYaw % 360.0F + 360.0F) % 360.0F - 360.0F : (rawYaw % 360.0F + 360.0F) % 360.0F;
        float forward = 1.0F;
        if (mc.player.moveForward < 0.0F) {
            yaw += 180.0F;
        }
        
        if (mc.player.moveForward < 0.0F) {
            forward = -0.5F;
        }
        
        if (mc.player.moveForward > 0.0F) {
            forward = 0.5F;
        }
        
        if (mc.player.moveStrafing > 0.0F) {
            yaw -= 90.0F * forward;
        }
        
        if (mc.player.moveStrafing < 0.0F) {
            yaw += 90.0F * forward;
        }
        
        return yaw;
    }
    
    private float strafeDirection() {
        float yaw = (float) Math.toDegrees(Math.atan2(-mc.player.motionX, mc.player.motionZ));
        if (yaw < 0.0F) {
            yaw += 360.0F;
        }
        
        return yaw;
    }
    
    public double applyFrictionMulti() {
        int speedAmplifier = MoveUtils.getSpeedAmplifier();
        return speedAmplifier > 1 ? friction.getValue() : 1.0;
    }
}
