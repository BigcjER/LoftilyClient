package loftily.module.impl.other;

import loftily.Client;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ClickGui", key = Keyboard.KEY_RSHIFT, category = ModuleCategory.Other, canBeToggled = false)
public class ClickGuiModule extends Module {
    @Override
    public void onEnable() {
        mc.displayGuiScreen(Client.INSTANCE.getClickGui());
    }
}
