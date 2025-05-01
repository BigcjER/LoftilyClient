package loftily.module;

import loftily.Client;
import loftily.core.AbstractManager;
import loftily.event.impl.client.KeyboardEvent;
import loftily.value.Value;
import loftily.value.impl.mode.ModeValue;
import net.lenni0451.lambdaevents.EventHandler;

import java.lang.reflect.Field;

public class ModuleManager extends AbstractManager<Module> {
    public ModuleManager() {
        super("impl", Module.class);

        for (Module module : this) {
            Field[] fields = module.getClass().getDeclaredFields();

            for (Field field : fields) {
                if (Value.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);

                        if (field.getType().isAssignableFrom(ModeValue.class)) {
                            module.getValues().add((Value<?>) field.get(module));
                            ((ModeValue) field.get(module)).initModes();
                            continue;
                        }

                        module.getValues().add((Value<?>) field.get(module));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Client.INSTANCE.getEventManager().register(this);
    }

    @EventHandler
    public void onKeyboard(KeyboardEvent event) {
        for (Module module : this) {
            if (module.getKey() == event.getKey()) {
                module.toggle();
            }
        }
    }
}
