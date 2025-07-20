package loftily.module.impl.movement;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.player.slowdown.ItemSlowDownEvent;
import loftily.event.impl.world.WorldLoadEvent;
import loftily.handlers.impl.player.MoveHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.player.PlayerUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.MultiBooleanValue;
import loftily.value.impl.NumberValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketWindowItems;
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
            .add("MainHand", false)
            .add("OffHandPlace",false)
            .add("OldGrim",false)
            .add("Post", false);
    
    private final MultiBooleanValue foodMode = new MultiBooleanValue("FoodMode")
            .add("Switch", false)
            .add("Extra", false)
            .add("Jump", false)
            .add("NoGround", false)
            .add("Clip", false)
            .add("HandPacket", false)
            .add("OldGrim",false)
            .add("MainHand", false);
    
    private final MultiBooleanValue bowMode = new MultiBooleanValue("BowMode")
            .add("Switch", false)
            .add("Extra", false)
            .add("HandPacket", false)
            .add("OldGrim",false)
            .add("MainHand", false);
    
    private final NumberValue blockingForward = new NumberValue("BlockingForward", 0.2f, 0.0f, 1.0f, 0.01f);
    private final NumberValue blockingStrafe = new NumberValue("BlockingStrafe", 0.2f, 0.0f, 1.0f, 0.01f);
    
    private final NumberValue foodForward = new NumberValue("FoodForward", 0.2f, 0.0f, 1.0f, 0.01f);
    private final NumberValue foodStrafe = new NumberValue("FoodStrafe", 0.2f, 0.0f, 1.0f, 0.01f);
    
    private final NumberValue bowForward = new NumberValue("BowForward", 0.2f, 0.0f, 1.0f, 0.01f);
    private final NumberValue bowStrafe = new NumberValue("BowStrafe", 0.2f, 0.0f, 1.0f, 0.01f);
    
    private final BooleanValue ignoreServerItemChange = new BooleanValue("IgnoreServerItemChange", false);
    private boolean blocking = false;

    @Override
    public void onToggle(){
        blocking = false;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        blocking = false;
    }
    
    public Item getUseItem() {
        if (PlayerUtils.isUsingItem()) {
            return mc.player.getHeldItem(mc.player.getActiveHand()).getItem();
        } else return null;
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if (mc.player == null || !PlayerUtils.isUsingItem() || getUseItem() == null) return;
        
        if (PlayerUtils.isBlocking()) {
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
                                case "OffHandPlace":
                                    if(event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                                                new BlockPos(-1, -1, -1),
                                                EnumFacing.DOWN, EnumHand.OFF_HAND, 0.0F, 0.0F, 0.0F));
                                    }
                                    break;
                                case "OldGrim":
                                    if(event.isPre()) {
                                        if (blocking) {
                                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                                            blocking = false;
                                        }
                                    }else {
                                        PacketUtils.sendPacket(new CPacketConfirmTransaction(1, (short) RandomUtils.randomInt(-999999,999999),true),false);
                                        mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
                                        blocking = true;
                                    }
                                    break;
                                case "MainHand":
                                    if (event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
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
                                    if(event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(-1, -1, -1), EnumFacing.DOWN, mc.player.getActiveHand(), 0f, 0f, 0f));
                                    }
                                    break;
                                case "Clip":
                                    event.setY(event.getY() + 1e-14);
                                    break;
                                case "HandPacket":
                                    if (event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
                                    }
                                    break;
                                case "OldGrim":
                                    if(event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketClickWindow(0, 36, 0, ClickType.SWAP, new ItemStack(Block.getBlockById(166)), (short) 0));
                                    }
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 8 + 1));
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 7 + 2));
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                                    break;
                                case "MainHand":
                                    if (event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                                    }
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
                                    if(event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 8 + 1));
                                        PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                                    }
                                    break;
                                case "Extra":
                                    if(event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(-1, -1, -1), EnumFacing.DOWN, mc.player.getActiveHand(), 0f, 0f, 0f));
                                    }
                                    break;
                                case "HandPacket":
                                    if (event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
                                    }
                                    break;
                                case "OldGrim":
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 8 + 1));
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem % 7 + 2));
                                    PacketUtils.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                                    break;
                                case "MainHand":
                                    if (event.isPre()) {
                                        PacketUtils.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                                    }
                                    break;
                            }
                        }
                    }
            );
        }
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent event){
        Packet<?> packet = event.getPacket();
        if(
                (foodMode.getValue("OldGrim") && (getUseItem() instanceof ItemFood || getUseItem() instanceof ItemBucketMilk || getUseItem() instanceof ItemPotion))
                || (bowMode.getValue("OldGrim") && (getUseItem() instanceof ItemBow))
        ){
            if(packet instanceof CPacketPlayerTryUseItem){
                MoveHandler.setMovement(0,0,0);
            }
            if(packet instanceof CPacketPlayerDigging && ((CPacketPlayerDigging) packet).getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM){
                MoveHandler.setMovement(0,0,0);
            }
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event){
        Packet<?> packet = event.getPacket();
        if(
                (foodMode.getValue("OldGrim") && (getUseItem() instanceof ItemFood || getUseItem() instanceof ItemBucketMilk || getUseItem() instanceof ItemPotion))
                        || (bowMode.getValue("OldGrim") && (getUseItem() instanceof ItemBow))
        ){
            if(packet instanceof SPacketWindowItems){
                event.setCancelled(true);
            }
        }
        if (!ignoreServerItemChange.getValue()) return;
        if (packet instanceof SPacketHeldItemChange) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSlowDown(ItemSlowDownEvent event) {
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
