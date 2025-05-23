package loftily.event;

import lombok.Getter;
import lombok.Setter;
import net.lenni0451.lambdaevents.types.ICancellableEvent;

@Getter
@Setter
public abstract class CancellableEvent implements ICancellableEvent {
    private boolean cancelled = false;
}
