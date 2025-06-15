package loftily.utils.player;

import loftily.utils.client.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;

import java.util.Arrays;
import java.util.List;

public class InventoryUtils implements ClientUtils {
    public static final List<Block> BLOCK_BLACKLIST = Arrays.asList(
            Blocks.ENCHANTING_TABLE, Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST,
            Blocks.ANVIL, Blocks.SAND, Blocks.WEB, Blocks.TORCH, Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.WATERLILY,
            Blocks.DISPENSER, Blocks.STONE_PRESSURE_PLATE, Blocks.WOODEN_PRESSURE_PLATE, Blocks.RED_FLOWER, Blocks.FLOWER_POT, Blocks.YELLOW_FLOWER,
            Blocks.NOTEBLOCK, Blocks.DROPPER, Blocks.STANDING_BANNER, Blocks.WALL_BANNER, Blocks.REDSTONE_TORCH,
            Blocks.GLASS_PANE, Blocks.STAINED_GLASS_PANE, Blocks.LEVER, Blocks.CACTUS, Blocks.LADDER
    );
    
    public static int findBlock() {
        for (int i = 0; i < 9; ++i) {
            if (mc.player.inventoryContainer.getSlot(i + 36).getHasStack() &&
                    mc.player.inventoryContainer.getSlot(i + 36).getStack().getItem() instanceof ItemBlock &&
                    !BLOCK_BLACKLIST.contains(((ItemBlock) mc.player.inventoryContainer.getSlot(i + 36).getStack().getItem()).getBlock())) {
                return i;
            }
        }
        return -1;
    }
}
