package loftily.module.impl.movement.flys;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.movement.Fly;
import loftily.utils.player.MoveUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;

import static java.lang.Math.*;

public class ClipFly extends Mode<Fly> {
    public ClipFly() {
        super("Clip");
    }

    private final NumberValue xValue = new NumberValue("X",0,-10,10,0.01);
    private final NumberValue yValue = new NumberValue("Y",0,-10,10,0.01);
    private final NumberValue zValue = new NumberValue("Z",0,-10,10,0.01);
    private final NumberValue delay = new NumberValue("Delay",0,0,5000);
    private final NumberValue timerValue = new NumberValue("Timer",1,0.1,3.0);
    private final BooleanValue resetMotion = new BooleanValue("ResetMotion",false);
    private final BooleanValue spoofGround = new BooleanValue("SpoofGround",false);
    private final BooleanValue spoofGroundOnlyClip = new BooleanValue("SpoofGroundOnlyClip",false).setVisible(spoofGround::getValue);

    private final DelayTimer timer = new DelayTimer();
    private boolean clipped = false;

    @Override
    public void onToggle(){
        clipped = false;
        timer.reset();
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if(packet instanceof CPacketPlayer){
            if(spoofGround.getValue()){
                if(!spoofGroundOnlyClip.getValue() || clipped){
                    ((CPacketPlayer) packet).onGround = true;
                    clipped = false;
                }
            }
        }
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if(event.isPost()){
            mc.timer.timerSpeed = timerValue.getValue().floatValue();
            if (timer.hasTimeElapsed(delay.getValue().intValue())) {
                double yaw = Math.toRadians(mc.player.rotationYaw);
                mc.player.setPosition(mc.player.posX + (-sin(yaw) * xValue.getValue()), mc.player.posY + yValue.getValue(), mc.player.posZ + (cos(yaw) * zValue.getValue()));
                timer.reset();
                clipped = true;
            }
            if(resetMotion.getValue()){
                MoveUtils.stop(false);
            }
        }
    }
}
