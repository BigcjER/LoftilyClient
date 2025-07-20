package loftily.module.impl.movement.flys;

import loftily.event.impl.world.PreUpdateEvent;
import loftily.module.impl.movement.Fly;
import loftily.utils.player.InventoryUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AirPlaceFly extends Mode<Fly> {
    public AirPlaceFly() {
        super("AirPlace");
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if(!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)){
            int i = InventoryUtils.findBlockInHotBar(true);
            if(i != -1){
                mc.player.inventory.currentItem = i;
            }else {
                return;
            }
        }
        //RotationHandler.setClientRotation(new Rotation(mc.player.rotationYaw,89F),1,1,"Silent");

        if(mc.player.onGround){
            mc.player.tryJump();
        }else {
            if((mc.player.motionY <= -0.05 && mc.gameSettings.keyBindJump.isKeyDown()) || (mc.player.motionY <= -0.38)){
                Item itemBlock = mc.player.getHeldItemMainhand().getItem();
                if(itemBlock instanceof ItemBlock){
                    BlockPos pos = new BlockPos(mc.player.posX,mc.player.posY,mc.player.posZ).down();
                    if(pos.getBlock().getMaterial().isReplaceable()){
                        mc.playerController.processRightClickBlock(mc.player,mc.world,pos, EnumFacing.UP,new Vec3d(mc.player.posX,pos.getY(),mc.player.posZ),EnumHand.MAIN_HAND);
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                    }
                }
            }
        }
    }
}
