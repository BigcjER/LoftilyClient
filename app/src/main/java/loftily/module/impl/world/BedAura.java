package loftily.module.impl.world;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.player.motion.MoveEvent;
import loftily.event.impl.render.Render2DEvent;
import loftily.event.impl.world.PreUpdateEvent;
import loftily.handlers.impl.player.RotationHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.block.BlockUtils;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.Rotation;
import loftily.utils.player.RotationUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@ModuleInfo(name = "BedAura", category = ModuleCategory.WORLD)
public class BedAura extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this, new StringMode("Vanilla"),
            new StringMode("Matrix"));
    private final BooleanValue debug = new BooleanValue("Debug", false);
    private final NumberValue range = new NumberValue("Range", 3, 0, 8, 0.1);
    private final ModeValue breakMode = new ModeValue("BreakMode", "Normal", this, new StringMode("Normal"), new StringMode("Packet"));
    private final BooleanValue swing = new BooleanValue("Swing", true);
    private final BooleanValue rotationValue = new BooleanValue("Rotation", true);
    private final ModeValue moveFixMode = new ModeValue("MoveFixMode", "None", this,
            new StringMode("None"),
            new StringMode("Strict"),
            new StringMode("Silent"));
    private final DelayTimer delayTimer = new DelayTimer();
    private Rotation rotation;
    private BlockPos target = null;
    private boolean fuckMatrix = false;
    private boolean disableMatrix = false;
    private boolean stop = false;
    
    @Override
    public void onDisable() {
        fuckMatrix = false;
        disableMatrix = false;
        stop = false;
        delayTimer.reset();
        target = null;
        rotation = null;
    }
    
    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof CPacketPlayer) {
            if (mode.is("Matrix") && disableMatrix) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onMove(MoveEvent event) {
        if (mode.is("Matrix") && disableMatrix) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if (mode.is("Matrix") && fuckMatrix) {
            if (mc.player.fallDistance > 0F) {
                mc.player.motionX = 0.0;
                mc.player.motionY = 0.0;
                mc.player.motionZ = 0.0;
                disableMatrix = true;
            }
        }
    }
    
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (debug.getValue() && target != null) {
            mc.fontRendererObj.drawString("BlockDamage:" + mc.playerController.curBlockDamageMP, 500, 250, new Color(255, 255, 255, 255).getRGB());
        }
    }
    
    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if (stop) {
            if (delayTimer.hasTimeElapsed(250)) {
                delayTimer.reset();
                disableMatrix = false;
                fuckMatrix = false;
                stop = false;
            }
        }

        if (target != null) {
            Block block = mc.world.getBlockState(target).getBlock();
            if (block instanceof BlockBed) {
                if (mode.is("Matrix")) {
                    if (mc.playerController.curBlockDamageMP > 0.8) {
                        mc.playerController.curBlockDamageMP = 1F;
                    }
                    fuckMatrix = true;
                    if (mc.player.onGround) {
                        mc.player.tryJump();
                    }
                }
                if (swing.getValue()) {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                }
                if (rotationValue.getValue()) {
                    rotation = RotationUtils.getBlockRotation(target).fixedSensitivity(0);
                    RotationHandler.setClientRotation(rotation, 2, 2, moveFixMode.getValueByName());
                }
                switch (breakMode.getValueByName()) {
                    case "Normal":
                        mc.playerController.onPlayerDamageBlock(target, EnumFacing.DOWN);
                        break;
                    case "Packet":
                        PacketUtils.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, target, EnumFacing.DOWN), true);
                        PacketUtils.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, target, EnumFacing.DOWN));
                        break;
                }
                return;
            } else {
                target = null;
                rotation = null;
            }
        } else {
            if (!fuckMatrix) {
                delayTimer.reset();

                if (disableMatrix) {
                    disableMatrix = false;
                }
            }
            if (fuckMatrix) {
                stop = true;
            }
        }
        final List<BlockPos> searchBlock = BlockUtils.searchBlocks(range.getValue().intValue());
        searchBlock.sort(Comparator.comparingDouble(blockPos -> mc.player.getDistance(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5)));
        boolean foundTarget = false;

        for (BlockPos pos : searchBlock) {
            if (Objects.requireNonNull(pos.getState()).getBlock() instanceof BlockBed) {
                target = pos;
                foundTarget = true;
                break;
            }
        }
        if (!foundTarget) {
            target = null;
            rotation = null;
        }
    }
}
