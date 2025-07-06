package loftily.core;

import loftily.utils.client.ClassUtils;
import loftily.utils.client.ClientUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

@SuppressWarnings("unchecked")
public abstract class AbstractManager<T> implements ClientUtils {
    protected final Map<Class<? extends T>, T> instanceMap = new HashMap<>();
    
    public AbstractManager(String childPackage, Class<T> superClass) {
        if (childPackage == null) return;
        
        List<Class<?>> classes = ClassUtils.resolvePackage(String.format("%s.%s", this.getClass().getPackage().getName(), childPackage));
        
        for (Class<?> clazz : classes) {
            try {
                if (superClass.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                    clazz.getDeclaredConstructor();
                    T instance = (T) clazz.getDeclaredConstructor().newInstance();
                    instanceMap.put((Class<? extends T>) clazz, instance);
                }
            } catch (NoSuchMethodException e) {
                //跳过没有空构造函数的类
                ClientUtils.LOGGER.info("Skipping class '{}' due to missing no-arg constructor", clazz.getSimpleName());
            } catch (InstantiationException | IllegalAccessException |
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
