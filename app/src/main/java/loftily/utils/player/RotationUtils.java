package loftily.utils.player;

import loftily.handlers.impl.RotationHandler;
import loftily.utils.client.ClientUtils;
import loftily.utils.math.Rotation;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static java.lang.Math.hypot;
import static net.minecraft.util.math.MathHelper.atan2;
import static net.minecraft.util.math.MathHelper.sqrt;

public class RotationUtils implements ClientUtils {
    public static Rotation toRotation(Vec3d vec, Entity fromEntity) {
        Vec3d eyesPos = fromEntity.getEyes();
        
        double diffX = vec.xCoord - eyesPos.xCoord;
        double diffY = vec.yCoord - eyesPos.yCoord;
        double diffZ = vec.zCoord - eyesPos.zCoord;
        
        return new Rotation(
                MathHelper.wrapAngleTo180_float(
                        (float) Math.toDegrees(atan2(diffZ, diffX)) - 90f
                ),
                MathHelper.wrapAngleTo180_float(
                        (float) -Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))
                )
        );
    }

    public static float getRotationDifference(Rotation r1, Rotation r2) {
        return (float) hypot(getAngleDifference(r1.yaw, r2.yaw), (r1.pitch - r2.pitch));
    }

    public static float getAngleDifference(float d1, float d2) {
        return ((d1 - d2) % 360f + 540f) % 360f - 180f;
    }
    
    public static Rotation smoothRotation(Rotation from, Rotation to, float yawSpeed, float pitchSpeed) {
        float yawDifference = RotationUtils.getAngleDifference(to.yaw, from.yaw);
        float pitchDifference = RotationUtils.getAngleDifference(to.pitch, from.pitch);
        
        return new Rotation(
                from.yaw + (yawDifference > yawSpeed ? yawSpeed : Math.max(-yawSpeed, yawDifference)),
                from.pitch + (pitchDifference > pitchSpeed ? pitchSpeed : Math.max(-pitchSpeed, pitchDifference))
        );
    }

    public static Rotation findBestRotationMultiCriteria(Entity player, Entity target) {
        AxisAlignedBB targetBB = target.getBox();

        Vec3d[] points = new Vec3d[]{
                new Vec3d((targetBB.minX + targetBB.maxX) / 2, (targetBB.minY + targetBB.maxY) / 2, (targetBB.minZ + targetBB.maxZ) / 2),
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
        float currentYaw = player.rotationYaw;
        float currentPitch = player.rotationPitch;

        Rotation bestRotation = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // 权重设置（可调）
        double weightAngle = 0.3;
        double weightDistance = 0.1;
        double weightHeight = 0.6;

        for (Vec3d point : points) {
            if (player.world.rayTraceBlocks(eyePos, point) == null) {
                Rotation rot = toRotation(point, player);

                double yawDiff = MathHelper.wrapAngleTo180_float(rot.yaw - currentYaw);
                double pitchDiff = MathHelper.wrapAngleTo180_float(rot.pitch - currentPitch);
                double angleDiff = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

                double distance = eyePos.distanceTo(point);
                double heightDiff = Math.abs(point.yCoord - eyePos.yCoord);

                // 归一化方法（简单示例，你可以用更复杂的方法）
                // 角度差和距离越小越好，转换成得分时用负值
                double angleScore = -angleDiff;
                double distanceScore = -distance;
                double heightScore = -heightDiff;

                // 计算总分
                double score = weightAngle * angleScore + weightDistance * distanceScore + weightHeight * heightScore;

                if (score > bestScore) {
                    bestScore = score;
                    bestRotation = rot;
                }
            }
        }

        return bestRotation;
    }

    public static Rotation findBestRotationSimulatedAnnealing(Entity player, Entity target) {
        AxisAlignedBB targetBB = target.getBox();

        Vec3d eyePos = player.getPositionEyes(1.0F);
        float currentYaw = player.rotationYaw;
        float currentPitch = player.rotationPitch;

        // 初始点：包围盒中心
        Vec3d currentPoint = new Vec3d(
                (targetBB.minX + targetBB.maxX) / 2,
                (targetBB.minY + targetBB.maxY) / 2,
                (targetBB.minZ + targetBB.maxZ) / 2
        );

        double currentEnergy = calculateEnergy(currentPoint, player, eyePos, currentYaw, currentPitch);

        Vec3d bestPoint = currentPoint;
        double bestEnergy = currentEnergy;

        double temperature = 1.0;
        double coolingRate = 0.003;

        java.util.Random rand = new java.util.Random();

        while (temperature > 1e-4) {
            // 在包围盒附近随机生成新点 (邻域搜索)
            double newX = clamp(currentPoint.xCoord + (rand.nextDouble() * 2 - 1) * 0.5, targetBB.minX, targetBB.maxX);
            double newY = clamp(currentPoint.yCoord + (rand.nextDouble() * 2 - 1) * 0.5, targetBB.minY, targetBB.maxY);
            double newZ = clamp(currentPoint.zCoord + (rand.nextDouble() * 2 - 1) * 0.5, targetBB.minZ, targetBB.maxZ);

            Vec3d newPoint = new Vec3d(newX, newY, newZ);

            // 射线检测确保可视
            if (player.world.rayTraceBlocks(eyePos, newPoint) != null) {
                // 被遮挡，跳过
                temperature *= 1 - coolingRate;
                continue;
            }

            double newEnergy = calculateEnergy(newPoint, player, eyePos, currentYaw, currentPitch);

            // 模拟退火接受概率
            if (acceptanceProbability(currentEnergy, newEnergy, temperature) > rand.nextDouble()) {
                currentPoint = newPoint;
                currentEnergy = newEnergy;

                if (newEnergy < bestEnergy) {
                    bestPoint = newPoint;
                    bestEnergy = newEnergy;
                }
            }

            temperature *= 1 - coolingRate;
        }

        return toRotation(bestPoint, player);
    }

    private static double calculateEnergy(Vec3d point, Entity player, Vec3d eyePos, float currentYaw, float currentPitch) {
        Rotation rot = toRotation(point, player);
        double yawDiff = MathHelper.wrapAngleTo180_float(rot.yaw - currentYaw);
        double pitchDiff = MathHelper.wrapAngleTo180_float(rot.pitch - currentPitch);
        double angleDiff = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

        double distance = eyePos.distanceTo(point);
        double heightDiff = Math.abs(point.yCoord - eyePos.yCoord);

        // 权重同之前示例
        double weightAngle = 0.3;
        double weightDistance = 0.2;
        double weightHeight = 0.5;

        // 注意这里是能量函数，越低越好，所以要用正分数
        return weightAngle * angleDiff + weightDistance * distance + weightHeight * heightDiff;
    }

    private static double acceptanceProbability(double currentEnergy, double newEnergy, double temperature) {
        if (newEnergy < currentEnergy) {
            return 1.0;
        }
        return Math.exp((currentEnergy - newEnergy) / temperature);
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
