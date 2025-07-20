package loftily.module.impl.movement.speeds;

import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.event.impl.world.PreUpdateEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.impl.player.CombatHandler;
import loftily.module.impl.movement.Speed;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static net.minecraft.util.math.MathHelper.sqrt;

public class BlocksMCV2Speed extends Mode<Speed> {
    public BlocksMCV2Speed() {
        super("BlocksMCV2");
    }

    private final BooleanValue lowHop = new BooleanValue("BlocksMCV2-LowHop",false);
    private final BooleanValue damageBoost = new BooleanValue("BlocksMCV2-DamageBoost",false);
    private final BooleanValue fullStrafe = new BooleanValue("BlocksMCV2-FullStrafe",false);

    @EventHandler(priority = -1000)
    public void onJump(JumpEvent event) {
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!mc.player.onGround) {
            if (mc.player.offGroundTicks == 4 && lowHop.getValue() && mc.player.hurtTime <= 6) {
                mc.player.motionY = -0.09800000190734863;
            }
            if (damageBoost.getValue() && CombatHandler.inCombat) {
                if (mc.player.hurtTime >= 6) {
                    switch (mc.player.hurtTime) {
                        case 10:
                        case 9:
                        case 8:
                            MoveUtils.setSpeed(Math.max(0.48, MoveUtils.getSpeed()), false);
                            break;
                        case 7:
                        case 6:
                            MoveUtils.setSpeed(Math.max(0.45, MoveUtils.getSpeed()), false);
                            break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if (MoveUtils.isMoving()) {
            if (mc.player.onGround) {
                MoveUtils.setSpeed(mc.player.movementInput.moveForward < 0 ? 0.215 : 0.275,false);
                mc.player.tryJump();
            }
        }
    }

    @EventHandler
    public void onStrafe(StrafeEvent event) {
        if(!fullStrafe.getValue())return;
        if(MoveUtils.isMoving()) {
            if (!mc.player.onGround) {
                if(mc.player.offGroundTicks < 6) {
                    double shotSpeed = sqrt((mc.player.motionX * mc.player.motionX) + (mc.player.motionZ * mc.player.motionZ));
                    double speed = (shotSpeed * 0.83);
                    double motionX = (mc.player.motionX * 0.17);
                    double motionZ = (mc.player.motionZ * 0.17);
                    double yaw = MoveUtils.getMovingYaw();
                    mc.player.motionX = (((-sin(Math.toRadians(yaw)) * speed) + motionX));
                    mc.player.motionZ = (((cos(Math.toRadians(yaw)) * speed) + motionZ));
                }else {
                    MoveUtils.strafe();
                }
            }
        }
    }
}
