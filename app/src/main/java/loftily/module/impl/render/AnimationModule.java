package loftily.module.impl.render;

import loftily.Client;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import lombok.Getter;

@Getter
@ModuleInfo(name = "Animation", category = ModuleCategory.RENDER, canBeToggled = false)
public class AnimationModule extends Module {
    private final NumberValue animationDuringMultiplier = new NumberValue("AnimationDuringMultiplier", 1, 0, 3, 0.1F);
    
    private final BooleanValue blockAnimation = new BooleanValue("BlockAnimation", true);
    private final BooleanValue swingAnimation1_8 = new BooleanValue("1.8SwingAnimation", true);
    
    private final BooleanValue hotbarAnimation = new BooleanValue("HotbarAnimation", true);
    
    public static AnimationModule getInstance() {
        return Client.INSTANCE.getModuleManager().get(AnimationModule.class);
    }
}
