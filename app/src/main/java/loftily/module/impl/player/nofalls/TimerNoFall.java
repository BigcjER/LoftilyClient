package loftily.module.impl.player.nofalls;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.player.NoFall;
import loftily.utils.client.PacketUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.play.client.CPacketPlayer;

public class TimerNoFall extends Mode<NoFall> {
    public TimerNoFall() {
        super("Timer");
    }
    private boolean timer = false;

    @Override
    public void onDisable(){
        mc.timer.timerSpeed = 1F;
        timer = false;
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if(event.isPost()) {
            return;
        }
        if (getParent().fallDamage() && getParent().inVoidCheck()){
            PacketUtils.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true));
            mc.timer.timerSpeed = 0.5F;
            timer = true;
            mc.player.fallDistance = 0;
        }else {
            if(timer) {
                mc.timer.timerSpeed = 1F;
                timer = false;
            }
        }
    }
}
