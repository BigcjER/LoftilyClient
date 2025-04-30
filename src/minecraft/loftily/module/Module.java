package loftily.module;

import loftily.Client;
import loftily.core.AbstractModule;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Module extends AbstractModule {
    private ModuleCategory moduleCategory;
    private boolean toggled;
    @Setter
    private int key;

    public Module(){
        if(!this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            throw new RuntimeException(String.format("ModuleInfo not found in %s!",this.getClass().getSimpleName()));
        }
        ModuleInfo moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);
        if (moduleInfo != null) {
            this.name = moduleInfo.name();
            this.key = moduleInfo.key();
            this.moduleCategory = moduleInfo.category();
        }
    }

    public void toggle(){
        toggled = !toggled;
        if(toggled){
            Client.INSTANCE.getEventManager().register(this);
        } else {
            Client.INSTANCE.getEventManager().unregister(this);
        }
    }
}
