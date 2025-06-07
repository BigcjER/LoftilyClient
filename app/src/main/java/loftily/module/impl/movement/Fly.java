package loftily.module.impl.movement;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.mode.ModeValue;

@ModuleInfo(name = "Fly",category = ModuleCategory.MOVEMENT)
public class Fly extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            ModeValue.getModes(getClass().getPackage().getName() + ".flys")
    );
}
