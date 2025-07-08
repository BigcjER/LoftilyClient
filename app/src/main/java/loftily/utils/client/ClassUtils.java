package loftily.utils.client;

import loftily.value.impl.mode.Mode;
import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ClassUtils {
    public static List<Class<?>> resolvePackage(String packagePath) {
        ResolverUtil resolver = new ResolverUtil();
        resolver.setClassLoader(Thread.currentThread().getContextClassLoader());
        
        resolver.findInPackage(new ResolverUtil.Test() {
            @Override
            public boolean matches(Class<?> type) {
                return true;
            }
            
            @Override
            public boolean matches(URI uri) {
                return true;
            }
            
            @Override
            public boolean doesMatchClass() {
                return true;
            }
            
            @Override
            public boolean doesMatchResource() {
                return true;
            }
        }, packagePath);
        
        List<Class<?>> list = new ArrayList<>();
        
        for (Class<?> resolved : resolver.getClasses()) {
            if (!resolved.isInterface() && !Modifier.isAbstract(resolved.getModifiers())) {
                list.add(resolved);
            }
        }
        
        return list;
    }
    
    /**
     * @param packageName Full package name
     */
    public static Mode[] getModes(String packageName) {
        List<Mode> modes = new ArrayList<>();
        
        ClassUtils.resolvePackage(packageName).forEach(clazz -> {
            try {
                if (Mode.class.isAssignableFrom(clazz)) {
                    modes.add((Mode) clazz.getDeclaredConstructor().newInstance());
                }
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
        
        if (modes.isEmpty())
            ClientUtils.LOGGER.warn("No mode found in '{}',please check that the packageName is existed!", packageName);
        
        return modes.toArray(new Mode[0]);
    }
}
