package loftily.gui.interaction.draggable;

public interface IDraggable {
    Draggable getDraggable();
    
    /**
     * @return 在DragsConfig里的名字
     */
    default String getName() {
        return "";
    }
}
