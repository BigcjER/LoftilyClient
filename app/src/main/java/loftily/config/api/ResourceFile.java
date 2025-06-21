package loftily.config.api;

import java.io.File;

public abstract class ResourceFile extends ManagedFile {
    public ResourceFile(File file) {
        super(file);
    }
}
