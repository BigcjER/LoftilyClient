package loftily.utils.math;

import loftily.handlers.impl.RotationHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;
import static loftily.handlers.impl.RotationHandler.moveFix;
import static loftily.utils.client.ClientUtils.mc;
import static net.minecraft.util.math.MathHelper.abs;
import static net.minecraft.util.math.MathHelper.ceil;

public class CalculateUtils {

    public static float getMoveFixForward(Rotation rotation) {
        float playerDirection = moveFix == RotationHandler.MoveFix.SILENT ? mc.player.rotationYaw : Math.round(mc.player.rotationYaw / 45f) * 45f;
        float diff = (playerDirection - rotation.yaw) * (float) (Math.PI / 180);

        float calcForward;

        float strafe = mc.player.movementInput.moveStrafe;
        float forward = mc.player.movementInput.moveForward;

        float modifiedForward = ceil(abs(forward)) * Math.signum(forward);
        float modifiedStrafe = ceil(abs(strafe)) * Math.signum(strafe);

        calcForward = round(modifiedForward * MathHelper.cos(diff) + modifiedStrafe * MathHelper.sin(diff));

        float f = (forward != 0f) ? forward : strafe;

        calcForward *= abs(f);

        return calcForward;
    }

    public static Vec3d getClosestPoint(Vec3d vec3, AxisAlignedBB box) {
        double x = MathHelper.clamp(vec3.xCoord, box.minX, box.maxX);
        double y = MathHelper.clamp(vec3.yCoord, box.minY, box.maxY);
        double z = MathHelper.clamp(vec3.zCoord, box.minZ, box.maxZ);
        return new Vec3d(x, y, z);
    }

    public static double getClosetDistance(EntityLivingBase player, EntityLivingBase target) {
        return player.getEyes().distanceTo(getClosestPoint(player.getEyes(),target.getBox()));
    }

    public static Boolean isVisible(Vec3d vec) {
        return mc.world.rayTraceBlocks(mc.player.getPositionEyes(1f), vec) == null;
    }

    public static List<BlockPos> searchBlocks(double xR, double yR, double zR) {
        List<BlockPos> blocks = new ArrayList<>();

        for (int x = (int) xR; x >= -xR + 1; x -= 1) {
            for (int y = (int) yR; y >= -yR + 1; y -= 1) {
                for (int z = (int) zR; z >= -zR + 1; z -= 1) {
                    BlockPos blockPos = new BlockPos((int) Math.round(mc.player.posX + x), (int) Math.round(mc.player.posY - 1.0 + y),
                            (int) Math.round(mc.player.posZ + z));

                    blocks.add(blockPos);
                }
            }
        }

        return blocks;
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
