package loftily.core;

import loftily.utils.client.ClassUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractManager<T> extends ArrayList<T> {
    public AbstractManager(String childPackage,Class<T> tClass){
        List<Class<? extends T>> classes = ClassUtils.resolvePackage(String.format("%s.%s", getClass().getPackage().getName(), childPackage), tClass);

        for (Class<? extends T> klass : classes) {
            try {
                T instance = klass.getDeclaredConstructor().newInstance();
                add(instance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate class: " + klass.getName(), e);
            }
        }
    }
}
