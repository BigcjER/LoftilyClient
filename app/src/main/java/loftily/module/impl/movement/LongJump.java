package loftily.module.impl.movement;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;

@ModuleInfo(name = "LongJump",category = ModuleCategory.Movement)
public class LongJump extends Module {
    private final ModeValue mode = new ModeValue("LongJumpMode", "Matrix", this,
            ModeValue.getModes(getClass().getPackage().getName() + ".longjumps")
    );

    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
