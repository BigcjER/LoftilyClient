package loftily;

import de.florianmichael.viamcp.ViaMCP;
import loftily.alt.AltManager;
import loftily.command.CommandManager;
import loftily.config.ConfigManager;
import loftily.gui.clickgui.ClickGui;
import loftily.gui.menu.SplashScreen;
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
    
    public static final String Name = "Loftily";
    public static final String Version = "v0.1";
    public static final String StringPreFix = String.format("%s%s%s",
            TextFormatting.YELLOW + "[",
            TextFormatting.DARK_AQUA + Name,
            TextFormatting.YELLOW + "]");
    
    public static final boolean DevelopmentBuild = true;
    
    private ModuleManager moduleManager;
    private LambdaManager eventManager;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private HandlerManager handlerManager;
    private NotificationManager notificationManager;
    private ClickGui clickGui;
    private AltManager altManager;
    
    public void init() {
        long start = System.currentTimeMillis();
        ClientUtils.Logger.info("Initializing {}...", Name);
        
        SplashScreen.INSTANCE.setProgressAndDraw("Initializing Modules", 40);
        eventManager = LambdaManager.basic(new LambdaMetaFactoryGenerator());
        moduleManager = new ModuleManager();
        
        SplashScreen.INSTANCE.setProgressAndDraw("Initializing Handlers", 50);
        handlerManager = new HandlerManager();
        
        SplashScreen.INSTANCE.setProgressAndDraw("Initializing ViaMCP", 60);
        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        
        SplashScreen.INSTANCE.setProgressAndDraw("Initializing Configuration Items", 75);
        configManager = new ConfigManager();
        configManager.init();/* late init I think */
        commandManager = new CommandManager();
        altManager = new AltManager();
        
        SplashScreen.INSTANCE.setProgressAndDraw("Initializing GUI", 90);
        clickGui = new ClickGui();
        notificationManager = new NotificationManager();
        
        SplashScreen.INSTANCE.setProgressAndDraw("Initialization Complete", 100);
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() - time <= 500) ;//waiting for 0.5s
        
        ClientUtils.Logger.info("Initialization completed, took {} ms.", (System.currentTimeMillis() - start));
    }
    
    public void shutdown() {
        ClientUtils.Logger.info("Saving all configs");
        configManager.saveAll();
    }
    
    public String getTitle() {
        return String.format("%s %s%s",
                Name,
                Version,
                DevelopmentBuild ? " | Development Build" : "");
    }
}
