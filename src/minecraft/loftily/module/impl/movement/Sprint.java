package loftily.module.impl.movement;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.impl.RotationHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.Rotation;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFood;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.MathHelper;

import static java.lang.Math.toRadians;
import static net.minecraft.util.math.MathHelper.abs;
import static net.minecraft.util.math.MathHelper.wrapAngleTo180_float;

@ModuleInfo(name = "Sprint", category = ModuleCategory.Movement)
public class Sprint extends Module {
    private final BooleanValue legitSprint = new BooleanValue("Legit",false);
    private final BooleanValue allDirections = new BooleanValue("AllDirections",false);
    private final BooleanValue noC0B = new BooleanValue("NoSprintPackets",false);
    private final BooleanValue noInventory = new BooleanValue("NoInventory",false);
    private final BooleanValue noFood = new BooleanValue("NoFood",false);
    private final BooleanValue noHunger = new BooleanValue("NoHunger",false);


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
    public void onLivingUpdate(LivingUpdateEvent event) {
        Rotation rotation = RotationHandler.clientRotation;

        if(!MoveUtils.isMoving()) {
            mc.player.setSprinting(false);
            return;
        }

        if(rotation != null && legitSprint.getValue()) {
            Rotation currentRotation = RotationHandler.clientRotation;
            float moveForward = Math.round(mc.player.movementInput.moveForward * MathHelper.cos((float) toRadians(mc.player.rotationYaw - currentRotation.yaw)) + mc.player.movementInput.moveStrafe * MathHelper.sin((float) toRadians(mc.player.rotationYaw - currentRotation.yaw)));
            if(moveForward > 0.8f){
                mc.player.setSprinting(true);
            }else {
                mc.player.setSprinting(false);
            }
        }else {
            if(!allDirections.getValue()) {
                mc.gameSettings.keyBindSprint.setPressed(true);
            }else {
                mc.player.setSprinting(true);
            }
        }

        if(noInventory.getValue() && mc.currentScreen instanceof GuiInventory) {
            mc.player.setSprinting(false);
        }

        if(noFood.getValue() && mc.player.isHandActive() &&
                (mc.player.getHeldItemMainhand().getItem() instanceof ItemFood
                || mc.player.getHeldItemOffhand().getItem() instanceof ItemFood
                || mc.player.getHeldItemMainhand().getItem() instanceof ItemBucketMilk
                || mc.player.getHeldItemOffhand().getItem() instanceof ItemBucketMilk)){
            mc.player.setSprinting(false);
        }

        if(noHunger.getValue() && mc.player.getFoodStats().getFoodLevel() <= 6) {
            mc.player.setSprinting(false);
        }
    }
}
