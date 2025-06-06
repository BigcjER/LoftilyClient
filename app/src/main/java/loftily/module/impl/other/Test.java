package loftily.module.impl.other;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import net.minecraft.util.EnumHand;

@SuppressWarnings("unused")
@ModuleInfo(name = "Test", category = ModuleCategory.OTHER)
public class Test extends Module {
    
    @Override
    public void onDisable() {
        System.out.println(mc.player.getHeldItem(EnumHand.MAIN_HAND));
    }
}
