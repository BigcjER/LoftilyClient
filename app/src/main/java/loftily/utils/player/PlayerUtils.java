package loftily.utils.player;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import loftily.utils.block.BlockUtils;
import loftily.utils.client.ClientUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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

    public static double fallDist() {
        if (overVoid()) {
            return 9999.0;
        } else {
            double fallDistance = -1.0;
            double y = mc.player.posY;
            if (mc.player.posY % 1.0 == 0.0) {
                y--;
            }

            for (int i = (int)Math.floor(y); i > -1; i--) {
                if (!BlockUtils.isPlaceable(new BlockPos(mc.player.posX, i, mc.player.posZ))) {
                    fallDistance = y - i;
                    break;
                }
            }

            return fallDistance - 1.0;
        }
    }

    public static boolean overVoid() {
        for (int i = (int)mc.player.posY; i > -1; i--) {
            if (!(mc.world.getBlockState(new BlockPos(mc.player.posX, i, mc.player.posZ)).getBlock() instanceof BlockAir)) {
                return false;
            }
        }

        return true;
    }

    public static boolean overVoid(double posX, double posY, double posZ) {
        for (int i = (int)posY; i > -1; i--) {
            if (!(mc.world.getBlockState(new BlockPos(posX, i, posZ)).getBlock() instanceof BlockAir)) {
                return false;
            }
        }

        return true;
    }
}
