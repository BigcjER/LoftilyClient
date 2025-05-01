package loftily.module.impl.other;

import loftily.event.impl.world.UpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.player.MoveUtils;
import net.lenni0451.lambdaevents.EventHandler;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Test",key = Keyboard.KEY_R,category = ModuleCategory.Other)
public class Test extends Module {
    @EventHandler
    public void onUpdate(UpdateEvent event){

    }
}
