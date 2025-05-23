package loftily.event.impl.player.motion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class StrafeEvent {
    private float strafe, up, forward, friction;
    private float yaw;
}
