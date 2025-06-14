package loftily.module.impl.other;

import loftily.module.AutoDisableType;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;

@SuppressWarnings("unused")
@ModuleInfo(name = "Test", category = ModuleCategory.OTHER, autoDisable = AutoDisableType.FLAG)
public class Test extends Module {
}
