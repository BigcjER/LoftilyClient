package loftily.utils.player;

import loftily.utils.client.ClientUtils;
import loftily.utils.math.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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
}
