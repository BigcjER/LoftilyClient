package loftily.config;


import loftily.Client;
import loftily.config.api.JsonConfig;
import loftily.core.AbstractManager;
import loftily.utils.other.FileUtils;

import java.io.File;

public class FileManager extends AbstractManager<JsonConfig> {
    public final static File ROOT_DIR = new File(Client.NAME);
    public final static File SKINS_CACHE_DIR = new File(ROOT_DIR, "caches");
    public final static File CONFIG_DIR = new File(ROOT_DIR, "configs");
    public final static File SOUND_DIR = new File(ROOT_DIR, "sounds");
    
    public static final File ENABLE_SOUND_FILE = new File(FileManager.SOUND_DIR, "module-enable.wav");
    public static final File DIABLE_SOUND_FILE = new File(FileManager.SOUND_DIR, "module-disable.wav");
    
    public FileManager() {
        super("impl", JsonConfig.class);
        if (!SKINS_CACHE_DIR.exists()) SKINS_CACHE_DIR.mkdirs();
        if (!ROOT_DIR.exists()) ROOT_DIR.mkdirs();
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        if (!SOUND_DIR.exists()) SOUND_DIR.mkdirs();
        
        try {
            if (!ENABLE_SOUND_FILE.exists())
                FileUtils.unpackFile(ENABLE_SOUND_FILE, "assets/minecraft/loftily/sound/module-enable.wav");
            
            if (!DIABLE_SOUND_FILE.exists())
                FileUtils.unpackFile(DIABLE_SOUND_FILE, "assets/minecraft/loftily/sound/module-disable.wav");
            
        } catch (Exception e) {
            //File not found and can't unpack file
            throw new RuntimeException(e);
        }
    }
    
    public void init() {
        this.getAll().forEach(JsonConfig::init);
    }
    
    public void saveAll() {
        this.getAll().forEach(JsonConfig::write);
    }
}
