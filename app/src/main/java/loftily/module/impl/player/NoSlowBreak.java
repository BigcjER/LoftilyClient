package loftily.module.impl.player;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;

@ModuleInfo(name = "NoSlowBreak", category = ModuleCategory.PLAYER)
public class NoSlowBreak extends Module {
    public final BooleanValue noSlowAir = new BooleanValue("Air", false);
    public final BooleanValue noSlowWater = new BooleanValue("Water", false);
}
