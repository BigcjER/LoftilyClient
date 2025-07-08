package loftily.module.impl.movement;

import loftily.event.impl.world.PreUpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "NoWeb", category = ModuleCategory.MOVEMENT)
public class NoWeb extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            new StringMode("Matrix"),
            new StringMode("Vanilla"),
            new StringMode("Motion")
    );
    private final NumberValue motionSpeed = new NumberValue("MotionSpeed", 0.3, 0.0, 1.0, 0.01).setVisible(() -> mode.is("Motion"));
    
    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if (!mc.player.isInWeb) return;
        
        switch (mode.getValueByName()) {
            case "Motion":
                MoveUtils.setSpeed(motionSpeed.getValue(), true);
                break;
            case "Vanilla":
                mc.player.isInWeb = false;
                break;
            case "Matrix":
                MoveUtils.setSpeed(0.45, true);
                if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motionY = -1.98D;
                } else if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motionY = 1.98D;
                } else if (mc.player.onGround) {
                    mc.player.tryJump();
                } else {
                    mc.player.motionY = 0;
                }
                break;
        }
    }
}
