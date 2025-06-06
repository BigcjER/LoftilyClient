package loftily.handlers.impl;

import loftily.gui.interaction.draggable.IDraggable;
import loftily.handlers.Handler;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class DraggableHandler extends Handler {
    @Getter
    private static final List<IDraggable> draggableList = new ArrayList<>();
    
    public static IDraggable get(String name) {
        return draggableList.stream()
                .filter(draggable -> name.equalsIgnoreCase(draggable.getName()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    protected boolean needRegister() {
        return false;
    }
}
