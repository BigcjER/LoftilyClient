package loftily.module.impl.combat;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.NumberValue;

@ModuleInfo(name = "HitBox", category = ModuleCategory.COMBAT)
public class HitBox extends Module {
    public final NumberValue expandSizeH = new NumberValue("HExpandSize", 0.1, -1.5, 1.5, 0.01);
    public final NumberValue expandSizeV = new NumberValue("VExpandSize", 0.1, -1.5, 1.5, 0.01);
}
