package loftily.handlers.impl.client;

import loftily.Client;
import loftily.event.impl.client.ClientTickEvent;
import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.world.WorldLoadEvent;
import loftily.gui.notification.NotificationType;
import loftily.handlers.Handler;
import loftily.module.AutoDisableType;
import loftily.module.Module;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

//TODO:Game End Check
public class AutoDisableHandler extends Handler {
    private boolean loadingWorld;
    
    @EventHandler
    public void onClientTick(ClientTickEvent event) {
    
    }
    
    @EventHandler
    public void onPacket(PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            if (loadingWorld) {
                loadingWorld = false;
                return;
            }
            autoDisable(AutoDisableType.FLAG);
        }
    }
    
    @EventHandler
    public void onWorld(WorldLoadEvent event) {
        loadingWorld = true;
        autoDisable(AutoDisableType.WORLD_CHANGE);
    }
    
    private void autoDisable(AutoDisableType autoDisableType) {
        String text = "";
        
        switch (autoDisableType) {
            case NONE:
                break;
            case FLAG:
                text = "Disabled %s due to a flag.";
                break;
            case WORLD_CHANGE:
                text = "Disabled %s due to a world change.";
                break;
            case GAME_END:
                text = "Disabled %s due to the game ending.";
                break;
        }
        
        for (Module module : Client.INSTANCE.getModuleManager().getAll()) {
            if (!module.isToggled() || module.getAutoDisableType() == AutoDisableType.NONE) continue;
            
            if (module.getAutoDisableType() == autoDisableType) {
                module.setToggled(false, true, false);
                
                if (!text.isEmpty())
                    Client.INSTANCE.getNotificationManager().add(NotificationType.WARING, "ModuleManager", String.format(text, module.getName()), 0);
            }
        }
    }
}
