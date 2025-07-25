package loftily.module.impl.movement;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.ClassUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;

@ModuleInfo(name = "LongJump", category = ModuleCategory.MOVEMENT)
public class LongJump extends Module {
    public final BooleanValue autoJump = new BooleanValue("AutoJump", true);
    private final ModeValue mode = new ModeValue("LongJumpMode", "Matrix", this,
            ClassUtils.getModes(getClass().getPackage().getName() + ".longjumps")
    );
    private final BooleanValue autoDisable = new BooleanValue("AutoDisable", true);
    
    public void jump() {
        if (!getAutoJump()) return;
        mc.player.tryJump();
    }
    
    public void autoDisable() {
        if (!getAutoDisable()) return;
        if (isToggled()) toggle();
    }
    
    public boolean getAutoJump() {
        return autoJump.getValue();
    }
    
    public boolean getAutoDisable() {
        return autoDisable.getValue();
    }
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
