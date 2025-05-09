package loftily;

import de.florianmichael.viamcp.ViaMCP;
import loftily.command.CommandManager;
import loftily.config.ConfigManager;
import loftily.gui.clickgui.ClickGui;
import loftily.module.ModuleManager;
import lombok.Getter;
import net.lenni0451.lambdaevents.LambdaManager;
import net.lenni0451.lambdaevents.generator.LambdaMetaFactoryGenerator;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

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
    
    private ModuleManager moduleManager;
    private LambdaManager eventManager;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private ClickGui clickGui;
    
    public void init() {
        eventManager = LambdaManager.basic(new LambdaMetaFactoryGenerator());
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();
        configManager.init();/* late init I think */
        commandManager = new CommandManager();
        clickGui = new ClickGui();
        
        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        
        Display.setTitle(getTitle());
    }
    
    public void shutdown() {
        Logger.info("Saving all configs");
        configManager.saveAll();
    }
    
    public String getTitle() {
        return String.format("%s %s", Name, Version);
    }
}
