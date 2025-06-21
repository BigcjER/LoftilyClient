package loftily.utils.player;

import loftily.utils.client.ClientUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class PlayerUtils implements ClientUtils {
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
    
    public static boolean isUsingItem() {
        return mc.player.isHandActive();
    }
}
