package loftily.module.impl.movement;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.player.slowdown.ItemSlowDownEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.PacketUtils;
import loftily.value.impl.MultiBooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashMap;

@ModuleInfo(name = "NoSlow", category = ModuleCategory.MOVEMENT)
public class NoSlow extends Module {

    private final MultiBooleanValue blockingMode = new MultiBooleanValue("BlockingPacketMode")
            .add("Switch", false)
            .add("Extra",false)
            .add("NoGround",false)
            .add("Jump", false);

    private final MultiBooleanValue foodMode = new MultiBooleanValue("FoodMode")
            .add("Switch", false)
            .add("Extra",false)
            .add("Jump", false)
            .add("NoGround",false)
            .add("Clip",false);

    private final MultiBooleanValue bowMode = new MultiBooleanValue("BowMode")
            .add("Switch", false)
            .add("Extra",false);

    private final NumberValue blockingForward = new NumberValue("BlockingForward",0.2f,0.0f,1.0f,0.01f);
    private final NumberValue blockingStrafe = new NumberValue("BlockingStrafe",0.2f,0.0f,1.0f,0.01f);

    private final NumberValue foodForward = new NumberValue("FoodForward",0.2f,0.0f,1.0f,0.01f);
    private final NumberValue foodStrafe = new NumberValue("FoodStrafe",0.2f,0.0f,1.0f,0.01f);

    private final NumberValue bowForward = new NumberValue("BowForward",0.2f,0.0f,1.0f,0.01f);
    private final NumberValue bowStrafe = new NumberValue("BowStrafe",0.2f,0.0f,1.0f,0.01f);

    public Item getUseItem(){
        if(mc.player.isHandActive()) {
            return mc.player.getHeldItem(mc.player.getActiveHand()).getItem();
        }else return null;
    }

    @EventHandler
    public void onMotion(MotionEvent event){
        if(mc.player == null || !mc.player.isHandActive() || getUseItem() == null) return;

        if(getUseItem() instanceof ItemSword || getUseItem() instanceof ItemShield) {
            blockingMode.getValue().forEach(
                    (mode, value) -> {
                        if (value) {
                            switch (mode) {
                                case "Switch":
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 8 + 1));
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                                    break;
                                case "NoGround":
                                    event.setOnGround(false);
                                    break;
                                case "Jump":
                                    if(mc.player.onGround){
                                        mc.player.tryJump();
                                    }
                                    break;
                                case "Extra":
                                    PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(-1,-1,-1), EnumFacing.DOWN,mc.player.getActiveHand(),0f,0f,0f));
                                    break;
                            }
                        }
                    }
            );
        }
        if(getUseItem() instanceof ItemFood || getUseItem() instanceof ItemBucketMilk || getUseItem() instanceof ItemPotion) {
            foodMode.getValue().forEach(
                    (mode, value) -> {
                        if (value) {
                            switch (mode) {
                                case "Switch":
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 8 + 1));
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                                    break;
                                case "NoGround":
                                    event.setOnGround(false);
                                    break;
                                case "Jump":
                                    if(mc.player.onGround){
                                        mc.player.tryJump();
                                    }
                                    break;
                                case "Extra":
                                    PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(-1,-1,-1), EnumFacing.DOWN,mc.player.getActiveHand(),0f,0f,0f));
                                    break;
                                case "Clip":
                                    event.setY(event.getY() + 1e-13);
                                    break;
                            }
                        }
                    }
            );
        }
        if(getUseItem() instanceof ItemBow) {
            bowMode.getValue().forEach(
                    (mode, value) -> {
                        if (value) {
                            switch (mode) {
                                case "Switch":
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 8 + 1));
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                                    break;
                                case "Extra":
                                    PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(-1,-1,-1), EnumFacing.DOWN,mc.player.getActiveHand(),0f,0f,0f));
                                    break;
                            }
                        }
                    }
            );
        }
    }

    @EventHandler
    public void onSlowDown(ItemSlowDownEvent event) {
        if(getUseItem() == null)return;

        if(getUseItem() instanceof ItemShield){
            event.setForwardMultiplier(blockingForward.getValue().floatValue());
            event.setStrafeMultiplier(blockingStrafe.getValue().floatValue());
        }
        if(getUseItem() instanceof ItemFood || getUseItem() instanceof ItemBucketMilk || getUseItem() instanceof ItemPotion){
            event.setForwardMultiplier(foodForward.getValue().floatValue());
            event.setStrafeMultiplier(foodStrafe.getValue().floatValue());
        }
        if(getUseItem() instanceof ItemBow){
            event.setForwardMultiplier(bowForward.getValue().floatValue());
            event.setStrafeMultiplier(bowStrafe.getValue().floatValue());
        }
    }
}
