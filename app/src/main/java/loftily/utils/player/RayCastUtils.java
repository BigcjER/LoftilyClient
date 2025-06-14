package loftily.utils.player;

import com.google.common.base.Function;
import loftily.utils.client.ClientUtils;
import loftily.utils.math.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static loftily.utils.math.CalculateUtils.getVectorForRotation;
import static loftily.utils.math.CalculateUtils.isVisible;

public class RayCastUtils implements ClientUtils {
    public static RayTraceResult rayTraceWithCustomRotation(Entity entity, double blockReachDistance, float yaw, float pitch) {
        Vec3d vec3 = entity.getPositionEyes(1f);
        Vec3d vec31 = entity.getVectorForRotation(pitch, yaw);
        Vec3d vec32 = vec3.addVector(
                vec31.xCoord * blockReachDistance,
                vec31.yCoord * blockReachDistance,
                vec31.zCoord * blockReachDistance
        );
        
        return entity.world.rayTraceBlocks(vec3, vec32, false, false, true);
    }
    
    public static RayTraceResult rayTraceWithCustomRotation(Entity entity, double blockReachDistance, Rotation rotation) {
        return rayTraceWithCustomRotation(entity, blockReachDistance, rotation.yaw, rotation.pitch);
    }
    
    public static boolean overBlock(Rotation rotation, EnumFacing enumFacing, BlockPos pos, boolean strict) {
        RayTraceResult movingObjectPosition = rayTraceWithCustomRotation(mc.player, 4.5, rotation.yaw, rotation.pitch);
        if (movingObjectPosition == null) {
            return false;
        }
        Vec3d hitVec = movingObjectPosition.hitVec;
        if (hitVec == null) {
            return false;
        }
        
        return movingObjectPosition.getBlockPos().equals(pos) && (!strict || movingObjectPosition.sideHit == enumFacing);
    }
    
    
    public static Entity raycastEntity(
            double range,
            float yaw,
            float pitch,
            Boolean throughWalls,
            Function<Entity, Boolean> entityFilter
    ) {
        Entity renderViewEntity = mc.getRenderViewEntity();
        
        if (renderViewEntity == null || mc.world == null)
            return null;
        
        final double[] blockReachDistance = {range};
        Vec3d eyePosition = renderViewEntity.getEyes();
        Vec3d entityLook = getVectorForRotation(new Rotation(yaw, pitch));
        Vec3d vec = eyePosition.add(entityLook.scale(blockReachDistance[0]));
        
        List<Entity> entityList = mc.world.getEntities(Entity.class, entity ->
                entity != null && (entity instanceof EntityLivingBase || entity instanceof EntityLargeFireball) &&
                        !(entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator()) &&
                        entity.canBeCollidedWith() &&
                        entity != renderViewEntity
        );
        
        final Entity[] pointedEntity = {null};
        
        for (Entity entity : entityList) {
            if (!entityFilter.apply(entity)) continue;
            
            boolean[] checkResult = {false};
            Runnable checkEntity = () -> {
                AxisAlignedBB axisAlignedBB = entity.getBox();
                
                RayTraceResult movingObjectPosition = axisAlignedBB.calculateIntercept(eyePosition, vec);
                
                if (axisAlignedBB.isVecInside(eyePosition)) {
                    if (blockReachDistance[0] >= 0.0) {
                        pointedEntity[0] = entity;
                        blockReachDistance[0] = 0.0;
                    }
                } else if (movingObjectPosition != null) {
                    if (!isVisible(movingObjectPosition.hitVec) && !throughWalls) {
                        return;
                    }
                    double eyeDistance = eyePosition.distanceTo(movingObjectPosition.hitVec);
                    
                    if (eyeDistance < blockReachDistance[0] || blockReachDistance[0] == 0.0) {
                        if (entity == renderViewEntity.getRidingEntity()) {
                            if (blockReachDistance[0] == 0.0) pointedEntity[0] = entity;
                        } else {
                            pointedEntity[0] = entity;
                            blockReachDistance[0] = eyeDistance;
                        }
                    }
                }
                
                checkResult[0] = false;
            };
            
            // Check newest entity first
            checkEntity.run();
        }
        
        return pointedEntity[0];
    }
}
