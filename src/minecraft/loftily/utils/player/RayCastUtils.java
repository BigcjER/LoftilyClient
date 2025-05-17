package loftily.utils.player;

import com.google.common.base.Predicates;
import loftily.utils.client.ClientUtils;
import loftily.utils.math.CalculateUtils;
import loftily.utils.math.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class RayCastUtils implements ClientUtils {
    public static RayTraceResult rayCastBlock(double blockReachDistance, Rotation rotation) {
        Vec3d vec3 = mc.player.getPositionEyes(1);
        Vec3d vec31 = mc.player.getLookVec().rotateYaw(rotation.yaw).rotatePitch(rotation.pitch);
        Vec3d vec32 = vec3.addVector(
                vec31.xCoord * blockReachDistance,
                vec31.yCoord * blockReachDistance,
                vec31.zCoord * blockReachDistance
        );
        
        return mc.player.world.rayTraceBlocks(vec3, vec32, false, false, true);
    }
    
    public static Entity rayCastEntity(double range, boolean throughWalls, Rotation rotation) {
        Entity entity = mc.getRenderViewEntity();
        Entity pointedEntity = null;
        
        if (entity != null && mc.world != null) {
            mc.mcProfiler.startSection("pick");
            double d0 = mc.playerController.getBlockReachDistance();
            Vec3d vec3 = entity.getPositionEyes(1);
            double d1 = CalculateUtils.getVectorForRotation(rotation).distanceTo(vec3);
            boolean flag = false;
            
            if (mc.playerController.extendedReach()) {
                d0 = 6.0D;
            } else if (d0 > 3.0D) {
                flag = true;
            }
            
            Vec3d vec31 = entity.getLook(1);
            Vec3d vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            Vec3d vec33 = null;
            float f = 1.0F;
            
            if (!throughWalls) {
                RayTraceResult blockHit = mc.world.rayTraceBlocks(vec3, vec32, false, true, false);
                if (blockHit != null && vec3.distanceTo(blockHit.hitVec) <= range) {
                    mc.mcProfiler.endSection();
                    return null;
                }
            }
            
            
            List<Entity> list = mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
            double d2 = d1;
            
            for (Entity value : list) {
                float f1 = (value).getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = (value).getEntityBoundingBox().expand(f1, f1, f1);
                RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                
                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = value;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                    
                    if (d3 < d2 || d2 == 0.0D) {
                        boolean flag1 = false;
                        
                        
                        if (!flag1 && value == entity.getRidingEntity()) {
                            if (d2 == 0.0D) {
                                pointedEntity = value;
                                vec33 = movingobjectposition.hitVec;
                            }
                        } else {
                            pointedEntity = value;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }
            
            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > range) {
                pointedEntity = null;
            }
            
            if (pointedEntity != null && (d2 < d1)) {
                if (pointedEntity instanceof EntityLivingBase) {
                    return pointedEntity;
                }
            }
            
            mc.mcProfiler.endSection();
        }
        
        return pointedEntity;
    }
}
