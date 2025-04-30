package loftily.event.impl.client;

import loftily.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class KeyboardEvent extends CancellableEvent {
    private final int key;
}
