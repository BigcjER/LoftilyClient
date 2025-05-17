package loftily.utils.math;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static loftily.utils.client.ClientUtils.mc;

public class CalculateUtils {
    
    public static Vec3d getClosestPoint(Vec3d vec3, AxisAlignedBB box) {
        double x = MathHelper.clamp(vec3.xCoord, box.minX, box.maxX);
        double y = MathHelper.clamp(vec3.yCoord, box.minY, box.maxY);
        double z = MathHelper.clamp(vec3.zCoord, box.minZ, box.maxZ);
        return new Vec3d(x, y, z);
    }

    public static double getClosetDistance(EntityLivingBase player, EntityLivingBase target) {
        return player.getEyes().distanceTo(getClosestPoint(player.getEyes(),target.getHitBox()));
    }

    public static Boolean isVisible(Vec3d vec) {
        return mc.world.rayTraceBlocks(mc.player.getPositionEyes(1f), vec) == null;
    }

    public static Vec3d getVectorForRotation(Rotation rotation) {
        float f = MathHelper.cos(-rotation.yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-rotation.yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-rotation.pitch * 0.017453292F);
        float f3 = MathHelper.sin(-rotation.pitch * 0.017453292F);
        return new Vec3d(f1 * f2, f3, f * f2);
    }

    public static double getDistanceToEntity(Entity target, Entity entityIn) {
        if (target == null || entityIn == null) return 3;
        
        Vec3d eyePos = entityIn.getPositionEyes(1);
        Vec3d pos = getClosestPoint(eyePos, target.getEntityBoundingBox());
        
        double xDist = Math.abs(pos.xCoord - eyePos.xCoord);
        double yDist = Math.abs(pos.yCoord - eyePos.yCoord);
        double zDist = Math.abs(pos.zCoord - eyePos.zCoord);
        return Math.sqrt(Math.pow(xDist, 2.0) + Math.pow(yDist, 2.0) + Math.pow(zDist, 2.0));
    }

}
