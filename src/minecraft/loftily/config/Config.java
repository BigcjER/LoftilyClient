package loftily.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public abstract class Config implements Comparable<Config> {
    
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected File configFile;
    
    public Config(File configFile) {
        this.configFile = configFile;
    }
    
    public abstract void read();
    
    public abstract void write();
    
    public void init() {
        if (configFile != null)
            if (configFile.exists()) read();
            else write();
    }
    
    protected int getPriority() {
        return 0;
    }
    
    @Override
    public int compareTo(Config other) {
        return Integer.compare(other.getPriority(), this.getPriority());
    }
    
}
