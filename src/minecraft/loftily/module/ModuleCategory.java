package loftily.module;

public enum ModuleCategory {
    Combat("swords"),
    Movement("directions_run"),
    Render("visibility"),
    World("public"),
    Player("person"),
    Exploit("bug_report"),
    Other("help");
    
    public final String icon;
    
    ModuleCategory(String icon) {
        this.icon = icon;
    }
}
