package loftily.module;

public enum AutoDisableType {
    NONE("None"),
    GAME_END("GameEnd"),
    FLAG("Flag"),
    WORLD_CHANGE("WorldChange");
    
    public final String name;
    
    AutoDisableType(String name) {
        this.name = name;
    }
    
    public static AutoDisableType fromName(String name) {
        if (name == null) {
            return NONE;
        }
        for (AutoDisableType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return NONE;
    }
}
