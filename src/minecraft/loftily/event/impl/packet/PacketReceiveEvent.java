package loftily.event.impl.packet;

import loftily.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;

@Getter
@Setter
@AllArgsConstructor
public class PacketReceiveEvent extends CancellableEvent {
    private final Packet<?> packet;
}
