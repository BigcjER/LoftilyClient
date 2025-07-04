package loftily.module.impl.render;

import loftily.event.impl.client.ClientTickEvent;
import loftily.event.impl.client.ShutDownEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Objects;

@ModuleInfo(name = "FullBright", category = ModuleCategory.RENDER)
public class FullBright extends Module {
    private final ModeValue mode = new ModeValue("Mode", "NightVision", this,
            new StringMode("NightVision"),
            new StringMode("Gamma"));
    
    private float prevGamma = -1F;
    
    @Override
    public void onEnable() {
        prevGamma = mc.gameSettings.gammaSetting;
    }
    
    @Override
    public void onDisable() {
        if (!(prevGamma == -1F)) {
            mc.gameSettings.gammaSetting = prevGamma;
            prevGamma = -1F;
        }
        
        mc.player.removePotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("night_vision")));
    }
    
    @EventHandler
    public void onUpate(UpdateEvent event) {
        switch (mode.getValueByName().toLowerCase()) {
            case "gamma":
                mc.gameSettings.gammaSetting = 100F;
                break;
            case "nightvision":
                mc.player.addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("night_vision")),1000));
                break;
        }
    }
    
    @EventHandler
    public void onShutDown(ShutDownEvent event) {
        onDisable();
    }
}
