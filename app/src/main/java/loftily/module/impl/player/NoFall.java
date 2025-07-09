package loftily.module.impl.player;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.ClassUtils;
import loftily.utils.player.PlayerUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;

@ModuleInfo(name = "NoFall", category = ModuleCategory.PLAYER)
public class NoFall extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            ClassUtils.getModes(getClass().getPackage().getName() + ".nofalls")
    );
    private final NumberValue fallDistance = new NumberValue("MinFallDistance", 3, 0, 8, 0.01);
    private final BooleanValue noVoid = new BooleanValue("NoVoid", true);
    
    public boolean fallDamage() {
        if (!PlayerUtils.nullCheck()) {
            return false;
        }
        
        return mc.player.fallDistance - mc.player.motionY > fallDistance.getValue();
    }
    
    public boolean inVoidCheck() {
        if (!noVoid.getValue()) {
            return true;
        } else {
            return !PlayerUtils.isInVoid();
        }
    }
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
