package loftily.module.impl.movement;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.ClassUtils;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;

@ModuleInfo(name = "Fly", category = ModuleCategory.MOVEMENT)
public class Fly extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            ClassUtils.getModes(getClass().getPackage().getName() + ".flys")
    );
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
