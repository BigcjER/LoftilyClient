package loftily.handlers.impl.client;

import loftily.Client;
import loftily.event.impl.client.ClientTickEvent;
import loftily.handlers.Handler;
import loftily.module.impl.combat.AntiBot;
import loftily.module.impl.combat.Teams;
import loftily.module.impl.player.Blink;
import loftily.utils.math.CalculateUtils;
import lombok.Getter;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TargetsHandler extends Handler {
    private static final List<EntityLivingBase> targets = new ArrayList<>();
    
    public static List<EntityLivingBase> getTargets(double range) {
        return targets.stream()
                .filter(entity -> CalculateUtils.getDistanceToEntity(entity, mc.player) < range && entity.getEntityId() != Blink.FAKE_ENTITY_ID)
                .collect(Collectors.toList());
    }
    
    public static boolean canAdd(Entity target) {
        Teams teams = Client.INSTANCE.getModuleManager().get(Teams.class);
        AntiBot antiBot = Client.INSTANCE.getModuleManager().get(AntiBot.class);
        if (!(target instanceof EntityLivingBase)) return false;
        
        if (target instanceof EntityPlayer) {
            if (((EntityPlayer) target).isSpectator()) {
                return false;
            }
            if (!mc.player.canAttackPlayer((EntityPlayer) target)) {
                return false;
            }
        }
        
        if (target instanceof EntityPlayerMP) {
            if (((EntityPlayerMP) target).isSpectator() || target.isSpectatedByPlayer((EntityPlayerMP) target) || ((EntityPlayerMP) target).isCreative()) {
                return false;
            }
        }
        
        if (target.isDead) {
            return false;
        }
        
        return ((EntityLivingBase) target).getHealth() > 0
                && target != mc.player && ((EntityLivingBase) target).deathTime <= 0 &&
                (!teams.isToggled() || !teams.isInTeam((EntityLivingBase) target)) &&
                (!antiBot.isToggled() || !antiBot.isBot((EntityLivingBase) target))
                && !(target instanceof EntityArmorStand) && !((EntityLivingBase) target).isPlayerSleeping();
    }
    
    @EventHandler(priority = 500)
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        List<EntityLivingBase> filteredTargets = mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase)
                .map(entity -> (EntityLivingBase) entity)
                .filter(TargetsHandler::canAdd)
                .collect(Collectors.toList());
        
        if (targets.size() != filteredTargets.size()) {
            targets.clear();
            targets.addAll(filteredTargets);
        }
    }
}
