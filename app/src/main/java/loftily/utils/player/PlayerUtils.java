package loftily.utils.player;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import loftily.utils.client.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSword;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
            return entity.getHeldItem(entity.getActiveHand()).getItem() instanceof ItemShield;
        }
    }
    
    public static boolean isBlocking() {
        return isBlocking(mc.player);
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
