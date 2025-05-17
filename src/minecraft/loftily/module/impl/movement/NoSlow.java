package loftily.module.impl.movement;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.module.impl.movement.noslows.VanillaNoSlow;
import loftily.value.impl.mode.ModeValue;

@ModuleInfo(name = "NoSlow", category = ModuleCategory.Movement)
public class NoSlow extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            new VanillaNoSlow("Vanilla"));
}
