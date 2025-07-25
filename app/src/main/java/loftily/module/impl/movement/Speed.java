package loftily.module.impl.movement;

import loftily.event.impl.player.motion.JumpEvent;
import loftily.module.AutoDisableType;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.ClassUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;
import net.lenni0451.lambdaevents.EventHandler;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Speed", key = Keyboard.KEY_V, autoDisable = AutoDisableType.FLAG, category = ModuleCategory.MOVEMENT)
public class Speed extends Module {
    public final BooleanValue alwaysSprint = new BooleanValue("AlwaysSprint", false);
    public final BooleanValue jumpFix = new BooleanValue("DoubleJumpFix", false);
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            ClassUtils.getModes(getClass().getPackage().getName() + ".speeds")
    );
    private boolean jumped;
    private int jumpTimes;
    
    @EventHandler
    public void onJump(JumpEvent event) {
        if (!jumpFix.getValue()) return;
        jumpTimes++;
        while (jumpTimes > 1) {
            event.setCancelled(true);
            jumpTimes = 0;
        }
    }
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
    
}
