package loftily;

import loftily.event.impl.client.KeyboardEvent;
import loftily.module.ModuleManager;
import lombok.Getter;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.lambdaevents.LambdaManager;
import net.lenni0451.lambdaevents.generator.LambdaMetaFactoryGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

@Getter
public enum Client {
    INSTANCE;

    public static final String Name = "Loftily";
    public static final String Version = "v0.1";
    public static final Logger Logger = LogManager.getLogger(Client.class);

    private ModuleManager moduleManager;
    private LambdaManager eventManager;

    public void init() {
        eventManager = LambdaManager.basic(new LambdaMetaFactoryGenerator());
        moduleManager = new ModuleManager();

        Display.setTitle(getTitle());
    }

    public String getTitle() {
        return String.format("%s %s",Name,Version);
    }
}
