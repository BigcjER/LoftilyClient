package loftily.core;

import loftily.utils.client.ClassUtils;
import loftily.utils.client.ClientUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@SuppressWarnings("unchecked")
public abstract class AbstractManager<T> {
    private final Map<Class<? extends T>, T> instanceMap = new HashMap<>();
    
    public AbstractManager(String childPackage, Class<T> superClass) {
        if (childPackage == null) return;
        
        List<Class<?>> classes = ClassUtils.resolvePackage(String.format("%s.%s", this.getClass().getPackage().getName(), childPackage));
        
        for (Class<?> clazz : classes) {
            try {
                if (superClass.isAssignableFrom(clazz) && !clazz.isInterface() && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                    T instance = (T) clazz.getDeclaredConstructor().newInstance();
                    instanceMap.put((Class<? extends T>) clazz, instance);
                }
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        
    }
    
    public <V extends T> V get(Class<V> clazz) {
        T instance = instanceMap.get(clazz);
        
        if (instance == null) {
            ClientUtils.LOGGER.error("Item {} is null", clazz.getSimpleName());
            return null;
        }
        
        return (V) instance;
    }
    
    public List<T> getAll() {
        List<T> list = new ArrayList<>(instanceMap.values());
        
        if (!list.isEmpty() && list.get(0) instanceof Comparable) {
            list.sort(null);
        }
        
        return Collections.unmodifiableList(list);
    }
}
