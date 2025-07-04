package loftily.module.impl.combat;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "Teams", category = ModuleCategory.COMBAT)
public class Teams extends Module {
    private final BooleanValue scoreboardValue = new BooleanValue("ScoreboardTeam", true);
    private final BooleanValue colorValue = new BooleanValue("NameColor", true);
    private final BooleanValue armorValue = new BooleanValue("ArmorColor", true);
    
    public boolean isSameTeam(EntityPlayer entity) {
        if (!isToggled()) return false;
        
        if (mc.player == null || mc.world == null || entity == null) return false;
        
        if (scoreboardValue.getValue() && mc.player.isOnSameTeam(entity)) return true;
        
        if (armorValue.getValue() && isSameTeamByArmor(entity)) return true;
        
        return colorValue.getValue() && isSameTeamByNameColor(entity);
    }
    
    private static boolean isSameTeamByNameColor(EntityPlayer entity) {
        String targetName = entity.getDisplayName().getFormattedText().replace("§r", "");
        String clientName = mc.player.getDisplayName().getFormattedText().replace("§r", "");
        return targetName.startsWith("§" + clientName.charAt(1));
    }
    
    public static boolean isSameTeamByArmor(EntityPlayer entityPlayer) {
        if (mc.player.inventory.armorInventory.get(3).getItem() instanceof ItemArmor &&
                entityPlayer.inventory.armorInventory.get(3).getItem() instanceof ItemArmor) {
            ItemStack myHead = mc.player.inventory.armorInventory.get(3);
            ItemArmor myItemArmor = (ItemArmor) myHead.getItem();
            ItemStack entityHead = entityPlayer.inventory.armorInventory.get(3);
            
            if (entityHead.getItem() instanceof ItemSkull) return false;
            
            ItemArmor entityItemArmor = (ItemArmor) entityHead.getItem();
            return myItemArmor.getColor(myHead) == entityItemArmor.getColor(entityHead);
        }
        return false;
    }
}
