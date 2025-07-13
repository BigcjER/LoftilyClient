package loftily.module.impl.movement;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.ClassUtils;
import loftily.value.impl.mode.ModeValue;

@ModuleInfo(name = "AirJump",category = ModuleCategory.MOVEMENT)
public class AirJump extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            ClassUtils.getModes(getClass().getPackage().getName() + ".airjumps")
    );
}
