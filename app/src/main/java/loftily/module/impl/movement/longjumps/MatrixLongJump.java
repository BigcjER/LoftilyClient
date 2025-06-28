package loftily.module.impl.movement.longjumps;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.module.impl.movement.LongJump;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public class MatrixLongJump extends Mode<LongJump> {
    private final NumberValue boostSpeed = new NumberValue("BoostSpeed", 1.97, -1.97, 1.97, 0.01);
    private boolean receivedFlag, canBoost, boosted;
    public MatrixLongJump() {
        super("Matrix");
    }

    @Override
    public void onDisable() {
        receivedFlag = false;
        canBoost = false;
    }

    @Override
    public void onEnable() {
        if (!mc.player.onGround && !boosted)
            canBoost = true;

        boosted = false;
    }

    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.player.onGround) {
            getParent().jump();
            canBoost = true;
            return;
        }

        if (canBoost) {
            MoveUtils.setSpeed(boostSpeed.getValue(), false);
            mc.player.motionY = 0.42;
            boosted = true;
        }

        if (receivedFlag && boosted) {
            getParent().autoDisable();
            if (!getParent().getAutoDisable()) onDisable();
        }
    }

    @EventHandler
    public void onPacket(PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) receivedFlag = true;
    }
}