package loftily.module.impl.render;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.NumberValue;

@ModuleInfo(name = "MotionBlur", category = ModuleCategory.Render)
public class MotionBlur extends Module {
    public NumberValue motionBlurAmount = new NumberValue("Amount", 5, 1, 10);
}
