package loftily.module.impl.movement.flys;

import loftily.event.impl.player.motion.MoveEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.module.impl.movement.Fly;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class TimerGlideFly extends Mode<Fly> {

    public TimerGlideFly() {
        super("TimerGlide");
    }

    private final NumberValue timerSpeed = new NumberValue("TimerSpeed",20,10,100,0.1);
    private final NumberValue speed = new NumberValue("Speed",0.02,0.0,0.05,0.01);
    private final BooleanValue customMotionY = new BooleanValue("CustomMotionY",false);
    private final NumberValue motionSpeed = new NumberValue("MotionSpeed",-0.01,-0.2,0.2,0.01).setVisible(customMotionY::getValue);
    private final BooleanValue smartHurt = new BooleanValue("SmartHurt",false);
    private final NumberValue flyTicks = new NumberValue("FlyTicks",900,800,1600).setVisible(smartHurt::getValue);

    private int ticks = 0;
    private boolean boost = false;

    @Override
    public void onEnable(){
        ticks = 0;
        boost = false;
    }

    @Override
    public void onDisable(){
        ticks = 0;
        boost = false;
        mc.timer.timerSpeed = 1;
    }

    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if(mc.player.hurtTime > 0 && !boost){
            if(mc.player.hurtTime <= 8) {
                mc.player.jump();
            }
            if(mc.player.offGroundTicks >= 4){
                boost = true;
            }
        }

        if(!smartHurt.getValue() || (ticks <= flyTicks.getValue() && boost)) {
            MoveUtils.setSpeed(speed.getValue(),false);
            mc.timer.timerSpeed = timerSpeed.getValue().floatValue();
            if(customMotionY.getValue()) {
                mc.player.motionY = motionSpeed.getValue().floatValue();
            }else {
                if(ticks <= 900){
                    mc.player.motionY *= 0.039;
                }else {
                    mc.player.motionY *= 0.045;
                }
            }
            ticks++;

        }
        if(ticks > flyTicks.getValue() && smartHurt.getValue()){
            getParent().toggle();
        }
    }
}
