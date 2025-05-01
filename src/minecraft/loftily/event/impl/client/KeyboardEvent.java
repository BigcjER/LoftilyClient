package loftily.event.impl.client;

import loftily.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KeyboardEvent extends CancellableEvent {
    private final int key;
}
