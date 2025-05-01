package loftily.event.impl.player;

import loftily.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MotionEvent extends Event {
    private final Type type;
    private double x, y, z;
    private boolean onGround;

    public MotionEvent(double x, double y, double z, boolean onGround) {
        this.type = Type.Pre;
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
    }

    public MotionEvent() {
        this.type = Type.Post;
    }

    public enum Type {
        Pre,
        Post
    }
}
