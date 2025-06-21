package loftily.config;


import loftily.Client;
import loftily.config.api.JsonConfig;
import loftily.core.AbstractManager;

import java.io.File;

public class FileManager extends AbstractManager<JsonConfig> {
    public final static File ROOT_DIR = new File(Client.NAME);
    public final static File SKINS_CACHE_DIR = new File(ROOT_DIR, "caches");
    public final static File CONFIG_DIR = new File(ROOT_DIR, "configs");
    
    public FileManager() {
        super("impl", JsonConfig.class);
        if (!SKINS_CACHE_DIR.exists()) SKINS_CACHE_DIR.mkdirs();
        if (!ROOT_DIR.exists()) ROOT_DIR.mkdirs();
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
    }
    
    public void init() {
        this.getAll().forEach(JsonConfig::init);
    }
    
    public void saveAll() {
        this.getAll().forEach(JsonConfig::write);
    }
}
