package loftily.utils.math;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class CalculateUtils {

    public static Vec3d getNearestPointBB(Vec3d eye, AxisAlignedBB box) {
        double[] origin = new double[]{eye.xCoord, eye.yCoord, eye.zCoord};
        double[] destMins = new double[]{box.minX, box.minY, box.minZ};
        double[] destMaxs = new double[]{box.maxX, box.maxY, box.maxZ};

        for (int i = 0; i < 3; i++) {
            if (origin[i] > destMaxs[i]) {
                origin[i] = destMaxs[i];
            } else if (origin[i] < destMins[i]) {
                origin[i] = destMins[i];
            }
        }

        return new Vec3d(origin[0], origin[1], origin[2]);
    }

}
