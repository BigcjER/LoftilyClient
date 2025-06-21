package loftily.utils;

import com.google.common.collect.Multimap;
import loftily.utils.client.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ItemUtils implements ClientUtils {
    public static final List<Block> BLOCK_BLACKLIST = Arrays.asList(
            Blocks.ENCHANTING_TABLE, Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST,
            Blocks.ANVIL, Blocks.SAND, Blocks.WEB, Blocks.TORCH, Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.WATERLILY,
            Blocks.DISPENSER, Blocks.STONE_PRESSURE_PLATE, Blocks.WOODEN_PRESSURE_PLATE, Blocks.RED_FLOWER, Blocks.FLOWER_POT, Blocks.YELLOW_FLOWER,
            Blocks.NOTEBLOCK, Blocks.DROPPER, Blocks.STANDING_BANNER, Blocks.WALL_BANNER, Blocks.REDSTONE_TORCH,
            Blocks.GLASS_PANE, Blocks.STAINED_GLASS_PANE, Blocks.LEVER, Blocks.CACTUS, Blocks.LADDER, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM
    );
    
    public static boolean isItemUsefulInChest(ContainerChest containerChest, int indexInChest) {
        ItemStack itemStack = containerChest.getLowerChestInventory().getStackInSlot(indexInChest);
        Item item = itemStack.getItem();
        
        if (item instanceof ItemAxe || item instanceof ItemPickaxe) return true;
        if (item instanceof ItemFood && item != Items.SPIDER_EYE) return true;
        if (item instanceof ItemPotion && !isPotionNegative(itemStack)) return true;
        if (item instanceof ItemBow || item == Items.ARROW) return true;
        if (item instanceof ItemSword && isBestSwordInChest(containerChest, itemStack)) return true;
        if (item instanceof ItemArmor /* TODO:Best armor check */) return true;
        if (item instanceof ItemBlock && !BLOCK_BLACKLIST.contains(((ItemBlock) item).getBlock())) return true;
        return item instanceof ItemEnderPearl;
    }
    
    private static boolean isBestSwordInChest(ContainerChest containerChest, ItemStack itemStack) {
        return true;
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
