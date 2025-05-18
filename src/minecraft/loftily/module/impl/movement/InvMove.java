package loftily.module.impl.movement;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;

@ModuleInfo(name = "InvMove",category = ModuleCategory.Movement)
public class InvMove extends Module {
    private final ModeValue mode = new ModeValue("Mode","Vanilla", this,
            new StringMode("Vanilla"),new StringMode("NoPacket"));
    private final BooleanValue sneak = new BooleanValue("Sneak",false);


    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(mc.player == null) return;

        if(mc.currentScreen == null)return;

        mc.gameSettings.keyBindForward.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindForward));
        mc.gameSettings.keyBindBack.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindBack));
        mc.gameSettings.keyBindLeft.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindLeft));
        mc.gameSettings.keyBindRight.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindRight));
        mc.gameSettings.keyBindJump.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindJump));
        mc.gameSettings.keyBindSneak.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindSneak));

        if(MoveUtils.isMoving() && sneak.getValue()){
            mc.gameSettings.keyBindSneak.setPressed(true);
        }
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();

        if(packet instanceof CPacketEntityAction && ((CPacketEntityAction) packet).getAction() == CPacketEntityAction.Action.OPEN_INVENTORY){
            if(mode.is("NoPacket")){
                event.setCancelled(true);
            }
        }
    }
}
