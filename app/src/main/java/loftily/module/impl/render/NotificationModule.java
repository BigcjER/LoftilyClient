package loftily.module.impl.render;

import loftily.Client;
import loftily.event.impl.render.DrawScreenEvent;
import loftily.event.impl.render.Render2DEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "Notification", category = ModuleCategory.RENDER, defaultToggled = true)
public class NotificationModule extends Module {
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (mc.currentScreen == null)
            Client.INSTANCE.getNotificationManager().renderNotifications();
    }
    
    @EventHandler
    public void onDrawScreen(DrawScreenEvent event) {
        Client.INSTANCE.getNotificationManager().renderNotifications();
    }
}
