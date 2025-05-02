package loftily.file;


import loftily.Client;
import loftily.core.AbstractManager;

import java.io.File;

public class ConfigManager extends AbstractManager<Config> {
    public static File rootDir = new File(Client.Name);
    public static File configDir = new File(Client.Name, "/configs");
    public static File themeDir = new File(Client.Name, "/themes");
    
    public ConfigManager() {
        super("impl", Config.class);
        if (!rootDir.exists()) rootDir.mkdirs();
        if (!configDir.exists()) configDir.mkdirs();
        if (!themeDir.exists()) themeDir.mkdirs();
    }
    
    public void init() {
        this.stream().sorted().forEach(Config::init);
    }
    
    public void saveAll() {
        this.forEach(Config::write);
    }
}
