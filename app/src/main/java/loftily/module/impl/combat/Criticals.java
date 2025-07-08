package loftily.module.impl.combat;


import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.ClassUtils;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;

@ModuleInfo(name = "Criticals", category = ModuleCategory.COMBAT)
public class Criticals extends Module {
    public final ModeValue mode = new ModeValue("Mode", "Jump", this,
            ClassUtils.getModes(getClass().getPackage().getName() + ".criticals")
    );
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
