package loftily.module;

import loftily.Client;
import loftily.core.AbstractManager;
import loftily.event.impl.client.KeyboardEvent;
import loftily.gui.interaction.draggable.IDraggable;
import loftily.handlers.impl.render.DraggableHandler;
import loftily.value.Value;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.ModeValue;
import net.lenni0451.lambdaevents.EventHandler;

import java.lang.reflect.Field;
import java.util.*;

public class ModuleManager extends AbstractManager<Module> {
    private final Map<String, Module> nameToModuleMap = new HashMap<>();
    private final Map<Integer, List<Module>> keyToModuleMap = new HashMap<>();
    private final Map<ModuleCategory, List<Module>> categoryToModuleMap = new EnumMap<>(ModuleCategory.class);
    
    public ModuleManager() {
        super("impl", Module.class);
        
        for (Module module : getAll()) {
            //填充名字快速查找Map
            nameToModuleMap.put(module.getName().toLowerCase(), module);
            //填充Key快速查找Map
            if (module.getKey() != 0 && module.getKey() != -1) {
                keyToModuleMap.computeIfAbsent(module.getKey(), k -> new ArrayList<>()).add(module);
            }
            //填充Category快速查找Map
            categoryToModuleMap.computeIfAbsent(module.getModuleCategory(), k -> new ArrayList<>()).add(module);
            
            //添加IDraggable
            if (module instanceof IDraggable) DraggableHandler.getDraggableList().add((IDraggable) module);
            
            //添加Value
            Field[] fields = module.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (Value.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        //跳过带有Mode的Value,用自带的初始化方法
                        if (field.getType().isAssignableFrom(ModeValue.class)) {
                            module.getValues().add((Value<?, ?>) field.get(module));
                            ((ModeValue) field.get(module)).initModes();
                            continue;
                        }
                        
                        if (field.getType().isAssignableFrom(BooleanValue.class)) {
                            module.getValues().add((Value<?, ?>) field.get(module));
                            ((BooleanValue) field.get(module)).initMode();
                            continue;
                        }
                        
                        module.getValues().add((Value<?, ?>) field.get(module));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        
        //处理DefaultToggle
        this.getAll().stream()
                .filter(Module::isDefaultToggled)
                .forEach(module -> module.setToggled(true, false, false));
        
        Client.INSTANCE.getEventManager().register(this);
    }
    
    @EventHandler(priority = 10)
    public void onKey(KeyboardEvent event) {
        List<Module> modules = keyToModuleMap.get(event.getKey());
        if (modules != null) {
            modules.forEach(Module::toggle);
        }
    }
    
    public Module get(String moduleName) {
        return nameToModuleMap.get(moduleName.toLowerCase());
    }
    
    public List<Module> get(ModuleCategory category) {
        List<Module> modules = categoryToModuleMap.get(category);
        return modules != null ? Collections.unmodifiableList(modules) : Collections.emptyList();
    }
    
    public Map<Module, Integer> getAllKeyBinds() {
        Map<Module, Integer> binds = new HashMap<>();
        
        for (List<Module> modules : keyToModuleMap.values()) {
            for (Module module : modules) {
                binds.put(module, module.getKey());
            }
        }
        return binds;
    }
    
    public void handelUpdateModuleKeybind(Module module, int oldKey, int newKey) {
        if (oldKey != 0 && oldKey != -1) {
            List<Module> oldList = keyToModuleMap.get(oldKey);
            if (oldList != null) {
                oldList.remove(module);
                if (oldList.isEmpty()) {
                    keyToModuleMap.remove(oldKey);
                }
            }
        }
        
        if (newKey != 0 && newKey != -1) {
            keyToModuleMap.computeIfAbsent(newKey, k -> new ArrayList<>()).add(module);
        }
    }
    
    /**
     * @return 根据首字母排序后的不可修改List
     */
    @Override
    public List<Module> getAll() {
        List<Module> list = new ArrayList<>(instanceMap.values());
        list.sort(Comparator.comparing(Module::getName));
        
        return Collections.unmodifiableList(list);
    }
}
