package loftily.module.impl.movement;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;

@ModuleInfo(name = "Velocity", category = ModuleCategory.MOVEMENT)
public class Velocity extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Normal", this,
            ModeValue.getModes(getClass().getPackage().getName() + ".velocitys")
    );
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
