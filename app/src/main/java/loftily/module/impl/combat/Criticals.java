package loftily.module.impl.combat;


import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.mode.ModeValue;

@ModuleInfo(name = "Criticals", category = ModuleCategory.COMBAT)
public class Criticals extends Module {
    public final ModeValue mode = new ModeValue("Mode", "Jump", this,
            ModeValue.getModes(getClass().getPackage().getName() + ".criticals")
    );
    
    @Override
    public String getTag() {
        return mode.getValueByName();
    }
}
