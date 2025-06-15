package loftily.module.impl.render;


import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.play.server.SPacketTimeUpdate;

@ModuleInfo(name = "Ambience" , category = ModuleCategory.RENDER)
public class Ambience extends Module {
    private final NumberValue time = new NumberValue("Time",1145,1,24000);
    private final ModeValue weather = new ModeValue("Weather","Sun",this,
            new StringMode("Rain"),
            new StringMode("Sun"),
            new StringMode("Thunder"));

    @EventHandler
    public void onMotion(MotionEvent event) {
        mc.world.setWorldTime(time.getValue().intValue());

        switch (weather.getValueByName()){
            case "Sun":
                mc.world.setRainStrength(0f);
                mc.world.setThunderStrength(0f);
                break;
            case "Rain":
                mc.world.setRainStrength(1f);
                mc.world.setThunderStrength(0f);
                break;
            case "Thunder":
                mc.world.setThunderStrength(1f);
                mc.world.setRainStrength(0f);
                break;
        }
    }
    @EventHandler
    public void onReceivePacket(PacketReceiveEvent event){
        if(event.getPacket() instanceof SPacketTimeUpdate){
            event.setCancelled(true);
        }
    }
}
