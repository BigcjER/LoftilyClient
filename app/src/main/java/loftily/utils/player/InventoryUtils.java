package loftily.utils.player;

import loftily.utils.ItemUtils;
import loftily.utils.client.ClientUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryUtils implements ClientUtils {
    public static final int[] HOTBAR_SLOT_IDS = new int[]{36, 37, 38, 39, 40, 41, 42, 43, 44, 45};
    
    public static int getBlocksInHotBar() {
        int blocks = 0;
        
        for (int i : HOTBAR_SLOT_IDS) {
            ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();
            if (stack.isEmptyStack()) continue;
            
            Item item = stack.getItem();
            
            if (item instanceof ItemBlock) {
                blocks += stack.getStackSize();
            }
        }
        
        return blocks;
    }

    public static int findBlockInHotBar(boolean biggest) {
        if (biggest) {
            int slot = -1;
            int size = 0;
            for (int i = 0 ; i < 9; i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);
                    ItemBlock block = (ItemBlock) stack.getItem();
                    if (!ItemUtils.BLOCK_BLACKLIST.contains(block.getBlock()) && stack.getStackSize() > size) {
                        size = stack.getStackSize();
                        slot = i;
                    }
                }
            }
            return slot;
        } else {
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
    }
    
    public static int findBestToolInHotBar(BlockPos blockPos) {
        float bestSpeed = 1;
        int bestSlot = -1;
        
        IBlockState blockState = mc.world.getBlockState(blockPos);
        
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            
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
