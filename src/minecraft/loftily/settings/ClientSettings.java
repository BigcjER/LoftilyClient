package loftily.settings;

import loftily.Client;
import loftily.config.impl.ClientSettingsConfig;

public class ClientSettings {
    public static final FieldProxy<String> lastModuleConfig = new FieldProxy<>(
            "default.json",
            (oldVal, newVal) -> save()
    );
    
    private static void save() {
        Client.INSTANCE.getConfigManager().get(ClientSettingsConfig.class).write();
    }
}