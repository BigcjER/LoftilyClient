package loftily.utils.block;

import loftily.utils.client.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;


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
        if(!isBlockBBValid(pos, state,true,true))return false;

        return !(block instanceof BlockContainer) && !(block instanceof BlockWorkbench);
    }

    public static List<BlockPos> searchBlocks(int radius) {
        List<BlockPos> blocks = new ArrayList<>();

        for (int x = radius; x >= -radius + 1; x--) {
            for (int y = radius; y >= -radius + 1; y--) {
                for (int z = radius; z >= -radius + 1; z--) {
                    BlockPos blockPos = new BlockPos(mc.player.posX + x, mc.player.posY + y,
                            mc.player.posZ + z);
                    IBlockState block = (blockPos).getState();
                    if (block == null) continue;

                    blocks.add(blockPos);
                }
            }
        }

        return blocks;
    }

    public static boolean isBlockBBValid(
            BlockPos blockPos,
            IBlockState blockState,
            boolean supportSlabs,
            boolean supportPartialBlocks
    ) {
        IBlockState state = blockState != null ? blockState : blockPos.getState();
        if (state == null) {
            return false;
        }

        AxisAlignedBB box = state.getBlock().getCollisionBoundingBox(state, mc.world, blockPos);
        if (box == null) {
            return false;
        }

        // Support blocks like stairs, slab (1x), dragon-eggs, glass-panes, fences, etc
        if (supportPartialBlocks && (box.maxY - box.minY < 1.0 || box.maxX - box.minX < 1.0 || box.maxZ - box.minZ < 1.0)) {
            return true;
        }

        // The slab will only return true if it's placed at a level that can be placed like any normal full block
        return box.maxX - box.minX == 1.0 && (box.maxY - box.minY == 1.0 || (supportSlabs && box.maxY % 1.0 == 0.0)) && box.maxZ - box.minZ == 1.0;
    }
}
