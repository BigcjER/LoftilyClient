package loftily.core;

import loftily.utils.client.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")

public abstract class AbstractManager<T> extends ArrayList<T> {
    public AbstractManager(String itemsPackage, Class<T> superClass) {
        if (itemsPackage == null) return;

        List<Class<?>> classes = ClassUtils.resolvePackage(String.format("%s.%s", this.getClass().getPackage().getName(), itemsPackage));

        for (Class<?> clazz : classes) {
            try {
                if (superClass.isAssignableFrom(clazz)) {
                    add((T) clazz.getDeclaredConstructor().newInstance());
                }
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
