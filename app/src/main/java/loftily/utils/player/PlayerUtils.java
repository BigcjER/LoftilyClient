package loftily.utils.player;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import loftily.Client;
import loftily.module.impl.combat.KillAura;
import loftily.utils.client.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSword;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class PlayerUtils implements ClientUtils {
    public static boolean nullCheck() {
        return mc.player != null && mc.world != null;
    }
    
    public static boolean canBeSeenEntity(Entity player, Entity target) {
        AxisAlignedBB targetBB = target.getEntityBoundingBox();
        
        Vec3d center = new Vec3d(
                (targetBB.minX + targetBB.maxX) / 2,
                (targetBB.minY + targetBB.maxY) / 2,
                (targetBB.minZ + targetBB.maxZ) / 2
        );
        
        Vec3d[] corners = new Vec3d[]{
                new Vec3d(targetBB.minX, targetBB.minY, targetBB.minZ),
                new Vec3d(targetBB.minX, targetBB.minY, targetBB.maxZ),
                new Vec3d(targetBB.minX, targetBB.maxY, targetBB.minZ),
                new Vec3d(targetBB.minX, targetBB.maxY, targetBB.maxZ),
                new Vec3d(targetBB.maxX, targetBB.minY, targetBB.minZ),
                new Vec3d(targetBB.maxX, targetBB.minY, targetBB.maxZ),
                new Vec3d(targetBB.maxX, targetBB.maxY, targetBB.minZ),
                new Vec3d(targetBB.maxX, targetBB.maxY, targetBB.maxZ)
        };
        
        Vec3d eyePos = player.getPositionEyes(1.0F);
        
        if (player.world.rayTraceBlocks(eyePos, center) == null) return true;
        
        for (Vec3d corner : corners) {
            if (player.world.rayTraceBlocks(eyePos, corner) == null) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isUsingItem(EntityLivingBase entity) {
        return entity.isHandActive();
    }
    
    public static boolean isUsingItem() {
        return isUsingItem(mc.player);
    }
    
    public static boolean isBlocking(EntityLivingBase entity) {
        if (!isUsingItem(entity)) return false;
        if (ViaLoadingBase.getInstance().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return entity.getHeldItemMainhand().getItem() instanceof ItemSword;
        } else {
            return entity.getHeldItemOffhand().getItem() instanceof ItemShield || entity.getHeldItemMainhand().getItem() instanceof ItemShield;
        }
    }
    
    public static boolean isBlocking() {
        return isBlocking(mc.player);
    }
    
    public static boolean nearAir() {
        BlockPos blockPos = new BlockPos(mc.player).down();
        BlockPos blockPos2 = blockPos.offset(mc.player.getHorizontalFacing());
        Block block = Objects.requireNonNull(blockPos2.getState()).getBlock();
        return block instanceof BlockAir;
    }
    
    public static boolean isDiagonally() {
        float directionDegree = mc.player.rotationYaw;
        float yaw = Math.round(Math.abs(MathHelper.wrapAngleTo180_float(directionDegree)) / 45f) * 45f;
        return yaw % 90 != 0f;
    }
    
    public static boolean onRightSide(EntityPlayerSP player) {
        IBlockState blockState = mc.world.getBlockState(new BlockPos(player));
        AxisAlignedBB block = blockState.getSelectedBoundingBox(mc.world, new BlockPos(player));
        boolean right = false;
        
        switch (player.getHorizontalFacing()) {
            case EAST:
                right = player.posZ <= block.minZ + (block.maxZ - block.minZ) * 0.5;
                break;
            case WEST:
                right = player.posZ >= block.minZ + (block.maxZ - block.minZ) * 0.5;
                break;
            case NORTH:
                right = player.posX <= block.minX + (block.maxX - block.minX) * 0.5;
                break;
            case SOUTH:
                right = player.posX >= block.minX + (block.maxX - block.minX) * 0.5;
                break;
        }
        return right;
    }
    
    public static boolean isInVoid() {
        if (mc.player.posY <= 0) return true;
        
        if (mc.player.isOnLadder() ||
                mc.player.capabilities.allowFlying ||
                mc.player.capabilities.disableDamage ||
                mc.player.isSpectator() ||
                mc.player.isInWater() ||
                mc.player.isInLava() ||
                mc.player.isInWeb ||
                mc.player.onGround) return false;
        
        for (int i = (int) mc.player.posY; i > -1; i--) {
            Block block = (new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ).down(i)).getBlock();
            
            if (block != Blocks.AIR) {
                return false;
            }
        }
        return true;
    }
    
}
