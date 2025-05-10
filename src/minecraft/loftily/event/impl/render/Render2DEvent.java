package loftily.event.impl.render;


import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;

@Getter
@AllArgsConstructor
public class Render2DEvent {
    private ScaledResolution scaledResolution;
    private float partialTicks;
}
