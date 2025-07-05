package loftily.module.impl.player;

import loftily.event.impl.player.BlockDigEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.player.InventoryUtils;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

@ModuleInfo(name = "AutoTool", category = ModuleCategory.PLAYER)
public class AutoTool extends Module {
    private final BooleanValue autoSwitchBack = new BooleanValue("AutoSwitchBack", true);
    private int blockBreak, prevSlot = -1;
    private BlockPos blockPos;
    
    @Override
    public void onToggle() {
        prevSlot = mc.player.inventory.currentItem;
        super.onToggle();
    }
    
    @EventHandler
    public void onBlockDig(BlockDigEvent event) {
        blockBreak = 3;
        blockPos = event.getBlockPos();
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null || mc.objectMouseOver == null) return;
        
        if (!autoSwitchBack.getValue()) {
            prevSlot = -1;
        }
        
        int slot = -1;
        if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
            if (blockPos != null && blockBreak > 0) {
                slot = InventoryUtils.findBestToolInHotBar(blockPos);
            }
        }
        
        if (slot != -1 && slot != mc.player.inventory.currentItem) {
            if (prevSlot == -1) {
                prevSlot = mc.player.inventory.currentItem;
            }
            mc.player.inventory.currentItem = slot;
        }
        
        if (blockBreak <= 0 && autoSwitchBack.getValue() && prevSlot != -1) {
            mc.player.inventory.currentItem = prevSlot;
            prevSlot = -1;
        }
        blockBreak--;
    }
}
