package loftily.utils.player;

import loftily.utils.ItemUtils;
import loftily.utils.client.ClientUtils;
import net.minecraft.item.ItemBlock;

public class InventoryUtils implements ClientUtils {
    public static int findBlock() {
        for (int i = 0; i < 9; ++i) {
            if (mc.player.inventoryContainer.getSlot(i + 36).getHasStack() &&
                    mc.player.inventoryContainer.getSlot(i + 36).getStack().getItem() instanceof ItemBlock &&
                    !ItemUtils.BLOCK_BLACKLIST.contains(((ItemBlock) mc.player.inventoryContainer.getSlot(i + 36).getStack().getItem()).getBlock())) {
                return i;
            }
        }
        return -1;
    }
}
