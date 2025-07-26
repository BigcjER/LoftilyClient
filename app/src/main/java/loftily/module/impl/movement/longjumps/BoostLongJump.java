package loftily.module.impl.movement.longjumps;

import loftily.module.impl.movement.LongJump;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;

public class BoostLongJump extends Mode<LongJump> {
    public BoostLongJump() {
        super("Boost");
    }
    private final NumberValue boostSpeed = new NumberValue("Boost-Speed", 3, 0, 5, 0.01);
    private final NumberValue motion = new NumberValue("Boost-Motion", 0.42, -5, 5, 0.01);

    @Override
    public void onEnable(){
        MoveUtils.setSpeed(boostSpeed.getValue(),false);
        mc.player.motionY = motion.getValue();
        getParent().autoDisable();
    }
}
