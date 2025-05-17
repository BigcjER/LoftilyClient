package loftily.event.impl.player.slowdown;

import loftily.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ItemSlowDownEvent extends CancellableEvent {
    private float strafeMultiplier, forwardMultiplier;
    
    @Override
    public boolean isCancelled() {
        return super.isCancelled() || (strafeMultiplier == 1 && forwardMultiplier == 1);
    }
}
