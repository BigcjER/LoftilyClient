package loftily.module.impl.other;

import loftily.config.FileManager;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.other.SoundsUtils;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;

@ModuleInfo(name = "ToggleSound", category = ModuleCategory.OTHER)
public class ToggleSound extends Module {
    private final ModeValue toggleSoundMode = new ModeValue("Mode", "Custom", this,
            new StringMode("Custom"),
            new StringMode("ClickButton"),
            new StringMode("ClickWoodButton"));
    
    public void playToggleSound(boolean toggled) {
        if (!isToggled() || mc.player == null) return;
        
        switch (toggleSoundMode.getValueByName()) {
            case "Custom":
                if (toggled) {
                    SoundsUtils.playSound(FileManager.ENABLE_SOUND_FILE);
                } else {
                    SoundsUtils.playSound(FileManager.DIABLE_SOUND_FILE);
                }
                break;
            
            case "ClickButton":
                mc.world.playSound(
                        mc.player.getPosition(),
                        SoundEvents.UI_BUTTON_CLICK,
                        SoundCategory.MASTER,
                        1F, toggled ? 1 : 0.9f,
                        false);
                break;
            
            case "ClickWoodButton":
                mc.world.playSound(
                        mc.player.getPosition(),
                        SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON,
                        SoundCategory.MASTER,
                        1F, toggled ? 1 : 0.9f, false);
                break;
        }
    }
}
