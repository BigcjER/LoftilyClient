package loftily.utils.player;

import loftily.utils.ItemUtils;
import loftily.utils.client.ClientUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class InventoryUtils implements ClientUtils {
    public static int findBlockInSlot() {
        for (int i = 0; i < 9; ++i) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock) mc.player.inventory.getStackInSlot(i).getItem();
                if (!ItemUtils.BLOCK_BLACKLIST.contains(itemBlock.getBlock())) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public static int findBestToolInSlot(BlockPos blockPos) {
        float bestSpeed = 1;
        int bestSlot = -1;
        
        final IBlockState blockState = mc.world.getBlockState(blockPos);
        
        for (int i = 0; i < 9; i++) {
            final ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            
            if (itemStack.isEmptyStack()) continue;
            
            final float speed = itemStack.getStrVsBlock(blockState);
            
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        
        return bestSlot;
    }
}
