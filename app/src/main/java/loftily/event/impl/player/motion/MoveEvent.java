package loftily.event.impl.player.motion;

import loftily.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MoveEvent extends CancellableEvent {
    private double x, y, z;
}
