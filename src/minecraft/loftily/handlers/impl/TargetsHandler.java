package loftily.handlers.impl;

import loftily.event.impl.client.ClientTickEvent;
import loftily.handlers.Handler;
import lombok.Getter;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TargetsHandler extends Handler {
    public static final List<EntityLivingBase> targets = new ArrayList<>();
    
    public static List<EntityLivingBase> getTargets(double range) {
        return targets.stream()
                .filter(entity -> mc.player.getDistanceToEntity(entity) < range)
                .collect(Collectors.toList());
    }
    
    @EventHandler(priority = 1000)
    public void onTick(ClientTickEvent event) {
        List<EntityLivingBase> filteredTargets = mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase)
                .map(entity -> (EntityLivingBase) entity)
                .filter(entity -> entity != mc.player)
                .filter(entityLivingBase -> !(entityLivingBase instanceof EntityArmorStand))
                .collect(Collectors.toList());
        
        if (targets.size() != filteredTargets.size()) {
            targets.clear();
            targets.addAll(filteredTargets);
        }
    }
    
}
