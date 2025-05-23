package loftily.handlers;

import loftily.Client;
import loftily.core.AbstractManager;
import lombok.Getter;

@Getter
public class HandlerManager extends AbstractManager<Handler> {
    
    public HandlerManager() {
        super("impl", Handler.class);
        
        for (Handler handler : this) {
            if (handler.needRegister()) {
                Client.INSTANCE.getEventManager().register(handler);
            }
        }
    }
}
