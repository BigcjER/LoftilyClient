package loftily.module.impl.movement;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;

@ModuleInfo(name = "NoSlow", category = ModuleCategory.MOVEMENT)
public class NoSlow extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            ModeValue.getModes(getClass().getPackage().getName() + ".noslows")
    );
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
