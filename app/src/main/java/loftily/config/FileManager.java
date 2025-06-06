package loftily.config;


import loftily.Client;
import loftily.core.AbstractManager;

import java.io.File;

public class FileManager extends AbstractManager<Config> {
    public final static File RootDir = new File(Client.NAME);
    public final static File SkinsCacheDir = new File(RootDir, "caches");
    public final static File ConfigDir = new File(RootDir, "configs");
    
    public FileManager() {
        super("impl", Config.class);
        if (!SkinsCacheDir.exists()) SkinsCacheDir.mkdirs();
        if (!RootDir.exists()) RootDir.mkdirs();
        if (!ConfigDir.exists()) ConfigDir.mkdirs();
    }
    
    public void init() {
        this.stream().sorted().forEach(Config::init);
    }
    
    public void saveAll() {
        this.forEach(Config::write);
    }
}
