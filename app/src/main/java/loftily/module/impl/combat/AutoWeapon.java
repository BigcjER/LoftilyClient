package loftily.module.impl.combat;

import com.google.common.collect.Multimap;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.AttackEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.Pair;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketUseEntity;

import java.util.Collection;

@ModuleInfo(name = "AutoWeapon", category = ModuleCategory.COMBAT)
public class AutoWeapon extends Module {
    
    private Boolean attackEntity = false;
    private final BooleanValue onlySwordValue = new BooleanValue("OnlySword", false);
    
    @EventHandler
    public void onAttack(AttackEvent event) {
        attackEntity = true;
    }
    
    private double getAttackDamage(ItemStack stack) {
        Multimap<String, AttributeModifier> attributeModifiers = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        
        if (attributeModifiers.containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName())) {
            Collection<AttributeModifier> modifiers = attributeModifiers.get(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName());
            
            if (!modifiers.isEmpty()) {
                AttributeModifier modifier = modifiers.iterator().next();
                return modifier.getAmount();
            }
        }
        
        return 0.0;
    }
    
    @EventHandler
    public void onPacket(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof CPacketUseEntity && ((CPacketUseEntity) packet).getAction() == CPacketUseEntity.Action.ATTACK && attackEntity) {
            Pair<Integer, ItemStack> bestWeapon = null;
            for (int i = 0; i <= 8; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (!stack.isEmptyStack() && (stack.getItem() instanceof ItemSword || (stack.getItem() instanceof ItemTool && !onlySwordValue.getValue()))) {
                    
                    double attackDamage = getAttackDamage(stack);
                    double enchantmentBonus = 0;
                    if (Enchantments.SHARPNESS != null) {
                        enchantmentBonus = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, stack);
                    }
                    if (enchantmentBonus > 0) enchantmentBonus = 1.0 + (enchantmentBonus - 1) * 0.5;
                    
                    double totalValue = attackDamage + enchantmentBonus;
                    if (bestWeapon == null || totalValue > (getAttackDamage(bestWeapon.getSecond()) + enchantmentBonus)) {
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
