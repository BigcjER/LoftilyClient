package loftily.module.impl.movement;

import loftily.Client;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.player.motion.PostJumpEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.module.AutoDisableType;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;

import java.util.Objects;

@ModuleInfo(name = "Speed", key = Keyboard.KEY_V, autoDisable = AutoDisableType.FLAG, category = ModuleCategory.MOVEMENT)
public class Speed extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            ModeValue.getModes(getClass().getPackage().getName() + ".speeds")
    );
    public final BooleanValue alwaysSprint = new BooleanValue("AlwaysSprint", false);
    public final BooleanValue jumpFix = new BooleanValue("DoubleJumpFix", false);
    private boolean jumped;
    private int jumpTimes;
    
    @EventHandler
    public void onJump(JumpEvent event) {
        if (!jumpFix.getValue()) return;
        jumpTimes++;
        while (jumpTimes > 1 && jumped){
            event.setCancelled(true);
            jumpTimes = 0;
        }
        jumped = true;
    }
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
    
}
