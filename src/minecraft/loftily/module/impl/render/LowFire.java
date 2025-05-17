package loftily.module.impl.render;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.NumberValue;

@ModuleInfo(name = "LowFire", category = ModuleCategory.Render)
public class LowFire extends Module {
    public final NumberValue fireYOffset = new NumberValue("FireYOffset", 0.5F, 0.3F, 0.7F, 0.1F);
}
