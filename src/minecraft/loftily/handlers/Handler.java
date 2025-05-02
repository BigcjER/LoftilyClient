package loftily.handlers;

import loftily.utils.client.MinecraftInstance;
import lombok.Setter;

@Setter
public abstract class Handler implements MinecraftInstance {
    protected boolean needRegister() {
        return true;
    }
}
