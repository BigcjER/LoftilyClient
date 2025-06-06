package loftily.module.impl.movement;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.handlers.impl.RotationHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.CalculateUtils;
import loftily.utils.math.Rotation;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFood;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;

@ModuleInfo(name = "Sprint", category = ModuleCategory.MOVEMENT)
public class Sprint extends Module {
    private final BooleanValue legitSprint = new BooleanValue("Legit",false);
    private final BooleanValue allDirections = new BooleanValue("AllDirections",false);
    private final BooleanValue allDirectionsJump = new BooleanValue("AllDirectionsJump",false);
    private final BooleanValue noC0B = new BooleanValue("NoSprintPackets",false);
    private final BooleanValue noInventory = new BooleanValue("NoInventory",false);
    private final BooleanValue noGui = new BooleanValue("NoAnyGui",false);
    private final BooleanValue noFood = new BooleanValue("NoFood",false);
    private final BooleanValue noHunger = new BooleanValue("NoHunger",false);
    private final BooleanValue noSneaking = new BooleanValue("NoSneak",false);

    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();

        if(!noC0B.getValue())return;

        if(packet instanceof CPacketEntityAction){
            if(((CPacketEntityAction) packet).getAction() == CPacketEntityAction.Action.START_SPRINTING
            || ((CPacketEntityAction) packet).getAction() == CPacketEntityAction.Action.STOP_SPRINTING){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if(!allDirectionsJump.getValue())return;
        event.setMovementYaw(
                (float) (event.getMovementYaw() + Math.toDegrees(MoveUtils.getDirection()) - mc.player.rotationYaw)
        );
    }

    @EventHandler(priority = -10)
    public void onLivingUpdate(LivingUpdateEvent event) {
        Rotation rotation = RotationHandler.clientRotation;

        if(!allDirections.getValue()) {
            if(!legitSprint.getValue()) {
                mc.gameSettings.keyBindSprint.setPressed(true);
            }
        }else {
            mc.player.setSprinting(true);
        }

        if(!MoveUtils.isMoving()) {
            stopSprinting();
            return;
        }

        if(legitSprint.getValue()){
            if(rotation != null) {
                float calcForward = CalculateUtils.getMoveFixForward(rotation);
                if (calcForward < 0.8) {
                    stopSprinting();
                }else {
                    mc.player.setSprinting(true);
                }
            }else {
                if (mc.player.movementInput.moveForward < 0.8) {
                    stopSprinting();
                }else {
                    mc.player.setSprinting(true);
                }
            }
        }

        if(noSneaking.getValue()){
            if(mc.player.isSneaking()){
                stopSprinting();
            }
        }

        if((noInventory.getValue() && mc.currentScreen instanceof GuiInventory) || (noGui.getValue() && mc.currentScreen != null)) {
            stopSprinting();
        }

        if(noFood.getValue() && mc.player.isHandActive() &&
                (mc.player.getHeldItemMainhand().getItem() instanceof ItemFood
                        || mc.player.getHeldItemOffhand().getItem() instanceof ItemFood
                        || mc.player.getHeldItemMainhand().getItem() instanceof ItemBucketMilk
                        || mc.player.getHeldItemOffhand().getItem() instanceof ItemBucketMilk)){
            stopSprinting();
        }

        if(noHunger.getValue() && mc.player.getFoodStats().getFoodLevel() <= 6) {
            stopSprinting();
        }
    }

    void stopSprinting(){
        mc.player.setSprinting(false);
        mc.gameSettings.keyBindSprint.setPressed(false);
    }
}
