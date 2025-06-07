package loftily.event.impl.player;


import loftily.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;

@Getter
@AllArgsConstructor
public class AttackEvent extends CancellableEvent {
    private Entity target;
}
