package loftily.module.impl.combat;

import loftily.event.impl.player.AttackEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.handlers.impl.player.MoveHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.CalculateUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.player.MoveUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketEntityAction;

@ModuleInfo(name = "MoreKB", category = ModuleCategory.COMBAT)
public class MoreKB extends Module {
    private final ModeValue modeValue = new ModeValue("Mode", "WTap", this,
            new StringMode("Packet"),
            new StringMode("DoublePacket"),
            new StringMode("WTap"),
            new StringMode("STap"),
            new StringMode("Reset"),
            new StringMode("ResetForward"),
            new StringMode("ResetSprint"));
    private final RangeSelectionNumberValue ticks = new RangeSelectionNumberValue("Ticks", 2, 2, 0, 5).setVisible(
            () -> modeValue.is("WTap") || modeValue.is("STap")
    );
    private final RangeSelectionNumberValue time = new RangeSelectionNumberValue("Time", 0, 50, 0, 500).setVisible(
            () -> modeValue.is("Reset") || modeValue.is("ResetForward")
    );
    private final RangeSelectionNumberValue hurtTime = new RangeSelectionNumberValue("TargetHurtTime", 0, 10, 0, 10);
    private final RangeSelectionNumberValue delay = new RangeSelectionNumberValue("Delay", 0, 100, 0, 500);
    private final RangeSelectionNumberValue distance = new RangeSelectionNumberValue("Distance", 0, 4, 0, 7, 0.01);
    private final BooleanValue onlyGround = new BooleanValue("OnlyGround", false);
    private final BooleanValue onlyMove = new BooleanValue("OnlyMove", false);
    private final BooleanValue onlySprint = new BooleanValue("OnlySprint", false);
    
    private final DelayTimer attackTimer = new DelayTimer();
    private int curDelay = 0;
    private boolean tickCancelSprint = false;
    private int attackTicks = 0;
    
    @Override
    public void onDisable() {
        tickCancelSprint = false;
        attackTicks = 0;
        attackTimer.reset();
    }
    
    @EventHandler(priority = 5000)
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (tickCancelSprint) {
            tickCancelSprint = false;
            mc.player.setSprinting(false);
        } else if (mc.currentScreen == null) {
            if (attackTicks > 0) {
                switch (modeValue.getValueByName()) {
                    case "WTap":
                        mc.gameSettings.keyBindForward.setPressed(false);
                        break;
                    case "STap":
                        mc.gameSettings.keyBindForward.setPressed(false);
                        mc.gameSettings.keyBindBack.setPressed(true);
                        break;
                }
                attackTicks--;
            } else {
                mc.gameSettings.keyBindForward.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindForward));
                mc.gameSettings.keyBindBack.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindBack));
            }
        }
    }
    
    @EventHandler
    public void onAttack(AttackEvent event) {
        if ((onlyGround.getValue() && !mc.player.onGround) || (onlyMove.getValue() && !MoveUtils.isMoving()) ||
                (onlySprint.getValue() && !mc.player.isSprinting()) || (!attackTimer.hasTimeElapsed(curDelay)))
            return;
        
        Entity target = event.getTarget();
        
        if (target instanceof EntityLivingBase) {
            if (((EntityLivingBase) target).hurtTime < hurtTime.getFirst() || ((EntityLivingBase) target).hurtTime > hurtTime.getSecond()) {
                return;
            }
            double range = CalculateUtils.getClosetDistance(mc.player, (EntityLivingBase) target);
            if (range >= distance.getFirst() && range <= distance.getSecond()) {
                attackTimer.reset();
                curDelay = RandomUtils.randomInt((int) delay.getFirst(), (int) delay.getSecond());
                switch (modeValue.getValueByName()) {
                    case "Packet":
                        PacketUtils.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                        PacketUtils.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                        break;
                    case "DoublePacket":
                        PacketUtils.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                        PacketUtils.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                        PacketUtils.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                        PacketUtils.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                        break;
                    case "STap":
                    case "WTap":
                        attackTicks = RandomUtils.randomInt((int) ticks.getFirst(), (int) ticks.getSecond());
                        break;
                    case "Reset":
                        MoveHandler.setMovement(0, 0, RandomUtils.randomInt((int) time.getFirst(), (int) time.getSecond()));
                        break;
                    case "ResetForward":
                        MoveHandler.setMovement(0, mc.player.movementInput.moveStrafe, RandomUtils.randomInt((int) time.getFirst(), (int) time.getSecond()));
                        break;
                    case "ResetSprint":
                        tickCancelSprint = true;
                        break;
                }
            }
        }
    }
}
