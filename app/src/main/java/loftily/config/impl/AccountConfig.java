package loftily.config.impl;

import loftily.config.Config;
import loftily.config.ConfigManager;

import java.io.File;

public class AccountConfig extends Config {
    public AccountConfig() {
        super(new File(ConfigManager.rootDir, "Accounts.json"));
    }
    
    @Override
    public void read() {
    
    }
    
    @Override
    public void write() {
    
    }
}
