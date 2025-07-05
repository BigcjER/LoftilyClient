package loftily;

import de.florianmichael.viamcp.ViaMCP;
import loftily.alt.AltManager;
import loftily.command.CommandManager;
import loftily.config.FileManager;
import loftily.event.impl.client.ShutDownEvent;
import loftily.gui.clickgui.ClickGui;
import loftily.gui.notification.NotificationManager;
import loftily.handlers.HandlerManager;
import loftily.module.ModuleManager;
import loftily.utils.client.ClientUtils;
import lombok.Getter;
import net.lenni0451.lambdaevents.LambdaManager;
import net.lenni0451.lambdaevents.generator.LambdaMetaFactoryGenerator;
import net.minecraft.util.text.TextFormatting;

@Getter
public enum Client {
    INSTANCE;
    
    public static final String NAME = "Loftily";
    public static final String VERSION = "v1.0.0";
    public static final String STRING_PREFIX = String.format("%s%s%s",
            TextFormatting.YELLOW + "[",
            TextFormatting.DARK_AQUA + NAME,
            TextFormatting.YELLOW + "]");
    
    public static final boolean DEVELOPMENT_BUILD = false;
    
    private ModuleManager moduleManager;
    private LambdaManager eventManager;
    private FileManager fileManager;
    private CommandManager commandManager;
    private HandlerManager handlerManager;
    private NotificationManager notificationManager;
    private ClickGui clickGui;
    private AltManager altManager;
    
    public void init() {
        eventManager = LambdaManager.basic(new LambdaMetaFactoryGenerator());
        moduleManager = new ModuleManager();
        
        handlerManager = new HandlerManager();
        
        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        
        altManager = new AltManager();
        
        fileManager = new FileManager();
        fileManager.init();/* late init I think */
        
        commandManager = new CommandManager();
        
        clickGui = new ClickGui();
        notificationManager = new NotificationManager();
    }
    
    public void shutdown() {
        Client.INSTANCE.getEventManager().call(new ShutDownEvent());
        ClientUtils.LOGGER.info("Saving all configs");
        fileManager.saveAll();
    }
    
    public String getTitle() {
        return String.format("%s %s | %s",
                NAME,
                VERSION,
                DEVELOPMENT_BUILD ? "Development Build" : "Release");
    }
}
