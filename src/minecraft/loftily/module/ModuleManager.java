package loftily.module;

import loftily.Client;
import loftily.core.AbstractManager;
import loftily.event.impl.client.KeyboardEvent;
import loftily.value.Value;
import loftily.value.impl.mode.ModeValue;
import net.lenni0451.lambdaevents.EventHandler;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

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
                            module.getValues().add((Value<?, ?>) field.get(module));
                            ((ModeValue) field.get(module)).initModes();
                            continue;
                        }
                        
                        module.getValues().add((Value<?, ?>) field.get(module));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        this.stream().filter(Module::isDefaultToggled).forEach(module -> module.setToggled(true, false));
        this.sort((m1, m2) -> m1.getName().compareToIgnoreCase(m2.getName()));
        
        Client.INSTANCE.getEventManager().register(this);
    }
    
    @EventHandler(priority = 10)
    public void onKey(KeyboardEvent event) {
        this.stream()
                .filter(module -> module.getKey() == event.getKey())
                .forEach(Module::toggle);
    }
    
    public Module get(String moduleName) {
        return this.stream()
                .filter(module -> moduleName.equalsIgnoreCase(module.getName()))
                .findFirst()
                .orElse(null);
    }
    
    public List<Module> get(ModuleCategory category) {
        return this.stream()
                .filter(module -> module.getModuleCategory() == category)
                .collect(Collectors.toList());
    }
}
