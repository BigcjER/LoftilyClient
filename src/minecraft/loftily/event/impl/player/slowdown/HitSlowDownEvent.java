package loftily.event.impl.player.slowdown;

import loftily.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HitSlowDownEvent extends CancellableEvent {
    private double motionXMultiplier, motionZMultiplier;
    
    @Override
    public boolean isCancelled() {
        return super.isCancelled() || (motionXMultiplier == 1 && motionZMultiplier == 1);
    }
}
