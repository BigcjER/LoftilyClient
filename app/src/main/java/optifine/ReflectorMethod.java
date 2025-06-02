package optifine;

import java.lang.reflect.Method;

public class ReflectorMethod {
    public ReflectorMethod(ReflectorClass p_i93_1_, String p_i93_2_)
    {
        this(p_i93_1_, p_i93_2_, (Class[])null, false);
    }

    public ReflectorMethod(ReflectorClass p_i94_1_, String p_i94_2_, Class[] p_i94_3_)
    {
        this(p_i94_1_, p_i94_2_, p_i94_3_, false);
    }

    public ReflectorMethod(ReflectorClass p_i95_1_, String p_i95_2_, Class[] p_i95_3_, boolean p_i95_4_)
    {
    }

    public Method getTargetMethod()
    {
        return null;
    }

    public boolean exists()
    {
        return false;
    }

    public Class getReturnType()
    {
        Method method = this.getTargetMethod();
        return method == null ? null : method.getReturnType();
    }
}
