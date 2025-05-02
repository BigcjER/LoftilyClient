package loftily.settings;

import loftily.Client;
import loftily.file.impl.ClientSettingsConfig;

public class ClientSettings {
    public static final FieldProxy<String> lastConfig = new FieldProxy<>(
            "default.json",
            (oldVal, newVal) -> save()
    );
    
    public static final FieldProxy<Boolean> isDarkMode = new FieldProxy<>(
            false,
            (oldVal, newVal) -> save()
    );
    
    private static void save() {
        Client.INSTANCE.getConfigManager().get(ClientSettingsConfig.class).write();
    }
}