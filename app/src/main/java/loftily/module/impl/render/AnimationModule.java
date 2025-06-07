package loftily.module.impl.render;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.NumberValue;
import lombok.Getter;

@Getter
@ModuleInfo(name = "Animation", category = ModuleCategory.RENDER, canBeToggled = false)
public class AnimationModule extends Module {
    private final NumberValue animationDuringMultiplier = new NumberValue("AnimationDuringMultiplier", 1, 0, 3, 0.1F);
}
