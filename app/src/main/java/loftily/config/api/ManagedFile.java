package loftily.config.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
@AllArgsConstructor
public abstract class ManagedFile {
    protected File file;
    
    public abstract void read();
    
    public abstract void write();
    
    public void init() {
        if (file != null) {
            if (file.exists()) {
                read();
            } else {
                file.getParentFile().mkdirs();
                write();
            }
        }
    }
}
