package loftily.module.impl.player;

import loftily.event.impl.player.BlockDigEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

@ModuleInfo(name = "AutoTool",category = ModuleCategory.PLAYER)
public class AutoTool extends Module {
    private int slot, originalSlot = -1;
    private int blockBreak;
    private BlockPos blockPos;
    private final BooleanValue switchToOriginalSlot = new BooleanValue("SwitchToOriginalSlot", true);

    @EventHandler
    public void onBlockDig(BlockDigEvent event) {
        blockBreak = 3;
        blockPos = event.getBlockPos();
    }
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null || mc.objectMouseOver == null) return;

        if (!switchToOriginalSlot.getValue()) {
            originalSlot = -1;
        }
        if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
            if (blockPos != null && blockBreak > 0) {
                slot = findBestTool(blockPos);
            } else {
                slot = -1;
            }
        } else {
            slot = -1;
        }

        if (slot != -1 && slot != mc.player.inventory.currentItem) {
            if (originalSlot == -1) {
                originalSlot = mc.player.inventory.currentItem;
            }
            mc.player.inventory.currentItem = slot;
        }

        if (blockBreak <= 0 && switchToOriginalSlot.getValue() && originalSlot != -1) {
            mc.player.inventory.currentItem = originalSlot;
            originalSlot = -1;
        }
        blockBreak--;
    }
    public static int findBestTool(final BlockPos blockPos) {
        float bestSpeed = 1;
        int bestSlot = -1;

        final IBlockState blockState = mc.world.getBlockState(blockPos);

        for (int i = 0; i < 9; i++) {
            final ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.isEmptyStack()) {
                continue;
            }

            final float speed = itemStack.getStrVsBlock(blockState);

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        return bestSlot;
    }
}
