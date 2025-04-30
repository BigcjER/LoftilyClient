package loftily.utils.player;

import loftily.utils.client.MinecraftInstance;

public class MoveUtils implements MinecraftInstance {
    public static double getDirection() {
        float rotationYaw = mc.player.rotationYaw;
        if (mc.player.movementInput.moveForward < 0F)
            rotationYaw += 180F;
        float forward = 1F;
        if (mc.player.movementInput.moveForward < 0F)
            forward = -0.5F;
        else if (mc.player.movementInput.moveForward > 0F)
            forward = 0.5F;

        if (mc.player.moveStrafing > 0F)
            rotationYaw -= 90F * forward;
        if (mc.player.moveStrafing < 0F)
            rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static boolean isMoving() {
        return mc.player.movementInput.moveForward != 0f || mc.player.movementInput.moveStrafe != 0f;
    }

    public static void setSpeed(final double speed) {
        if (!isMoving())
            return;

        final double yaw = getDirection();
        mc.player.motionX = -Math.sin(yaw) * speed;
        mc.player.motionZ = Math.cos(yaw) * speed;
    }

    public static void stop() {
        mc.player.motionX = 0;
        mc.player.motionZ = 0;
    }
}
