package loftily.module;

import loftily.Client;
import loftily.core.AbstractManager;
import loftily.event.impl.client.KeyboardEvent;
import net.lenni0451.lambdaevents.EventHandler;

public class ModuleManager extends AbstractManager<Module> {
    public ModuleManager() {
        super("impl", Module.class);

        Client.INSTANCE.getEventManager().register(this);
    }

    @EventHandler
    public void onKeyboard(KeyboardEvent event){
        for (Module module : this){
            if(module.getKey() == event.getKey()){
                module.toggle();
            }
        }
    }
}
