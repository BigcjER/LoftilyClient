package loftily.utils;

import com.google.common.collect.Multimap;
import loftily.utils.client.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ItemUtils implements ClientUtils {
    public static final List<Block> BLOCK_BLACKLIST = Arrays.asList(
            Blocks.ENCHANTING_TABLE, Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST,
            Blocks.ANVIL, Blocks.SAND, Blocks.WEB, Blocks.TORCH, Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.WATERLILY,
            Blocks.DISPENSER, Blocks.STONE_PRESSURE_PLATE, Blocks.WOODEN_PRESSURE_PLATE, Blocks.RED_FLOWER, Blocks.FLOWER_POT, Blocks.YELLOW_FLOWER,
            Blocks.NOTEBLOCK, Blocks.DROPPER, Blocks.STANDING_BANNER, Blocks.WALL_BANNER, Blocks.REDSTONE_TORCH,
            Blocks.GLASS_PANE, Blocks.STAINED_GLASS_PANE, Blocks.LEVER, Blocks.CACTUS, Blocks.LADDER, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM,
            Blocks.OAK_FENCE_GATE, Blocks.ACACIA_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE,
            Blocks.END_GATEWAY, Blocks.GLASS,Blocks.TNT,Blocks.WATER,Blocks.AIR,Blocks.VINE,Blocks.SKULL
    );
    
    public static boolean isItemUsefulInContainer(Container container, ItemStack itemStack) {
        Item item = itemStack.getItem();
        
        if ((item instanceof ItemAxe || item instanceof ItemPickaxe) && isItemUnique(container, itemStack)) return true;
        if (item instanceof ItemFood && item != Items.SPIDER_EYE && item != Items.ROTTEN_FLESH) return true;
        if (item instanceof ItemPotion && !isPotionNegative(itemStack)) return true;
        if (item instanceof ItemBow || item == Items.ARROW) return true;
        if (item instanceof ItemSword && isBestSword(container, itemStack) && isItemUnique(container, itemStack))
            return true;
        if (item instanceof ItemArmor && isBestArmor(container, itemStack) && isItemUnique(container, itemStack))
            return true;
        if (item instanceof ItemBlock && !BLOCK_BLACKLIST.contains(((ItemBlock) item).getBlock())) return true;
        return item instanceof ItemEnderPearl;
    }
    
    public static boolean isItemUnique(Container container, ItemStack itemStack) {
        if (itemStack.isEmptyStack()) {
            return true;
        }
        
        for (Slot slot : container.inventorySlots) {
            ItemStack slotStack = slot.getStack();
            
            if (!slotStack.isEmptyStack() && slotStack != itemStack && slotStack.getItem() == itemStack.getItem()) {
                return itemStack.getItemDamage() < slotStack.getItemDamage();
            }
        }
        
        return true;
    }
    
    private static boolean isBestArmor(Container container, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (itemStack.isEmptyStack() || !(item instanceof ItemArmor)) return false;
        
        ItemArmor itemArmor = (ItemArmor) item;
        EntityEquipmentSlot armorType = itemArmor.armorType;
        
        double thisArmorWeight = getArmorWeight(itemStack);
        double maxArmorWeight = 0D;
        
        for (Slot slot : container.inventorySlots) {
            ItemStack slotStack = slot.getStack();
            
            if (!slotStack.isEmptyStack() && slotStack.getItem() instanceof ItemArmor) {
                ItemArmor slotArmor = (ItemArmor) slotStack.getItem();
                
                if (slotArmor.armorType == armorType) {
                    double tempWeight = getArmorWeight(slotStack);
                    if (tempWeight > maxArmorWeight) {
                        maxArmorWeight = tempWeight;
                    }
                }
            }
        }
        
        return thisArmorWeight >= maxArmorWeight;
    }
    
    public static double getArmorWeight(ItemStack itemStack) {
        if (itemStack.isEmptyStack() || !(itemStack.getItem() instanceof ItemArmor)) return 0.0;
        
        ItemArmor itemArmor = (ItemArmor) itemStack.getItem();
        double value = itemArmor.damageReduceAmount;
        
        if (itemStack.isItemEnchanted()) {
            final Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                
                if (enchantment.getName().contains("protection")) {
                    if (Enchantment.getEnchantmentID(enchantment) == 0) {
                        value += level * 1.25;
                    } else {
                        value += level * 1.1;
                    }
                }
            }
        }
        
        return value;
    }
    
    private static boolean isBestSword(Container container, ItemStack itemStack) {
        double thisSwordDamage = getAttackDamage(itemStack);
        double maxSwordDamage = 0D;
        
        for (Slot slot : container.inventorySlots) {
            ItemStack slotStack = slot.getStack();
            if (!slotStack.isEmptyStack() && slotStack.getItem() instanceof ItemSword) {
                double tempDamage = getAttackDamage(slotStack);
                
                if (tempDamage >= maxSwordDamage) {
                    maxSwordDamage = tempDamage;
                }
            }
        }
        
        return thisSwordDamage >= maxSwordDamage;
    }
    
    public static double getAttackDamage(ItemStack stack) {
        double attackDamage = 1.0D;
        
        if (stack == null || stack.isEmptyStack()) return attackDamage;
        Multimap<String, AttributeModifier> attributeModifiers = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        
        if (attributeModifiers.containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName())) {
            Collection<AttributeModifier> modifiers = attributeModifiers.get(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName());
            
            if (!modifiers.isEmpty()) {
                AttributeModifier modifier = modifiers.iterator().next();
                attackDamage += modifier.getAmount();
            }
        }
        
        attackDamage += EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
        
        return attackDamage;
    }
    
    public static boolean isPotionNegative(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemPotion)) return false;
        
        final List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stack);
        
        return effects.stream()
                .map(PotionEffect::getPotion)
                .anyMatch(Potion::isBadEffect);
    }
}
