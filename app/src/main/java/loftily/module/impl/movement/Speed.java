package loftily.module.impl.movement;

import loftily.module.AutoDisableType;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Speed", key = Keyboard.KEY_V, autoDisable = AutoDisableType.FLAG, category = ModuleCategory.MOVEMENT)
public class Speed extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            ModeValue.getModes(getClass().getPackage().getName() + ".speeds")
    );
    public final BooleanValue alwaysSprint = new BooleanValue("AlwaysSprint", false);
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
    
}
