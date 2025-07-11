package loftily.module.impl.combat;

import loftily.Client;
import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.AttackEvent;
import loftily.event.impl.render.Render3DEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.event.impl.world.WorldLoadEvent;
import loftily.handlers.impl.client.TargetsHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.CalculateUtils;
import loftily.utils.math.RandomUtils;
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

import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "TimerRange", category = ModuleCategory.COMBAT)
public class TimerRange extends Module {
    private final ModeValue modeValue = new ModeValue("Mode", "StartLow", this,
            new StringMode("StartLow"),
            new StringMode("StartFast")
    );
    private final RangeSelectionNumberValue startRange = new RangeSelectionNumberValue("StartRange", 0, 3, 0, 8, 0.01);
    private final RangeSelectionNumberValue activeRange = new RangeSelectionNumberValue("ActiveRange", 0, 6, 0, 8, 0.01);
    private final NumberValue startChance = new NumberValue("StartChance", 100, 0, 100);
    private final RangeSelectionNumberValue time = new RangeSelectionNumberValue("Time", 200, 1000, 0, 5000);
    private final RangeSelectionNumberValue everyTimerDelay = new RangeSelectionNumberValue("EveryTimerDelay", 100, 500, 0, 5000);
    private final RangeSelectionNumberValue targetHurtTime = new RangeSelectionNumberValue("TargetHurtTime", 0, 10, 0, 10);
    private final RangeSelectionNumberValue slowTimerValue = new RangeSelectionNumberValue("SlowTimer", 0.0, 0.1, 0, 0.99, 0.01);
    private final RangeSelectionNumberValue fastTimerValue = new RangeSelectionNumberValue("FastTimer", 2, 4, 1.1, 20, 0.01);
    private final BooleanValue runLastTimerWhenHurt = new BooleanValue("RunLastTimerWhenHurt", false);
    private final BooleanValue runLastTimerWhenOverRange = new BooleanValue("RunLastTimerWhenOverRange", false);
    private final BooleanValue lagReset = new BooleanValue("LagReset", false);
    private final BooleanValue attackToStart = new BooleanValue("AttackToStart", false);
    private final BooleanValue onlyKillAura = new BooleanValue("OnlyKillAura", false);

    private final DelayTimer timerRangeHelper = new DelayTimer();
    private final DelayTimer everyTimerHelper = new DelayTimer();
    private int curTime = 0;
    private int curDelay = 0;
    private boolean runTimer = false;
    private Status timerStatus = Status.NONE;
    private int times = 0;
    private float slowTimer = 0;
    private float fastTimer = 0;
    private EntityLivingBase target = null;

    @Override
    public void onToggle() {
        mc.timer.timerSpeed = 1;
        runTimer = false;
        target = null;
        timerStatus = Status.NONE;
        timerRangeHelper.reset();
        everyTimerHelper.reset();
        curDelay = RandomUtils.randomInt((int) everyTimerDelay.getFirst(), (int) everyTimerDelay.getSecond());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        onToggle();
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
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
                        times = 0;
                        timerStatus = modeValue.is("StartLow") ? Status.LOW : Status.FAST;
                        slowTimer = (float) RandomUtils.randomDouble(slowTimerValue.getFirst(), slowTimerValue.getSecond());
                        fastTimer = (float) RandomUtils.randomDouble(fastTimerValue.getFirst(), fastTimerValue.getSecond());
                        timerRangeHelper.reset();
                        curTime = RandomUtils.randomInt((int) time.getFirst(), (int) time.getSecond());
                        curDelay = RandomUtils.randomInt((int) everyTimerDelay.getFirst(), (int) everyTimerDelay.getSecond());
                        this.target = l;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
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
                            times = 0;
                            timerStatus = modeValue.is("StartLow") ? Status.LOW : Status.FAST;
                            slowTimer = (float) RandomUtils.randomDouble(slowTimerValue.getFirst(), slowTimerValue.getSecond());
                            fastTimer = (float) RandomUtils.randomDouble(fastTimerValue.getFirst(), fastTimerValue.getSecond());
                            timerRangeHelper.reset();
                            curTime = RandomUtils.randomInt((int) time.getFirst(), (int) time.getSecond());
                            curDelay = RandomUtils.randomInt((int) everyTimerDelay.getFirst(), (int) everyTimerDelay.getSecond());
                            this.target = target;
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
                times = 2;
            }
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (runTimer) {
            if (timerStatus.equals(Status.LOW)) {
                if (!timerRangeHelper.hasTimeElapsed(curTime)) {
                    mc.timer.timerSpeed = slowTimer;
                } else {
                    times++;
                    if (times < 2) {
                        fastTimer = (float) RandomUtils.randomDouble(fastTimerValue.getFirst(), fastTimerValue.getSecond());
                        timerStatus = Status.FAST;
                        timerRangeHelper.reset();
                    }
                }
            } else if (timerStatus.equals(Status.FAST)) {
                if (!timerRangeHelper.hasTimeElapsed((int) (curTime / fastTimer))) {
                    mc.timer.timerSpeed = fastTimer;
                } else {
                    times++;
                    if (times < 2) {
                        slowTimer = (float) RandomUtils.randomDouble(slowTimerValue.getFirst(), slowTimerValue.getSecond());
                        timerStatus = Status.LOW;
                        timerRangeHelper.reset();
                    }
                }
            }
            if (times >= 2) {
                mc.timer.timerSpeed = 1;
                runTimer = false;
                timerStatus = Status.NONE;
                timerRangeHelper.reset();
                everyTimerHelper.reset();
                curDelay = RandomUtils.randomInt((int) everyTimerDelay.getFirst(), (int) everyTimerDelay.getSecond());
            }
            double range = CalculateUtils.getClosetDistance(mc.player, target);
            if (times < 1) {
                if ((runLastTimerWhenHurt.getValue() && mc.player.hurtTime >= 8)
                        || (runLastTimerWhenOverRange.getValue() && (range < activeRange.getFirst() || range > activeRange.getSecond()))) {
                    times = 1;
                    curTime = (int) timerRangeHelper.getElapsedTime();
                    timerRangeHelper.reset();
                    timerStatus = timerStatus.equals(Status.LOW) ? Status.FAST : Status.LOW;
                }
            }
            if (!runLastTimerWhenOverRange.getValue()) {
                if (range < activeRange.getFirst() || range > activeRange.getSecond()) {
                    times = 2;
                }
            }
        }
    }

    private enum Status {
        NONE,
        LOW,
        FAST
    }
}
