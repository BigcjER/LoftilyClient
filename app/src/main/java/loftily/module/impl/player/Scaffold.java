package loftily.module.impl.player;

import com.google.common.collect.Lists;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.block.BlockUtils;
import loftily.utils.math.Pair;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "Scaffold", category = ModuleCategory.Player)
public class Scaffold extends Module {

    private static Pair<BlockPos, EnumFacing> searchBlock() {
        BlockPos playerPos;
        if ((int) mc.player.posY <= mc.player.posY) {
            playerPos = new BlockPos(mc.player.posX, (int) mc.player.posY - 1.0, mc.player.posZ);
        } else if (mc.player.posY == Math.round(mc.player.posY) + 0.5) {
            playerPos = new BlockPos(mc.player);
        } else {
            playerPos = new BlockPos(mc.player).down();
        }
        IBlockState iBlockState = mc.world.getBlockState(playerPos);

        if (!iBlockState.getBlock().blockMaterial.isReplaceable() && !(iBlockState.getBlock() instanceof BlockAir)) {
            return null;
        }
        Iterable<BlockPos> blockPosIterable = BlockPos.getAllInBox(
                playerPos.add(-5, 0, -5), playerPos.add(5, -5, 5)
        );
        List<BlockPos> blocks = Lists.newArrayList(blockPosIterable);

        blocks.sort(Comparator.comparingDouble(b -> mc.player.getDistance(b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5)));
        for (BlockPos blockPos : blocks) {
            if (mc.world.getBlockState(blockPos).getBlock() instanceof BlockAir) {
                for (EnumFacing facing : EnumFacing.values()) {
                    BlockPos pos = blockPos.offset(facing);
                    if (!BlockUtils.canBeClick(pos)) {
                        continue;
                    }
                    return new Pair<>(blockPos, facing);
                }
            }
        }

        return null;
    }

    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.player == null) return;

        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) && !(mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock)) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack.getItem() instanceof ItemBlock) {
                    mc.player.inventory.currentItem = i;
                    break;
                }
            }
            return;
        }

        Pair<BlockPos, EnumFacing> pair = searchBlock();

        if (pair == null) {
            return;
        }

        //if(!mc.world.getBlockState(placePos).getBlock().blockMaterial.isReplaceable())return;

        click(pair.getFirst(), pair.getSecond(), new Vec3d(0.5, 0.5, 0.5));

    }

    private void click(BlockPos placePos, EnumFacing facing, Vec3d hitVec) {
        EnumHand hand = mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;

        if (!(mc.player.getHeldItem(hand).getItem() instanceof ItemBlock)) return;

        mc.playerController.processRightClickBlock(mc.player, mc.world, placePos, facing, hitVec, hand);
        mc.player.swingArm(hand);
    }
}
