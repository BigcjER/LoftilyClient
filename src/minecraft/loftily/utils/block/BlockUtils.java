package loftily.utils.block;

import loftily.utils.client.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class BlockUtils implements ClientUtils {

    public static boolean canBeClick(BlockPos pos) {
        World world = mc.world;
        if (world == null) return false;

        IBlockState state = mc.world.getBlockState(pos);

        Block block = state.getBlock();
        if (block instanceof BlockAir) return false;

        if (!world.getWorldBorder().contains(pos)) return false;
        if (!block.canCollideCheck(state, false)) return false;
        if (block.blockMaterial.isReplaceable()) return false;
        if (block.hasTileEntity()) return false;

        return !(block instanceof BlockContainer) && !(block instanceof BlockWorkbench);
    }
}
