package loftily.module.impl.movement.velocitys;

import loftily.Client;
import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.handlers.impl.player.RotationHandler;
import loftily.module.impl.combat.KillAura;
import loftily.module.impl.movement.Velocity;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.CalculateUtils;
import loftily.utils.player.MoveUtils;
import loftily.utils.player.RayCastUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumHand;

public class GrimVerticalVelocity extends Mode<Velocity> {
    public GrimVerticalVelocity() {
        super("GrimVertical");
    }

    public final NumberValue range = new NumberValue("Grim-Range",3.2,3,5,0.01);
    private final BooleanValue rayCast = new BooleanValue("Grim-RayCast",true);
    private final BooleanValue legit = new BooleanValue("Grim-Legit",true);
    private boolean received,attacked = false;

    @Override
    public void onDisable(){
        received = false;
        attacked = false;
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if(packet instanceof SPacketEntityVelocity){
            SPacketEntityVelocity velocity = (SPacketEntityVelocity) packet;
            if(velocity.getEntityID() == mc.player.getEntityId()){
                double speed = MoveUtils.getSpeed(velocity.getMotionX(),velocity.getMotionZ());
                if(speed > 1000){
                    received = true;
                    attacked = false;
                }
            }
        }
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if(!event.isPost())return;
        if (mc.player.hurtTime == 0) {
            received = attacked = false;
        }
        if (received && !attacked) {
            Entity entity = RayCastUtils.raycastEntity(5, RotationHandler.getCurrentRotation().yaw,RotationHandler.getCurrentRotation().pitch,true,entity1 -> entity1 instanceof EntityLivingBase);;
            double reduceXZ;
            if (entity == null && !rayCast.getValue()) {
                EntityLivingBase target = Client.INSTANCE.getModuleManager().get(KillAura.class).target;
                if (target != null && CalculateUtils.getClosetDistance(mc.player, target) <= 5) {
                    entity = Client.INSTANCE.getModuleManager().get(KillAura.class).target;
                }
            }

            if (entity == null || !entity.isEntityAlive()) {
                return;
            }

            boolean state = mc.player.serverSprintState;
            if (!state) {
                PacketUtils.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING),false);
            }
            int count = legit.getValue() ? 1 : 6;
            for (int i = 1; i <= count; i++) {
                PacketUtils.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND),false);
                CPacketUseEntity cPacketUseEntity = new CPacketUseEntity(entity);
                PacketUtils.sendPacket(cPacketUseEntity,false);
            }
            if (!state) {
                PacketUtils.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING),false);
            }
            attacked = true;
            reduceXZ = legit.getValue() ? 0.6D : 0.07776D;

            mc.player.motionX *= reduceXZ;
            mc.player.motionZ *= reduceXZ;
        }
    }
}
