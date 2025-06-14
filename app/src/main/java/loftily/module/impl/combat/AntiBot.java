package loftily.module.impl.combat;

import com.mojang.authlib.GameProfile;
import loftily.event.impl.client.ClientTickEvent;
import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.world.WorldLoadEvent;
import loftily.handlers.impl.player.CombatHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketSpawnPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.viaversion.viaversion.util.ChatColorUtil.stripColor;

@ModuleInfo(name = "AntiBot", category = ModuleCategory.COMBAT)
public class AntiBot extends Module {
    private final BooleanValue tabValue = new BooleanValue("Tab", false);
    private final BooleanValue spawnValue = new BooleanValue("Spawn", false);
    private final BooleanValue spawnInCombatValue = new BooleanValue("SpawnOnlyInCombat", false);
    private final BooleanValue mobValue = new BooleanValue("Mob", false);
    private final BooleanValue animalValue = new BooleanValue("Animal", false);
    private final BooleanValue invisibleValue = new BooleanValue("Invisible", false);
    private final BooleanValue deadValue = new BooleanValue("Dead", false);
    private final BooleanValue playerValue = new BooleanValue("Player", false);
    private final BooleanValue villagerValue = new BooleanValue("Villager", false);
    private final BooleanValue matrixValue = new BooleanValue("Matrix", false);
    
    private final List<Integer> spawnInCombat = new ArrayList<>();
    private final List<Integer> hasRemovedEntities = new ArrayList<>();
    private final HashSet<UUID> suspectList = new HashSet<>();
    private final HashSet<UUID> botList = new HashSet<>();
    
    public boolean isADuplicate(GameProfile profile) {
        return (int) mc.player.connection.getPlayerInfoMap().stream()
                .filter(it -> it.getGameProfile().getName().equals(profile.getName()) && !it.getGameProfile().getId().equals(profile.getId()))
                .count() == 1;
    }
    
    public boolean isGameProfileUnique(GameProfile profile) {
        return (int) mc.player.connection.getPlayerInfoMap().stream()
                .filter(it -> it.getGameProfile().getName().equals(profile.getName()) && it.getGameProfile().getId().equals(profile.getId()))
                .count() == 1;
    }
    
    private boolean isFullyArmored(EntityPlayer entity) {
        return IntStream.rangeClosed(0, 3).allMatch(i -> {
            ItemStack stack = entity.inventory.armorInventory.get(i);
            return stack.getItem() instanceof ItemArmor && stack.isItemEnchanted();
        });
    }
    
    private boolean updatesArmor(EntityPlayer entity, Iterable<ItemStack> prevArmor) {
        return prevArmor != entity.getArmorInventoryList();
    }
    
    @EventHandler
    public void onWorld(WorldLoadEvent event) {
        clear();
    }
    
    @Override
    public void onDisable() {
        clear();
    }
    
    public void clear() {
        spawnInCombat.clear();
        hasRemovedEntities.clear();
        botList.clear();
        suspectList.clear();
    }
    
    @EventHandler
    public void onClientTick(ClientTickEvent event) {
        if (suspectList.isEmpty()) {
            return;
        }
        
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityPlayer) {
                if (!suspectList.contains(entity.getUniqueID())) {
                    continue;
                }
                
                List<ItemStack> armor = null;
                
                if (!isFullyArmored((EntityPlayer) entity)) {
                    armor = ((EntityPlayer) entity).inventory.armorInventory;
                }
                
                if ((isFullyArmored((EntityPlayer) entity) || updatesArmor((EntityPlayer) entity, armor)) && ((EntityPlayer) entity).getGameProfile().getProperties().isEmpty()) {
                    botList.add(entity.getUniqueID());
                }
                
                suspectList.remove(entity.getUniqueID());
            }
        }
    }
    
    @EventHandler
    public void onPacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketSpawnPlayer) {
            if (spawnValue.getValue() && !hasRemovedEntities.contains(((SPacketSpawnPlayer) packet).getEntityID())) {
                if (!spawnInCombatValue.getValue() || CombatHandler.inCombat) {
                    spawnInCombat.add(((SPacketSpawnPlayer) packet).getEntityID());
                }
            }
        }
        if (packet instanceof SPacketPlayerListItem) {
            for (SPacketPlayerListItem.AddPlayerData entry : ((SPacketPlayerListItem) packet).getEntries()) {
                GameProfile profile = entry.getProfile();
                if (entry.getPing() < 2 || !profile.getProperties().isEmpty() || isGameProfileUnique(profile)) {
                    continue;
                }
                
                if (isADuplicate(profile)) {
                    botList.add(entry.getProfile().getId());
                    continue;
                }
                suspectList.add(entry.getProfile().getId());
            }
        } else if (packet instanceof SPacketDestroyEntities) {
            for (int id : ((SPacketDestroyEntities) packet).getEntityIDs()) {
                Entity entity = mc.world.getEntityByID(id);
                if (entity instanceof EntityLivingBase) {
                    UUID uuid = entity.getUniqueID();
                    botList.remove(uuid);
                    suspectList.remove(uuid);
                }
            }
        }
        if (packet instanceof SPacketDestroyEntities) {
            int[] entityIDz = ((SPacketDestroyEntities) packet).getEntityIDs();
            for (int entityID : entityIDz) {
                hasRemovedEntities.add(entityID);
            }
        }
    }
    
    public boolean isBot(EntityLivingBase entity) {
        
        if (matrixValue.getValue() && botList.contains(entity.getUniqueID())) {
            return true;
        }
        
        if (spawnValue.getValue() && spawnInCombat.contains(entity.getEntityId()) && !hasRemovedEntities.contains(entity.getEntityId())) {
            return true;
        }
        
        if (mobValue.getValue() && entity instanceof EntityMob) {
            return true;
        }
        
        if (animalValue.getValue() && entity instanceof EntityAnimal) {
            return true;
        }
        
        if (invisibleValue.getValue() && entity.isInvisible()) {
            return true;
        }
        
        if (deadValue.getValue() && !entity.isEntityAlive()) {
            return true;
        }
        
        if (playerValue.getValue() && entity instanceof EntityPlayer) {
            return true;
        }
        
        if (villagerValue.getValue() && entity instanceof EntityVillager) {
            return true;
        }
        
        if (tabValue.getValue()) {
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
