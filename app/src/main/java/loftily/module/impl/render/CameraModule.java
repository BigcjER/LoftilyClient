package loftily.module.impl.render;

import loftily.event.impl.render.FovModifierEvent;
import loftily.event.impl.render.HurtCameraEvent;
import loftily.event.impl.world.WorldLoadEvent;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.EasingModeValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.settings.GameSettings;

@ModuleInfo(name = "Camera", category = ModuleCategory.RENDER)
public final class CameraModule extends Module {
    private final BooleanValue noHurtCamera = new BooleanValue("NoHurtCamera", true);
    //Zoom
    private final BooleanValue animation = new BooleanValue("AnimationZoom", true);
    private final EasingModeValue zoomInEasing = (EasingModeValue) new EasingModeValue("ZoomInEasing", Easing.EaseOutExpo, this)
            .setVisible(animation::getValue);
    private final EasingModeValue zoomOutEasing = (EasingModeValue) new EasingModeValue("ZoomOutEasing", Easing.EaseOutExpo, this)
            .setVisible(animation::getValue);
    private final NumberValue zoomInEasingDuring = new NumberValue("ZoomInEasingDuring", 500, 100, 1000)
            .setVisible(animation::getValue);
    private final NumberValue zoomOutEasingDuring = new NumberValue("ZoomOutEasingDuring", 500, 100, 1000)
            .setVisible(animation::getValue);
    private final NumberValue zoomMultiplier = new NumberValue("ZoomMultiplier", 4, 2, 10, 0.1)
            .setVisible(animation::getValue);
    
    
    private final Animation zoomAnimation = new Animation(Easing.EaseOutExpo, 1000);
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        zoomAnimation.setValue(1);
    }
    
    @EventHandler
    public void onZoom(FovModifierEvent event) {
        if (mc.currentScreen != null) {
            zoomAnimation.run(1);
            event.setZoomMultiplier(zoomAnimation.getValuef());
            return;
        }
        
        boolean in = GameSettings.isKeyDown(mc.gameSettings.ofKeyBindZoom);
        
        
        if (animation.getValue()) {
            zoomAnimation.setEasing(in ? zoomInEasing.getValueByEasing() : zoomOutEasing.getValueByEasing());
            zoomAnimation.setDuration(in ? zoomInEasingDuring.getValue().longValue() : zoomOutEasingDuring.getValue().longValue());
            zoomAnimation.run(in ? zoomMultiplier.getValue() : 1);
        } else {
            zoomAnimation.setValue(event.getZoomMultiplier());
        }
        
        
        event.setZoomMultiplier(zoomAnimation.getValuef());
    }
    
    
    @EventHandler
    public void onHurtCamera(HurtCameraEvent event) {
        if (noHurtCamera.getValue()) {
            event.setCancelled(true);
        }
    }
}
