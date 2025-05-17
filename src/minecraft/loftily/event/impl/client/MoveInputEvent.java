package loftily.event.impl.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MoveInputEvent {
    private float forward, strafe;
    private boolean jump, sneak;
    private double sneakMultiplier;
}
