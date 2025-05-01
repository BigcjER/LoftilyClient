package loftily.event.impl.player;

import loftily.event.Event;
import loftily.utils.math.Rotation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RotationEvent extends Event {
    private Rotation rotation;
}
