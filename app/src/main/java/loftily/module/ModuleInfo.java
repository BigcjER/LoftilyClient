package loftily.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleInfo {
    ModuleCategory category();
    
    String name();
    
    int key() default 0;
    
    boolean defaultToggled() default false;
    
    boolean canBeToggled() default true;
    
    AutoDisableType autoDisable() default AutoDisableType.NONE;
}