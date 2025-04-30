package loftily.core;

import loftily.utils.client.MinecraftInstance;
import lombok.Getter;

@Getter
public abstract class AbstractModule implements MinecraftInstance {
    protected String name;
}
