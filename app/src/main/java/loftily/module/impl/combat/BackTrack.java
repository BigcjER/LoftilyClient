package loftily.module.impl.combat;

import loftily.Client;
import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.AttackEvent;
import loftily.event.impl.render.Render3DEvent;
import loftily.event.impl.world.GameLoopEvent;
import loftily.event.impl.world.WorldLoadEvent;
import loftily.handlers.impl.client.TargetsHandler;
import loftily.handlers.impl.player.CombatHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.CalculateUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.render.ESPUtils;
import loftily.utils.render.RenderUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.server.SPacketPong;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

@ModuleInfo(name = "BackTrack", category = ModuleCategory.COMBAT)
public class BackTrack extends Module {
    public final static Queue<Packet<?>> packets = new LinkedList<>();
    private final ModeValue modeValue = new ModeValue("Mode", "Pulse", this,
            new StringMode("Pulse"),
            new StringMode("SimulatePing"),
            new StringMode("Test")/*just a test*/);
    private final RangeSelectionNumberValue sizeValue = new RangeSelectionNumberValue("EveryReceiveSize", 1, 5, 0, 1000).setVisible(() -> modeValue.is("SimulatePing"));
    private final RangeSelectionNumberValue delayValue = new RangeSelectionNumberValue("Delay", 200, 500, 0, 9999);
    private final RangeSelectionNumberValue rangeValue = new RangeSelectionNumberValue("Range", 1, 5, 0, 8, 0.01);
    private final BooleanValue predictValue = new BooleanValue("Predict", false);
    private final NumberValue maxPredictRange = new NumberValue("MaxPredictRange", 3, 0, 6, 0.01).setVisible(predictValue::getValue);
    private final NumberValue chanceValue = new NumberValue("Chance", 100, 1, 100);
    private final RangeSelectionNumberValue activeRangeValue = new RangeSelectionNumberValue("MaxActiveRange",3, 6, 0, 10, 0.01);
    private final NumberValue maxTargetHurtTime = new NumberValue("MaxTargetHurtTime", 10, 0, 10);
    private final BooleanValue attackToStart = new BooleanValue("AttackToStart", false);
    private final BooleanValue cancelWhenNotInCombat = new BooleanValue("CancelWhenNotInCombat", false);
    private final RangeSelectionNumberValue everyBackTrackDelay = new RangeSelectionNumberValue("EveryBackTrackDelay", 200, 300, 0, 2000);
    private final BooleanValue renderServerPosition = new BooleanValue("RenderServerPosition", false);
    private final ModeValue renderMode = new ModeValue("RenderMode", "Model", this,
            new StringMode("Box"),
            new StringMode("Model")
    );
    private final NumberValue colorRed = new NumberValue("ColorRed", 27, 0, 255).setVisible(() -> renderMode.is("Box"));
    private final NumberValue colorGreen = new NumberValue("ColorGreen", 27, 0, 255).setVisible(() -> renderMode.is("Box"));
    private final NumberValue colorBlue = new NumberValue("ColorBlue", 27, 0, 255).setVisible(() -> renderMode.is("Box"));
    private final NumberValue colorAlpha = new NumberValue("ColorAlpha", 80, 0, 255).setVisible(() -> renderMode.is("Box"));
    private final Map<Integer, Vec3d> positionsMap = new ConcurrentHashMap<>();
    private final DelayTimer backTimer = new DelayTimer();
    private final DelayTimer everyBackTimer = new DelayTimer();
    private int backDelay, everyBackDelay;
    private boolean canStart;
    private EntityLivingBase target;
    
    
    private void reset() {
        positionsMap.clear();
        packets.clear();
        backTimer.reset();
        canStart = false;
    }
    
    public void releaseNormally() {
        everyBackDelay = RandomUtils.randomInt((int) everyBackTrackDelay.getFirst(), (int) everyBackTrackDelay.getSecond());
        backDelay = RandomUtils.randomInt((int) delayValue.getFirst(), (int) delayValue.getSecond());
        backTimer.reset();
        canStart = false;
        positionsMap.clear();
        clearPackets();
    }
    
    public void clearPackets() {
        while (!packets.isEmpty()) {
            Packet<?> packet = packets.poll();
            PacketReceiveEvent event = new PacketReceiveEvent(packet, PacketReceiveEvent.Type.BACKTRACK);
            Client.INSTANCE.getEventManager().call(event);
            if (event.isCancelled()) return;
            
            packet = event.getPacket();
            PacketUtils.receivePacket(packet, false);
        }
        if (!canStart) {
            everyBackTimer.reset();
        }
    }
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        reset();
    }
    
    @Override
    public void onDisable() {
        releaseNormally();
    }
    
    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!renderServerPosition.getValue() || !canStart) return;
        
        switch (renderMode.getValueByName()) {
            case "Box":
                RenderUtils.resetColor();
                positionsMap.forEach(
                        (key, vec3d) -> {
                            Entity entity = mc.world.getEntityByID(key);
                            if (entity == null) return;
                            
                            GlStateManager.pushMatrix();
                            ESPUtils.drawEntityBoxWithCustomPos(
                                    entity,
                                    new Color(
                                            colorRed.getValue().intValue(),
                                            colorGreen.getValue().intValue(),
                                            colorBlue.getValue().intValue(),
                                            colorAlpha.getValue().intValue()),
                                    true,
                                    vec3d.xCoord - mc.getRenderManager().renderPosX,
                                    vec3d.yCoord - mc.getRenderManager().renderPosY,
                                    vec3d.zCoord - mc.getRenderManager().renderPosZ);
                            GlStateManager.popMatrix();
                        }
                );
                RenderUtils.resetColor();
                break;
            case "Model":
                positionsMap.forEach(
                        (key, vec3d) -> {
                            Entity entity = mc.world.getEntityByID(key);
                            if (entity == null) return;
                            
                            GlStateManager.pushMatrix();
                            
                            GlStateManager.translate(
                                    vec3d.xCoord - mc.getRenderManager().viewerPosX,
                                    vec3d.yCoord - mc.getRenderManager().viewerPosY,
                                    vec3d.zCoord - mc.getRenderManager().viewerPosZ
                            );
                            
                            GlStateManager.enableDepth();
                            RenderHelper.enableStandardItemLighting();
                            GlStateManager.color(1, 1, 1, 0.5F);
                            
                            //Set lightmap
                            int i = entity.getBrightnessForRender();
                            int j = i % 65536;
                            int k = i / 65536;
                            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
                            
                            mc.getRenderManager().doRenderEntity(
                                    entity, 0, 0, 0, entity.rotationYaw,
                                    event.getPartialTicks(),
                                    false
                            );
                            
                            RenderHelper.disableStandardItemLighting();
                            GlStateManager.disableDepth();
                            GlStateManager.popMatrix();
                        }
                );
                
                break;
        }
    }
    
    @EventHandler
    public void onGameLoop(GameLoopEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        for (Entity entity : mc.world.loadedEntityList) {
            if (TargetsHandler.canAdd(entity) && (!positionsMap.containsKey(entity.getEntityId()) || !canStart)) {
                positionsMap.put(entity.getEntityId(), entity.getPositionVector());
            }
        }
        
        if (!packets.isEmpty()) {
            switch (modeValue.getValueByName()) {
                case "SimulatePing":
                    if (backTimer.hasTimeElapsed(backDelay)) {
                        int i = 0;
                        int size = RandomUtils.randomInt((int) sizeValue.getFirst(), (int) sizeValue.getSecond());
                        
                        while (!packets.isEmpty()) {
                            if (i >= size) return;
                            
                            Packet<?> packet = packets.poll();
                            PacketReceiveEvent packetEvent = new PacketReceiveEvent(packet, PacketReceiveEvent.Type.BACKTRACK);
                            Client.INSTANCE.getEventManager().call(packetEvent);
                            if (packetEvent.isCancelled()) return;
                            packet = packetEvent.getPacket();
                            PacketUtils.receivePacket(packet, false);
                            i++;
                        }
                    }
                    break;
                case "Pulse":
                    if (backTimer.hasTimeElapsed(backDelay)) {
                        releaseNormally();
                    }
                    break;
                case "Test":
                    backDelay = RandomUtils.randomInt((int) delayValue.getFirst(), (int) delayValue.getSecond());
                    while (packets.size() > backDelay) {
                        Packet<?> packet = packets.poll();
                        PacketReceiveEvent packetEvent = new PacketReceiveEvent(packet, PacketReceiveEvent.Type.BACKTRACK);
                        Client.INSTANCE.getEventManager().call(packetEvent);
                        if (packetEvent.isCancelled()) return;
                        packet = packetEvent.getPacket();
                        PacketUtils.receivePacket(packet, false);
                    }
                    break;
            }
            double range = CalculateUtils.getClosetDistance(mc.player, target);
            if (range < rangeValue.getFirst() || range > rangeValue.getSecond()) {
                releaseNormally();
            } else {
                Vec3d targetVec3d = positionsMap.get(target.getEntityId());
                if (targetVec3d == null) return;
                
                Vec3d vec3d = new Vec3d(targetVec3d.xCoord, targetVec3d.yCoord, targetVec3d.zCoord);
                double realRange = mc.player.getEyes().distanceTo(vec3d);
                if ((realRange > activeRangeValue.getSecond() || realRange < activeRangeValue.getFirst()) || (cancelWhenNotInCombat.getValue() && !CombatHandler.inCombat)) {
                    releaseNormally();
                }
            }
            everyBackTimer.reset();
        } else {
            backTimer.reset();
        }
        if (!canStart) {
            if (!attackToStart.getValue()) {
                if (predictValue.getValue()) {
                    Vec3d predictVec = new Vec3d(
                            target.posX + (target.posX - target.lastTickPosX),
                            target.posY + (target.posY - target.lastTickPosY),
                            target.posZ + (target.posZ - target.lastTickPosZ)
                    );
                    double predictRange = mc.player.getEyes().distanceTo(predictVec);
                    if (predictRange > maxPredictRange.getValue()) {
                        return;
                    }
                }
                if (everyBackTimer.hasTimeElapsed(everyBackDelay)) {
                    List<EntityLivingBase> targets = TargetsHandler.getTargets(rangeValue.getSecond() + 1.0);
                    canStart = !targets.isEmpty() && chanceValue.getValue() >= RandomUtils.randomInt(1, 100) && targets.get(0).hurtTime <= maxTargetHurtTime.getValue();
                    if (canStart) {
                        backTimer.reset();
                        backDelay = RandomUtils.randomInt((int) delayValue.getFirst(), (int) delayValue.getSecond());
                        targets.sort(Comparator.comparingDouble(t -> CalculateUtils.getClosetDistance(mc.player, t)));
                        this.target = targets.get(0);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onAttack(AttackEvent event) {
        if (canStart || !everyBackTimer.hasTimeElapsed(everyBackDelay)) return;
        Entity target = event.getTarget();
        
        if (TargetsHandler.canAdd(target) && attackToStart.getValue()) {
            if (predictValue.getValue()) {
                Vec3d predictVec = new Vec3d(
                        target.posX + (target.posX - target.lastTickPosX),
                        target.posY + (target.posY - target.lastTickPosY),
                        target.posZ + (target.posZ - target.lastTickPosZ)
                );
                double predictRange = mc.player.getEyes().distanceTo(predictVec);
                if (predictRange > maxPredictRange.getValue()) {
                    return;
                }
            }
            double targetRange = CalculateUtils.getClosetDistance(mc.player, (EntityLivingBase) target);
            if (targetRange >= rangeValue.getFirst() && targetRange <= rangeValue.getSecond()) {
                canStart = chanceValue.getValue() >= RandomUtils.randomInt(1, 100) && ((EntityLivingBase) target).hurtTime <= maxTargetHurtTime.getValue();
                
                if (canStart) {
                    backTimer.reset();
                    backDelay = RandomUtils.randomInt((int) delayValue.getFirst(), (int) delayValue.getSecond());
                    this.target = (EntityLivingBase) target;
                }
            }
        }
    }
    
    @EventHandler(priority = 5000)
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (mc.world == null || mc.player == null) {
            return;
        }
        if (packet.getClass().getSimpleName().startsWith("S") && canStart && event.getType() == PacketReceiveEvent.Type.VANILLA) {
            if (packet instanceof SPacketTimeUpdate || packet instanceof SPacketPong || packet instanceof SPacketChat) {
                return;
            }
            
            if (packet instanceof SPacketDisconnect || packet instanceof SPacketJoinGame) {
                packets.clear();
                return;
            }
            
            if (packet instanceof SPacketDestroyEntities) {
                int[] entityIDs = ((SPacketDestroyEntities) packet).getEntityIDs();
                for (int entityID : entityIDs) {
                    if (entityID == target.getEntityId()) {
                        clearPackets();
                        return;
                    }
                }
            }
            
            if ((packet instanceof SPacketUpdateHealth && ((SPacketUpdateHealth) packet).getHealth() <= 0) || packet instanceof SPacketRespawn) {
                releaseNormally();
                canStart = false;
                return;
            }
            
            event.setCancelled(true);
            packets.add(packet);
            
            if (packet instanceof SPacketEntity) {
                SPacketEntity packetEntity = (SPacketEntity) packet;
                Entity entity = packetEntity.getEntity(mc.world);
                if (entity instanceof EntityLivingBase) {
                    int id = packetEntity.getEntity(mc.world).getEntityId();
                    
                    Vec3d oldData = positionsMap.get(id);
                    if (oldData == null) return;
                    
                    Vec3d newData = new Vec3d(
                            oldData.xCoord + packetEntity.getX() / 4096.0D,
                            oldData.yCoord + packetEntity.getY() / 4096.0D,
                            oldData.zCoord + packetEntity.getZ() / 4096.0D);
                    
                    positionsMap.put(id, newData);
                }
            } else if (packet instanceof SPacketEntityTeleport) {
                SPacketEntityTeleport packetEntity = (SPacketEntityTeleport) packet;
                int id = packetEntity.getEntityId();
                Vec3d vec3d = new Vec3d(
                        packetEntity.getX(),
                        packetEntity.getY(),
                        packetEntity.getZ());
                positionsMap.put(id, vec3d);
            }
        }
    }
}
