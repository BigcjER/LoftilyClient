package loftily.module.impl.other;

import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.impl.client.FriendsHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.MessageUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "MiddleClick", category = ModuleCategory.OTHER)
public class MiddleClick extends Module {
    private final BooleanValue addFriend = new BooleanValue("AddFriend", true);
    
    private final DelayTimer delayTimer = new DelayTimer();
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (Mouse.isButtonDown(2) && delayTimer.hasTimeElapsed(300) && addFriend.getValue()) {
            if (mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) mc.objectMouseOver.entityHit;
                
                if (entity instanceof EntityArmorStand) return;
                
                if (FriendsHandler.contains(entity.getName(), entity.getUniqueID())) {
                    FriendsHandler.remove(entity.getName(), entity.getUniqueID());
                    MessageUtils.clientMessageWithWaterMark("Removed " + entity.getName() + " from friends.");
                } else {
                    FriendsHandler.add(entity.getName(), entity.getUniqueID());
                    MessageUtils.clientMessageWithWaterMark("Added " + entity.getName() + " as a friend.");
                }
                
                mc.gameSettings.keyBindPickBlock.setPressed(false);
            }
            delayTimer.reset();
        }
    }
}
