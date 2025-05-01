package loftily.module.impl.other;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Test", key = Keyboard.KEY_R, category = ModuleCategory.Other)
public class Test extends Module {
}
