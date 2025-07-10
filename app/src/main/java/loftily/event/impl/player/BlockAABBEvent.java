package loftily.event.impl.player;

import loftily.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Getter
@AllArgsConstructor
public class BlockAABBEvent extends CancellableEvent {
    private final World world;
    private final Block block;
    private final BlockPos blockPos;
    private final AxisAlignedBB entityBoundingBox;
    private AxisAlignedBB boundingBox;
}
