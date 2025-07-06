package loftily.event.impl.render;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DrawScreenEvent {
    public int mouseX, mouseY;
    public float partialTicks;
}
