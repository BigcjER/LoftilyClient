package loftily.module.impl.world;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.ClassUtils;
import loftily.utils.player.PlayerUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;

@ModuleInfo(name = "AntiVoid", category = ModuleCategory.WORLD)
public class AntiVoid extends Module {
    private final ModeValue mode = new ModeValue("Mode", "PacketFlag", this,
            ClassUtils.getModes(getClass().getPackage().getName() + ".antivoids")
    );
    
    private final BooleanValue voidOnly = new BooleanValue("VoidOnly", true);
    private final NumberValue fallDistance = new NumberValue("MaxFallDistance", 5, 0, 20);
    
    public boolean isSafe() {
        return !(mc.player.fallDistance >= fallDistance.getValue()) || (voidOnly.getValue() && !PlayerUtils.isInVoid());
    }
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
