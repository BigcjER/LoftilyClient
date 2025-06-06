package loftily.gui.interaction.draggable;

public interface IDraggable {
    Draggable getDraggable();
    
    default String getName() {
        return "";
    }
}
