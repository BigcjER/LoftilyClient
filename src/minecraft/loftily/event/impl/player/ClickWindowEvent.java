package loftily.event.impl.player;

import loftily.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;

@Getter
@AllArgsConstructor
public class ClickWindowEvent extends CancellableEvent {
    private int windowId, slotId, mouseButton;
    private ClickType type;
    private EntityPlayer player;
}
