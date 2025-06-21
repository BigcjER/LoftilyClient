package loftily.module.impl.combat;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.AttackEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.ItemUtils;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.Pair;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketUseEntity;

@ModuleInfo(name = "AutoWeapon", category = ModuleCategory.COMBAT)
public class AutoWeapon extends Module {
    private final BooleanValue onlySwordValue = new BooleanValue("OnlySword", false);
    private boolean attackEntity = false;
    
    @EventHandler
    public void onAttack(AttackEvent event) {
        attackEntity = true;
    }
    
    @EventHandler
    public void onPacket(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof CPacketUseEntity && ((CPacketUseEntity) packet).getAction() == CPacketUseEntity.Action.ATTACK && attackEntity) {
            attackEntity = false;
            
            Pair<Integer, ItemStack> bestWeapon = null;
            for (int i = 0; i <= 8; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (!stack.isEmptyStack() && (stack.getItem() instanceof ItemSword || (stack.getItem() instanceof ItemTool && !onlySwordValue.getValue()))) {
                    
                    double totalValue = ItemUtils.getAttackDamage(stack);
                    if (bestWeapon == null || totalValue > (ItemUtils.getAttackDamage(bestWeapon.getSecond()))) {
                        bestWeapon = new Pair<>(i, stack);
                    }
                }
            }
            
            if (bestWeapon == null) {
                return;
            }
            
            int slot = bestWeapon.getFirst();
            if (slot == mc.player.inventory.currentItem) { // If in hand no need to swap
                return;
            }
            
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
            
            PacketUtils.sendPacket(packet, true);
            event.setCancelled(true);
        }
    }
}
