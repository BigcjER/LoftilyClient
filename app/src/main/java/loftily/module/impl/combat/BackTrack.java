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
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import lombok.Getter;
import lombok.Setter;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.server.SPacketPong;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;

@ModuleInfo(name = "BackTrack",category = ModuleCategory.COMBAT)
public class BackTrack extends Module {
    private final ModeValue modeValue = new ModeValue("Mode","Pulse",this,
            new StringMode("Pulse"),
            new StringMode("SimulatePing"),
            new StringMode("Test")/*just a test*/);
    private final RangeSelectionNumberValue sizeValue = new RangeSelectionNumberValue("EveryReceiveSize",1,5,0,1000).setVisible(()->modeValue.is("SimulatePing"));
    private final RangeSelectionNumberValue delayValue = new RangeSelectionNumberValue("Delay",200,500,0,9999);
    private final RangeSelectionNumberValue rangeValue = new RangeSelectionNumberValue("Range",1,5,0,8,0.01);
    private final BooleanValue predictValue = new BooleanValue("Predict",false);
    private final NumberValue maxPredictRange = new NumberValue("MaxPredictRange",3,0,6,0.01).setVisible(predictValue::getValue);
    private final NumberValue chanceValue = new NumberValue("Chance",100,1,100);
    private final NumberValue maxActiveRangeValue = new NumberValue("MaxActiveRange",6,3,10,0.01);
    private final NumberValue maxTargetHurtTime = new NumberValue("MaxTargetHurtTime",10,0,10);
    private final BooleanValue attackToStart = new BooleanValue("AttackToStart",false);
    private final BooleanValue cancelWhenNotInCombat = new BooleanValue("CancelWhenNotInCombat",false);
    private final RangeSelectionNumberValue everyBackTrackDelay = new RangeSelectionNumberValue("EveryBackTrackDelay",200,300,0,2000);
    private final BooleanValue renderServerPosition = new BooleanValue("RenderServerPosition",false);
    private final ModeValue renderMode = new ModeValue("RenderMode","Model",this,
            //new StringMode("Box"),this mode works badly :)
            new StringMode("Model")
    );
    private final NumberValue colorRed = new NumberValue("ColorRed", 27, 0, 255).setVisible(()->renderMode.is("Box"));
    private final NumberValue colorGreen = new NumberValue("ColorGreen", 27, 0, 255).setVisible(()->renderMode.is("Box"));
    private final NumberValue colorBlue = new NumberValue("ColorBlue", 27, 0, 255).setVisible(()->renderMode.is("Box"));
    private final NumberValue colorAlpha = new NumberValue("ColorAlpha", 80, 0, 255).setVisible(()->renderMode.is("Box"));
    private final BooleanValue positionalInterpolation = new BooleanValue("PositionalInterpolation", true).setVisible(()->renderMode.is("Box"));

    public final static Queue<Packet<?>> packets = new LinkedList<>();
    private final DelayTimer backTimer = new DelayTimer();
    private final DelayTimer everyBackTimer = new DelayTimer();
    private int backDelay = 0;
    private int everyBackDelay = 0;
    private boolean canStart = false;
    private EntityLivingBase target = null;
    private final HashMap<Integer,PosData> hashMap = new HashMap<>();


    private void reset(){
        hashMap.clear();
        packets.clear();
        backTimer.reset();
        canStart = false;
    }

    public void releaseNormally(){
        everyBackDelay = RandomUtils.randomInt((int) everyBackTrackDelay.getFirst(),(int) everyBackTrackDelay.getSecond());
        backDelay = RandomUtils.randomInt((int) delayValue.getFirst(),(int)delayValue.getSecond());
        backTimer.reset();
        canStart = false;
        clearPackets();
    }

    public void clearPackets(){
        while (!packets.isEmpty()){
            Packet<?> packet = packets.poll();
            PacketReceiveEvent event = new PacketReceiveEvent(packet,true);
            Client.INSTANCE.getEventManager().call(event);
            if (event.isCancelled()) return;

            packet = event.getPacket();
            PacketUtils.receivePacket(packet,false);
        }
        if(!canStart){
            everyBackTimer.reset();
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event){
        reset();
    }

    @Override
    public void onDisable(){
        releaseNormally();
    }

    @EventHandler
    public void onRender3D(Render3DEvent event){
        if(renderServerPosition.getValue() && canStart){
            switch (renderMode.getValueByName()){
                case "Box":
                    hashMap.forEach(
                            (key,data) -> ESPUtils.drawEntityBoxWithCustomPos(
                                    mc.world.getEntityByID(key),
                                    new Color(
                                    colorRed.getValue().intValue(),
                                    colorGreen.getValue().intValue(),
                                    colorBlue.getValue().intValue(),
                                    colorAlpha.getValue().intValue()),
                                    positionalInterpolation.getValue(),
                                    true,
                                    data.x,data.y ,data.z)
                    );
                    break;
                case "Model":
                    GL11.glColor4f(1f,1f,1f,1f);
                    hashMap.forEach(
                            (key,data)->{
                                Entity entity = mc.world.getEntityByID(key);
                                RenderManager renderManager = mc.getRenderManager();
                                if(entity != null ) {
                                    renderManager.doRenderEntity(
                                            entity,
                                            data.x - renderManager.renderPosX,
                                            data.y - renderManager.renderPosY,
                                            data.z - renderManager.renderPosZ,
                                            entity.prevRotationYaw,
                                            event.getPartialTicks(),
                                            true
                                            );
                                }
                            }
                    );
                    break;
            }
        }
    }

    @EventHandler
    public void onGameLoop(GameLoopEvent e){
        if(mc.player == null || mc.world == null) return;

        for (Entity entity : mc.world.loadedEntityList){
            if(TargetsHandler.canAdd(entity) && (!hashMap.containsKey(entity.getEntityId()) || !canStart)){
                PosData posData = new PosData(entity.posX, entity.posY, entity.posZ);
                hashMap.put(entity.getEntityId(),posData);
            }
        }
        if(!packets.isEmpty()){
            switch (modeValue.getValueByName()){
                case "SimulatePing":
                    if(backTimer.hasTimeElapsed(backDelay)) {
                        int i = 0;
                        int size = RandomUtils.randomInt((int) sizeValue.getFirst(), (int) sizeValue.getSecond());

                        while (!packets.isEmpty()) {
                            if (i > size) return;

                            Packet<?> packet = packets.poll();
                            PacketReceiveEvent packetEvent = new PacketReceiveEvent(packet, true);
                            Client.INSTANCE.getEventManager().call(packetEvent);
                            if (packetEvent.isCancelled()) return;
                            packet = packetEvent.getPacket();
                            PacketUtils.receivePacket(packet, false);
                            i++;
                        }
                    }
                    break;
                case "Pulse":
                    if(backTimer.hasTimeElapsed(backDelay)) {
                        releaseNormally();
                    }
                    break;
                case "Test":
                    backDelay = RandomUtils.randomInt((int) delayValue.getFirst(), (int) delayValue.getSecond());
                    while (packets.size() > backDelay) {
                        Packet<?> packet = packets.poll();
                        PacketReceiveEvent packetEvent = new PacketReceiveEvent(packet, true);
                        Client.INSTANCE.getEventManager().call(packetEvent);
                        if (packetEvent.isCancelled()) return;
                        packet = packetEvent.getPacket();
                        PacketUtils.receivePacket(packet, false);
                    }
                    break;
            }
            double range = CalculateUtils.getClosetDistance(mc.player,target);
            if(range < rangeValue.getFirst() || range > rangeValue.getSecond()){
                releaseNormally();
            }else {
                PosData targetPosData = hashMap.get(target.getEntityId());
                Vec3d vec3d = new Vec3d(targetPosData.x,targetPosData.y,targetPosData.z);
                double realRange = mc.player.getEyes().distanceTo(vec3d);
                if(realRange > maxActiveRangeValue.getValue() || (cancelWhenNotInCombat.getValue() && !CombatHandler.inCombat)){
                    releaseNormally();
                }
            }
            everyBackTimer.reset();
        }else {
            backTimer.reset();
        }
        if(!canStart){
            if(!attackToStart.getValue()){
                if(predictValue.getValue()){
                    Vec3d predictVec = new Vec3d(
                            target.posX + (target.posX - target.lastTickPosX),
                            target.posY + (target.posY - target.lastTickPosY),
                            target.posZ + (target.posZ - target.lastTickPosZ)
                    );
                    double predictRange = mc.player.getEyes().distanceTo(predictVec);
                    if(predictRange > maxPredictRange.getValue()){
                        return;
                    }
                }
                if(everyBackTimer.hasTimeElapsed(everyBackDelay)) {
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
    public void onAttack(AttackEvent event){
        if(canStart || !everyBackTimer.hasTimeElapsed(everyBackDelay))return;
        Entity target = event.getTarget();

        if(TargetsHandler.canAdd(target) && attackToStart.getValue()){
            if(predictValue.getValue()){
                Vec3d predictVec = new Vec3d(
                        target.posX + (target.posX - target.lastTickPosX),
                        target.posY + (target.posY - target.lastTickPosY),
                        target.posZ + (target.posZ - target.lastTickPosZ)
                );
                double predictRange = mc.player.getEyes().distanceTo(predictVec);
                if(predictRange > maxPredictRange.getValue()){
                    return;
                }
            }
            double targetRange = CalculateUtils.getClosetDistance(mc.player,(EntityLivingBase) target);
            if(targetRange >= rangeValue.getFirst() && targetRange <= rangeValue.getSecond()){
                canStart = chanceValue.getValue() >= RandomUtils.randomInt(1,100) && ((EntityLivingBase) target).hurtTime <= maxTargetHurtTime.getValue();;
                if(canStart) {
                    backTimer.reset();
                    backDelay = RandomUtils.randomInt((int) delayValue.getFirst(), (int) delayValue.getSecond());
                    this.target = (EntityLivingBase) target;
                }
            }
        }
    }

    @EventHandler(priority = 2000)
    public void onPacketReceive(PacketReceiveEvent event){
        Packet<?> packet = event.getPacket();
        if(mc.world == null || mc.player == null){
            return;
        }
        if(packet.getClass().getSimpleName().startsWith("S") && canStart && !event.isGAY()) {
            if (packet instanceof SPacketTimeUpdate || packet instanceof SPacketPong || packet instanceof SPacketChat) {
                return;
            }

            if (packet instanceof SPacketDisconnect || packet instanceof SPacketJoinGame) {
                packets.clear();
                return;
            }

            if(packet instanceof SPacketDestroyEntities){
                int[] entityIDs = ((SPacketDestroyEntities) packet).getEntityIDs();
                for(int entityID : entityIDs){
                    if(entityID == target.getEntityId()){
                        clearPackets();
                        return;
                    }
                }
            }

            if ((packet instanceof SPacketUpdateHealth && ((SPacketUpdateHealth) packet).getHealth() <= 0) || packet instanceof SPacketRespawn) {
                clearPackets();
                return;
            }

            event.setCancelled(true);
            packets.add(packet);

            if (packet instanceof SPacketEntity) {
                SPacketEntity packetEntity = (SPacketEntity) packet;
                Entity entity = packetEntity.getEntity(mc.world);
                if(entity instanceof EntityLivingBase) {
                    int id = packetEntity.getEntity(mc.world).getEntityId();
                    PosData oldData = hashMap.get(id);
                    PosData newData = new PosData(
                            oldData.x + packetEntity.getX() / 4096.0D,
                            oldData.y + packetEntity.getY() / 4096.0D,
                            oldData.z + packetEntity.getZ() / 4096.0D);
                    hashMap.put(id, newData);
                }
            }else if(packet instanceof SPacketEntityTeleport){
                SPacketEntityTeleport packetEntity = (SPacketEntityTeleport) packet;
                int id = packetEntity.getEntityId();
                PosData newData = new PosData(
                        packetEntity.getX(),
                        packetEntity.getY(),
                        packetEntity.getZ());
                hashMap.put(id, newData);
            }
        }
    }

    @Getter
    @Setter
    public static class PosData{
        public double x;
        public double y;
        public double z;

        public PosData(double x, double y, double z){
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
