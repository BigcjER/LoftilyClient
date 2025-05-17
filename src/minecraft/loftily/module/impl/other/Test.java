package loftily.module.impl.other;

import loftily.gui.animation.Easing;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.MultiBooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.EasingModeValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;

@ModuleInfo(name = "Test", category = ModuleCategory.Other)
public class Test extends Module {
    private final EasingModeValue easingModeValue = new EasingModeValue("TestEasingModes", Easing.EaseOutExpo, this);
    private final ModeValue modeValue = new ModeValue("TestModeValue", "TestMode1", this,
            new TestMo1("TestMode1"),
            new TestMo2("TestMode2"),
            new TestMo3("TestMode3"),
            new StringMode("TestStringMode"));
    private final NumberValue numberValue = new NumberValue("IntNumberValue", 10, 0, 100);
    private final NumberValue numberValue2 = new NumberValue("NumberValue", 1, 0.01, 10, 0.01);
    private final MultiBooleanValue multiBooleanValue = new MultiBooleanValue("TestMultiBoolean")
            .add("Test1", true)
            .add("Test2", false)
            .add("Test3", false)
            .add("Test4", true)
            .add("Test5", true);
    public BooleanValue booleanValue = new BooleanValue("TestBoolean", true);
    
}
