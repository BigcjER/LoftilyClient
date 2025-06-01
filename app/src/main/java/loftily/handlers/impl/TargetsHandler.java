package loftily.handlers.impl;

import loftily.Client;
import loftily.event.impl.client.ClientTickEvent;
import loftily.handlers.Handler;
import loftily.module.impl.combat.AntiBot;
import loftily.module.impl.combat.Teams;
import loftily.module.impl.player.Blink;
import loftily.utils.math.CalculateUtils;
import lombok.Getter;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;

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
    
    @EventHandler(priority = 500)
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.world == null) return;

        Teams teams = Client.INSTANCE.getModuleManager().get(Teams.class);
        AntiBot antiBot = Client.INSTANCE.getModuleManager().get(AntiBot.class);

        List<EntityLivingBase> filteredTargets = mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase)
                .map(entity -> (EntityLivingBase) entity)
                .filter(entity -> entity != mc.player)
                .filter(entity -> (!teams.isToggled() || !teams.isInTeam(entity)))
                .filter(entity -> (!antiBot.isToggled() || !antiBot.isBot(entity)))
                .filter(entityLivingBase -> !(entityLivingBase instanceof EntityArmorStand))
                .collect(Collectors.toList());
        
        if (targets.size() != filteredTargets.size()) {
            targets.clear();
            targets.addAll(filteredTargets);
        }
    }
}
