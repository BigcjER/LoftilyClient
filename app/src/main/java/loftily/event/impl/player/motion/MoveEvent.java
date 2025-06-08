package loftily.event.impl.player.motion;

import loftily.event.CancellableEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

@Getter
@Setter
public class MoveEvent extends CancellableEvent {
    private double x, y, z;
    private Entity entity;
    private boolean safeWalk;
    
    public MoveEvent(double x, double y, double z, Entity entity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.entity = entity;
    }
}
