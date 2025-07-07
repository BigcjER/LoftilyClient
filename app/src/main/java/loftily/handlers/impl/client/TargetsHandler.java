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
        if (!(target instanceof EntityLivingBase) || target == mc.player) return false;
        EntityLivingBase entityLivingBase = (EntityLivingBase) target;

        if (target instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) target;
            if (entityPlayer.isSpectator() || Client.INSTANCE.getModuleManager().get(Teams.class).isSameTeam(entityPlayer)) {
                return false;
            }
        }

        return entityLivingBase.deathTime <= 0 &&
                !Client.INSTANCE.getModuleManager().get(AntiBot.class).isBot(entityLivingBase) &&
                !(target instanceof EntityArmorStand) &&
                !entityLivingBase.isPlayerSleeping();
    }
    
    @EventHandler(priority = 500)
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        List<EntityLivingBase> filteredTargets = mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase)
                .map(entity -> (EntityLivingBase) entity)
                .filter(TargetsHandler::canAdd)
                .collect(Collectors.toList());


        targets.clear();
        targets.addAll(filteredTargets);
    }
}
