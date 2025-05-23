package loftily.utils.client;

import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil;

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
}
