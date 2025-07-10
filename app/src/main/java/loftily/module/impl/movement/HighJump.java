package loftily.module.impl.movement;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.ClassUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.ModeValue;

@ModuleInfo(name = "HighJump", category = ModuleCategory.MOVEMENT)
public class HighJump extends Module {
    @SuppressWarnings("unused")
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            ClassUtils.getModes(getClass().getPackage().getName() + ".highjumps")
    );
    
    private final BooleanValue autoDisable = new BooleanValue("AutoDisable", true);
    
    public void autoDisable() {
        if (!autoDisable.getValue()) return;
        if (isToggled()) toggle();
    }
}
