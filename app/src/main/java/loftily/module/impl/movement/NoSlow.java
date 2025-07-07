package loftily.module.impl.movement;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.player.slowdown.ItemSlowDownEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.PacketUtils;
import loftily.utils.player.PlayerUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.MultiBooleanValue;
import loftily.value.impl.NumberValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

@ModuleInfo(name = "NoSlow", category = ModuleCategory.MOVEMENT)
public class NoSlow extends Module {
    
    private final MultiBooleanValue blockingMode = new MultiBooleanValue("BlockingPacketMode")
            .add("Switch", false)
            .add("Extra", false)
            .add("AAC5", false)
            .add("NoGround", false)
            .add("Jump", false)
            .add("HandPacket", false)
            .add("Post", false);
    
    private final MultiBooleanValue foodMode = new MultiBooleanValue("FoodMode")
            .add("Switch", false)
            .add("Extra", false)
            .add("Jump", false)
            .add("NoGround", false)
            .add("Clip", false);
    
    private final MultiBooleanValue bowMode = new MultiBooleanValue("BowMode")
            .add("Switch", false)
            .add("Extra", false);
    
    private final NumberValue blockingForward = new NumberValue("BlockingForward", 0.2f, 0.0f, 1.0f, 0.01f);
    private final NumberValue blockingStrafe = new NumberValue("BlockingStrafe", 0.2f, 0.0f, 1.0f, 0.01f);
    
    private final NumberValue foodForward = new NumberValue("FoodForward", 0.2f, 0.0f, 1.0f, 0.01f);
    private final NumberValue foodStrafe = new NumberValue("FoodStrafe", 0.2f, 0.0f, 1.0f, 0.01f);
    
    private final NumberValue bowForward = new NumberValue("BowForward", 0.2f, 0.0f, 1.0f, 0.01f);
    private final NumberValue bowStrafe = new NumberValue("BowStrafe", 0.2f, 0.0f, 1.0f, 0.01f);
    
    private final BooleanValue ignoreServerItemChange = new BooleanValue("IgnoreServerItemChange", false);
    
    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!ignoreServerItemChange.getValue()) return;
        
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketHeldItemChange) {
            event.setCancelled(true);
        }
    }
    
    public Item getUseItem() {
        if (PlayerUtils.isUsingItem()) {
            return mc.player.getHeldItem(mc.player.getActiveHand()).getItem();
        } else return null;
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if (mc.player == null || !PlayerUtils.isUsingItem() || getUseItem() == null) return;
        
        if (getUseItem() instanceof ItemSword || getUseItem() instanceof ItemShield) {
            blockingMode.getValue().forEach(
                    (mode, value) -> {
                        if (value) {
                            switch (mode) {
                                case "Switch":
                                    if (event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 8 + 1));
                                        PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                                    }
                                    break;
                                case "NoGround":
                                    event.setOnGround(false);
                                    break;
                                case "Jump":
                                    if (mc.player.onGround && event.isPost()) {
                                        mc.player.tryJump();
                                    }
                                    break;
                                case "AAC5":
                                    if (event.isPost()) {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                                                new BlockPos(-1, -1, -1),
                                                EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
                                    }
                                    break;
                                case "Extra":
                                    if (event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                                                new BlockPos(-1, -1, -1),
                                                EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
                                    }
                                    break;
                                case "HandPacket":
                                    if (event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
                                    }
                                    break;
                                case "Post":
                                    if (event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                                    } else {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                                                new BlockPos(-1, -1, -1),
                                                EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
                                    }
                                    break;
                            }
                        }
                    }
            );
        }
        if (getUseItem() instanceof ItemFood || getUseItem() instanceof ItemBucketMilk || getUseItem() instanceof ItemPotion) {
            foodMode.getValue().forEach(
                    (mode, value) -> {
                        if (value) {
                            switch (mode) {
                                case "Switch":
                                    if (event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 8 + 1));
                                        PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                                    }
                                    break;
                                case "NoGround":
                                    event.setOnGround(false);
                                    break;
                                case "Jump":
                                    if (mc.player.onGround && event.isPost()) {
                                        mc.player.tryJump();
                                    }
                                    break;
                                case "Extra":
                                    PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(-1, -1, -1), EnumFacing.DOWN, mc.player.getActiveHand(), 0f, 0f, 0f));
                                    break;
                                case "Clip":
                                    event.setY(event.getY() + 1e-14);
                                    break;
                            }
                        }
                    }
            );
        }
        if (getUseItem() instanceof ItemBow) {
            bowMode.getValue().forEach(
                    (mode, value) -> {
                        if (value) {
                            switch (mode) {
                                case "Switch":
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 8 + 1));
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                                    break;
                                case "Extra":
                                    PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(-1, -1, -1), EnumFacing.DOWN, mc.player.getActiveHand(), 0f, 0f, 0f));
                                    break;
                            }
                        }
                    }
            );
        }
    }
    
    @EventHandler
    public void onSlowDown(ItemSlowDownEvent event) {
//        println(getUseItem());
        if (PlayerUtils.isBlocking()) {
            event.setForwardMultiplier(blockingForward.getValue().floatValue());
            event.setStrafeMultiplier(blockingStrafe.getValue().floatValue());
        }
        if (getUseItem() instanceof ItemFood || getUseItem() instanceof ItemBucketMilk || getUseItem() instanceof ItemPotion) {
            event.setForwardMultiplier(foodForward.getValue().floatValue());
            event.setStrafeMultiplier(foodStrafe.getValue().floatValue());
        }
        if (getUseItem() instanceof ItemBow) {
            event.setForwardMultiplier(bowForward.getValue().floatValue());
            event.setStrafeMultiplier(bowStrafe.getValue().floatValue());
        }
    }
}
