package loftily.event.impl.player;

import loftily.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

@Getter
@AllArgsConstructor
public class BlockDigEvent extends CancellableEvent {
    private BlockPos blockPos;
    private EnumFacing facing;
}
