package loftily.module.impl.combat;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import java.util.Objects;

@ModuleInfo(name = "Teams", category = ModuleCategory.COMBAT)
public class Teams extends Module {
    private final BooleanValue scoreboardValue = new BooleanValue("ScoreboardTeam", true);
    private final BooleanValue colorValue = new BooleanValue("NameColor", true);
    private final BooleanValue armorValue = new BooleanValue("ArmorColor", true);
    
    public boolean isInTeam(EntityLivingBase entity) {
        if (mc.player == null || mc.world == null || !(entity instanceof EntityPlayer)) return false;
        
        if (scoreboardValue.getValue() && mc.player.getTeam() != null && entity.getTeam() != null &&
                Objects.requireNonNull(mc.player.getTeam()).isSameTeam(entity.getTeam())) {
            return true;
        }
        
        if (armorValue.getValue()) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            ItemStack myHead = Objects.requireNonNull(mc.player.inventory.armorInventory).get(3);
            ItemArmor myItemArmor = Objects.requireNonNull((ItemArmor) myHead.getItem());
            
            ItemStack entityHead = Objects.requireNonNull(entityPlayer.inventory.armorInventory.get(3));
            ItemArmor entityItemArmor = Objects.requireNonNull((ItemArmor) entityHead.getItem());
            
            if (myItemArmor.getColor(myHead) == entityItemArmor.getColor(entityHead)) {
                return true;
            }
        }
        
        if (colorValue.getValue()) {
            String targetName = entity.getDisplayName().getFormattedText().replace("§r", "");
            String clientName = mc.player.getDisplayName().getFormattedText().replace("§r", "");
            return targetName.startsWith("§" + clientName.charAt(1));
        }
        
        return false;
    }
}
