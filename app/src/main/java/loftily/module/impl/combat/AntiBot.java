package loftily.module.impl.combat;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.world.WorldLoadEvent;
import loftily.handlers.impl.CombatHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketSpawnPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.viaversion.viaversion.util.ChatColorUtil.stripColor;

@ModuleInfo(name = "AntiBot",category = ModuleCategory.COMBAT)
public class AntiBot extends Module {
    private final BooleanValue tabValue = new BooleanValue("Tab",false);
    private final BooleanValue spawnValue = new BooleanValue("Spawn",false);
    private final BooleanValue spawnInCombatValue = new BooleanValue("SpawnOnlyInCombat",false);
    private final BooleanValue mobValue = new BooleanValue("Mob",false);
    private final BooleanValue animalValue = new BooleanValue("Animal",false);
    private final BooleanValue invisibleValue = new BooleanValue("Invisible",false);
    private final BooleanValue deadValue = new BooleanValue("Dead",false);
    private final BooleanValue playerValue = new BooleanValue("Player",false);
    private final BooleanValue villagerValue = new BooleanValue("Villager",false);

    private final List<Integer> spawnInCombat = new ArrayList<>();
    private final List<Integer> hasRemovedEntities = new ArrayList<>();

    @EventHandler
    public void onWorld(WorldLoadEvent event){
        clear();
    }

    @Override
    public void onDisable(){
        clear();
    }

    public void clear(){
        spawnInCombat.clear();
        hasRemovedEntities.clear();
    }

    @EventHandler
    public void onPacket(PacketReceiveEvent event){
        Packet packet = event.getPacket();
        if(packet instanceof SPacketSpawnPlayer){
            if(spawnValue.getValue() && !hasRemovedEntities.contains(((SPacketSpawnPlayer) packet).getEntityID())){
                if(!spawnInCombatValue.getValue() || CombatHandler.inCombat){
                    spawnInCombat.add(((SPacketSpawnPlayer) packet).getEntityID());
                }
            }
        }else if(packet instanceof SPacketDestroyEntities){
            int[] entityIDz = ((SPacketDestroyEntities) packet).getEntityIDs();
            for(int entityID : entityIDz){
                hasRemovedEntities.add(entityID);
            }
        }
    }

    public boolean isBot(EntityLivingBase entity){
        if (spawnValue.getValue() && spawnInCombat.contains(entity.getEntityId())) {
            return true;
        }

        if(mobValue.getValue() && entity instanceof EntityMob){
            return true;
        }

        if(animalValue.getValue() && entity instanceof EntityAnimal){
            return true;
        }

        if(invisibleValue.getValue() && entity.isInvisible()){
            return true;
        }

        if(deadValue.getValue() && !entity.isEntityAlive()){
            return true;
        }

        if(playerValue.getValue() && entity instanceof EntityPlayer){
            return true;
        }

        if(villagerValue.getValue() && entity instanceof EntityVillager){
            return true;
        }

        if(tabValue.getValue()) {
            for (NetworkPlayerInfo networkPlayerInfo : mc.player.connection.getPlayerInfoMap()) {
                String targetName = stripColor(entity.getDisplayName().getFormattedText());
                if (networkPlayerInfo.getDisplayName() != null) {
                    String netWorkName = stripColor(networkPlayerInfo.getDisplayName().getFormattedText());
                    if (netWorkName.contains(targetName)) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }
}
