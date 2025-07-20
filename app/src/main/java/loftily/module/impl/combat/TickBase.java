package loftily.module.impl.combat;

import loftily.Client;
import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.AttackEvent;
import loftily.event.impl.player.RotationEvent;
import loftily.event.impl.world.GameLoopEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.event.impl.world.WorldLoadEvent;
import loftily.handlers.impl.client.TargetsHandler;
import loftily.handlers.impl.player.RotationHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.CalculateUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.player.MoveUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "TickBase", category = ModuleCategory.COMBAT)
public class TickBase extends Module {
    private final ModeValue modeValue = new ModeValue("Mode","Stuck",this
    ,new StringMode("Stuck"),new StringMode("Boost"));
    private final RangeSelectionNumberValue lagTicks = new RangeSelectionNumberValue("Ticks",2,4,0,20);
    private final RangeSelectionNumberValue startRange = new RangeSelectionNumberValue("StartRange", 0, 3, 0, 8, 0.01);
    private final NumberValue startChance = new NumberValue("StartChance", 100, 0, 100);
    private final RangeSelectionNumberValue everyTimerDelay = new RangeSelectionNumberValue("NextLagDelay", 100, 500, 0, 5000);
    private final RangeSelectionNumberValue targetHurtTime = new RangeSelectionNumberValue("TargetHurtTime", 0, 10, 0, 10);
    private final BooleanValue lagReset = new BooleanValue("LagReset", false);
    private final BooleanValue attackToStart = new BooleanValue("AttackToStart", false);
    private final BooleanValue onlyKillAura = new BooleanValue("OnlyKillAura", false);
    private final BooleanValue onlyMove = new BooleanValue("OnlyMove", false);
    private final BooleanValue onlySprint = new BooleanValue("OnlySprint", false);


    private final DelayTimer everyTimerHelper = new DelayTimer();
    private int curDelay = 0;
    private boolean runTimer = false;
    private int skipTicks = 0;
    private int boostTicks = 0;

    @Override
    public void onToggle() {
        skipTicks = 0;
        boostTicks = 0;
        mc.timer.timerSpeed = 1;
        runTimer = false;
        everyTimerHelper.reset();
        curDelay = RandomUtils.randomInt((int) everyTimerDelay.getFirst(), (int) everyTimerDelay.getSecond());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        onToggle();
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if((onlySprint.getValue() && !mc.player.isSprinting()) || (onlyMove.getValue() && !MoveUtils.isMoving()))return;

        if (runTimer || !attackToStart.getValue() || !everyTimerHelper.hasTimeElapsed(curDelay)
                || (onlyKillAura.getValue() && !Client.INSTANCE.getModuleManager().get(KillAura.class).isToggled())) {
            return;
        }
        Entity target = event.getTarget();
        if (TargetsHandler.canAdd(target)) {
            EntityLivingBase l = (EntityLivingBase) target;
            if ((l.hurtTime >= targetHurtTime.getFirst() && l.hurtTime <= targetHurtTime.getSecond())) {
                double range = CalculateUtils.getClosetDistance(mc.player, l);
                if (range >= startRange.getFirst() && range <= startRange.getSecond()) {
                    runTimer = startChance.getValue() >= RandomUtils.randomInt(0, 100);
                    if (runTimer) {
                        skipTicks = RandomUtils.randomInt((int) lagTicks.getFirst(), (int) lagTicks.getSecond());
                        boostTicks = skipTicks;
                        curDelay = RandomUtils.randomInt((int) everyTimerDelay.getFirst(), (int) everyTimerDelay.getSecond());
                    }
                }
            }
        }
    }

    @EventHandler(priority = -5000)
    public void onUpdate(UpdateEvent event) throws IOException {
        if(runTimer){
            if(modeValue.is("Stuck")) {
                if (skipTicks > 0) {
                    skipTicks--;
                    Client.INSTANCE.getEventManager().call(new RotationEvent(RotationHandler.getRotation()));
                    event.setCancelled(true);
                } else {
                    while (boostTicks > 0) {
                        boostTicks--;
                        mc.runTick();
                    }
                    runTimer = false;
                }
            }else if (modeValue.is("Boost")) {
                if(boostTicks > 0) {
                    while (boostTicks > 0) {
                        boostTicks--;
                        mc.runTick();
                    }
                }else {
                    if (skipTicks > 0) {
                        skipTicks--;
                        Client.INSTANCE.getEventManager().call(new RotationEvent(RotationHandler.getRotation()));
                        event.setCancelled(true);
                    } else {
                        runTimer = false;
                    }
                }
            }
            everyTimerHelper.reset();
        }
    }

    @EventHandler
    public void onGameLoop(GameLoopEvent event) {
        if((onlySprint.getValue() && !mc.player.isSprinting()) || (onlyMove.getValue() && !MoveUtils.isMoving()))return;

        if (runTimer || !everyTimerHelper.hasTimeElapsed(curDelay)
                || (onlyKillAura.getValue() && !Client.INSTANCE.getModuleManager().get(KillAura.class).isToggled())) {
            return;
        }
        if (!attackToStart.getValue()) {
            List<EntityLivingBase> targets = TargetsHandler.getTargets(startRange.getSecond() + 1.0);
            if (!targets.isEmpty()) {
                targets.sort(Comparator.comparingDouble(e -> CalculateUtils.getClosetDistance(mc.player, e)));
                EntityLivingBase target = targets.get(0);
                if (target.hurtTime >= targetHurtTime.getFirst() && target.hurtTime <= targetHurtTime.getSecond()) {
                    double range = CalculateUtils.getClosetDistance(mc.player, target);
                    if (range >= startRange.getFirst() && range <= startRange.getSecond()) {
                        runTimer = startChance.getValue() >= RandomUtils.randomInt(0, 100);
                        if (runTimer) {
                            skipTicks = RandomUtils.randomInt((int) lagTicks.getFirst(), (int) lagTicks.getSecond());
                            boostTicks = skipTicks;
                            curDelay = RandomUtils.randomInt((int) everyTimerDelay.getFirst(), (int) everyTimerDelay.getSecond());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketPlayerPosLook) {
            if (runTimer && lagReset.getValue()) {
                this.onToggle();
            }
        }
    }
}
