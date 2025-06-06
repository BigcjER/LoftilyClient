package loftily.gui.notification;

import loftily.Client;
import loftily.gui.animation.Animation;
import loftily.module.impl.render.NotificationModule;
import loftily.utils.client.ClientUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NotificationManager implements ClientUtils {
    private final ConcurrentLinkedQueue<Notification> notifications = new ConcurrentLinkedQueue<>();
    
    public void renderNotifications() {
        if (!Client.INSTANCE.getModuleManager().get(NotificationModule.class).isToggled() || notifications.isEmpty())
            return;
        
        ScaledResolution sr = new ScaledResolution(mc);
        
        Iterator<Notification> iterator = notifications.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Notification notification = iterator.next();
            Animation xAnim = notification.xAnimation;
            Animation yAnim = notification.yAnimation;
            notification.out = !notification.isFinished();
            //if(xAnim.isFinished()) out = false;
            
            xAnim.run(notification.out ? 1 : 0);
            yAnim.run(index * 60);
            notification.x = sr.getScaledWidth() - (notification.width * xAnim.getValuef());
            notification.y = sr.getScaledHeight() - 50 - yAnim.getValuef();
            index++;
            
            notification.drawNotification();
            
            if (!notification.out && notification.isFinished() && yAnim.isFinished() && xAnim.isFinished())
                iterator.remove();
        }
    }
    
    public void add(NotificationType type, String title, String message, long duration) {
        if (!Client.INSTANCE.getModuleManager().get(NotificationModule.class).isToggled()) return;
        Notification notification = new Notification(type, title, message, duration);
        notifications.add(notification);
    }
}
