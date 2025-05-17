package loftily.module.impl.movement;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.module.impl.movement.velocitys.NormalVelocity;
import loftily.value.impl.mode.ModeValue;

@ModuleInfo(name = "Velocity", category = ModuleCategory.Movement)
public class Velocity extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Normal", this,
            new NormalVelocity("Normal")
    );
}
