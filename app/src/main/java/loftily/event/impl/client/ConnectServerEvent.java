package loftily.event.impl.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.multiplayer.ServerData;

@Getter
@Setter
@AllArgsConstructor
public class ConnectServerEvent {
    public ServerData serverData;
}
