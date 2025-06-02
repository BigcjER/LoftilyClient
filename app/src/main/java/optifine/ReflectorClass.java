package optifine;

public class ReflectorClass
{
    public ReflectorClass(String p_i81_1_)
    {
        this(p_i81_1_, false);
    }

    public ReflectorClass(String p_i82_1_, boolean p_i82_2_)
    {
    }

    public ReflectorClass(Class p_i83_1_)
    {
    }

    public Class getTargetClass()
    {
        return null;
    }

    public boolean exists()
    {
        return this.getTargetClass() != null;
    }


    public boolean isInstance(Object p_isInstance_1_)
    {
        return this.getTargetClass() != null && this.getTargetClass().isInstance(p_isInstance_1_);
    }


    public ReflectorMethod makeMethod(String p_makeMethod_1_)
    {
        return new ReflectorMethod(this, p_makeMethod_1_);
    }
    
}
