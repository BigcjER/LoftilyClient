package loftily.module.impl.render;

import loftily.gui.animation.Easing;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.EasingModeValue;

@ModuleInfo(name = "ZoomModifier", category = ModuleCategory.Other)
public class ZoomModifier extends Module {
    public BooleanValue animation = new BooleanValue("AnimationZoom", true);
    public BooleanValue smoothCamera = new BooleanValue("SmoothCamera", true);
    public EasingModeValue zoomInEasing = new EasingModeValue("ZoomInEasing", Easing.EaseOutExpo, this);
    public EasingModeValue zoomOutEasing = new EasingModeValue("ZoomOutEasing", Easing.EaseOutExpo, this);
    public NumberValue zoomInEasingDuring = new NumberValue("ZoomInEasingDuring", 500, 100, 1000);
    public NumberValue zoomOutEasingDuring = new NumberValue("ZoomOutEasingDuring", 500, 100, 1000);
    public NumberValue zoomMultiplier = new NumberValue("ZoomMultiplier", 4, 1, 10, 0.1);
}
