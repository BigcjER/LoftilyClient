package loftily.event.impl.player;

import loftily.utils.math.Rotation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LookEvent {
    private Rotation rotation;
}
