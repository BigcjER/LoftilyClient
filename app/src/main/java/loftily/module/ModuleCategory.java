package loftily.module;

public enum ModuleCategory {
    COMBAT("swords", "Combat"),
    MOVEMENT("directions_run", "Movement"),
    RENDER("visibility", "Render"),
    WORLD("public", "World"),
    PLAYER("person", "Player"),
    EXPLOIT("bug_report", "Exploit"),
    OTHER("help", "Other");
    
    public final String icon, name;
    
    ModuleCategory(String icon, String name) {
        this.icon = icon;
        this.name = name;
    }
}
