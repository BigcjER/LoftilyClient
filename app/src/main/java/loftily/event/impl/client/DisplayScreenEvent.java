package loftily.event.impl.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiScreen;

@AllArgsConstructor
@Getter
@Setter
public class DisplayScreenEvent {
    private GuiScreen currentScreen, newScreen;
}
