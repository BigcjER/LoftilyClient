package loftily.module.impl.movement.airjumps;

import loftily.event.impl.world.PreUpdateEvent;
import loftily.module.impl.movement.AirJump;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.settings.GameSettings;

public class VanillaAirJump extends Mode<AirJump> {
    public VanillaAirJump() {
        super("Vanilla");
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if(GameSettings.isKeyDown(mc.gameSettings.keyBindJump)){
            mc.player.onGround = true;
        }
    }
}
