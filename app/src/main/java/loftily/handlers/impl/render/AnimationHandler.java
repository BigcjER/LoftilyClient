package loftily.handlers.impl.render;

import loftily.event.impl.render.Render2DEvent;
import loftily.gui.animation.Animation;
import loftily.handlers.Handler;
import net.lenni0451.lambdaevents.EventHandler;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provide a handle class, to post render animation.
 * e.g., Scaffold counter's out animation after the scaffold is disabled.
 */
public class AnimationHandler extends Handler {
    private static final Map<Animation, Runnable> animations = new ConcurrentHashMap<>();
    
    public static void add(Animation animation, Runnable runnable) {
        animations.put(animation, runnable);
    }
    
    @EventHandler(priority = -100)
    public void onRender2D(Render2DEvent event) {
        Iterator<Map.Entry<Animation, Runnable>> iterator = animations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Animation, Runnable> entry = iterator.next();
            
            Animation animation = entry.getKey();
            Runnable runnable = entry.getValue();
            
            runnable.run();

            if (animation.isFinished()) {
                iterator.remove();
            }
        }
    }
}