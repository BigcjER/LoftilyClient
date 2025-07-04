package loftily.utils.player;

import loftily.utils.ItemUtils;
import loftily.utils.client.ClientUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class InventoryUtils implements ClientUtils {
    public static final int[] HOTBAR_SLOT_IDS = new int[]{36, 37, 38, 39, 40, 41, 42, 43, 44, 45};
    
    public static int findBlockInSlot() {
        for (int i : HOTBAR_SLOT_IDS) {
            Item item = mc.player.inventoryContainer.getSlot(i).getStack().getItem();
            if (mc.player.inventoryContainer.getSlot(i).getStack().getItem() instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock) item;
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
        
        IBlockState blockState = mc.world.getBlockState(blockPos);
        
        for (int i : HOTBAR_SLOT_IDS) {
            ItemStack itemStack = mc.player.inventoryContainer.getSlot(i).getStack();
            
            if (itemStack.isEmptyStack()) continue;
            
            float speed = itemStack.getStrVsBlock(blockState);
            
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        
        return bestSlot;
    }
}
