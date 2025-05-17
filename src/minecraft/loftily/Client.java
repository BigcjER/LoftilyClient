package loftily;

import de.florianmichael.viamcp.ViaMCP;
import loftily.command.CommandManager;
import loftily.config.ConfigManager;
import loftily.gui.clickgui.ClickGui;
import loftily.gui.menu.SplashScreen;
import loftily.handlers.HandlerManager;
import loftily.module.ModuleManager;
import lombok.Getter;
import net.lenni0451.lambdaevents.LambdaManager;
import net.lenni0451.lambdaevents.generator.LambdaMetaFactoryGenerator;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public enum Client {
    INSTANCE;
    
    public static final String Name = "Loftily";
    public static final String Version = "v0.1";
    public static final Logger Logger = LogManager.getLogger(Client.class);
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
    private ClickGui clickGui;
    
    public void init() {
        long start = System.currentTimeMillis();
        Logger.info("Initializing {}...", Name);
        
        SplashScreen.INSTANCE.setProgressAndDraw("Event Manager", 40);
        eventManager = LambdaManager.basic(new LambdaMetaFactoryGenerator());
        
        SplashScreen.INSTANCE.setProgressAndDraw("Module Manager", 45);
        moduleManager = new ModuleManager();
        
        SplashScreen.INSTANCE.setProgressAndDraw("Handlers", 55);
        handlerManager = new HandlerManager();
        
        SplashScreen.INSTANCE.setProgressAndDraw("ViaMCP", 60);
        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        
        SplashScreen.INSTANCE.setProgressAndDraw("Configs", 80);
        configManager = new ConfigManager();
        configManager.init();/* late init I think */
        
        SplashScreen.INSTANCE.setProgressAndDraw("Commands", 90);
        commandManager = new CommandManager();
        
        SplashScreen.INSTANCE.setProgressAndDraw("Click Gui", 95);
        clickGui = new ClickGui();
        
        SplashScreen.INSTANCE.setProgressAndDraw("Completed", 100);
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() - time <= 1000) ;
        
        Logger.info("Initialization completed, took {} ms.", (System.currentTimeMillis() - start));
    }
    
    public void shutdown() {
        Logger.info("Saving all configs");
        configManager.saveAll();
    }
    
    public String getTitle() {
        return String.format("%s %s%s",
                Name,
                Version,
                DevelopmentBuild ? " | Development Build" : "");
    }
}
