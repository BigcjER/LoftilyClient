package loftily.module.impl.player;

import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;
import net.minecraft.block.BlockAir;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

@ModuleInfo(name = "NoFall", category = ModuleCategory.PLAYER)
public class NoFall extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", this,
            ModeValue.getModes(getClass().getPackage().getName() + ".nofalls")
    );
    private final NumberValue fallDistance = new NumberValue("MinFallDistance",3,0,8,0.01);
    private final BooleanValue noVoid = new BooleanValue("NoVoid", true);

    public boolean fallDamage(){
        return mc.player.fallDistance - mc.player.motionY > fallDistance.getValue();
    }

    public boolean inVoidCheck(){
        if(!noVoid.getValue()){
            return true;
        }else{
            for (int i = 0; i < mc.player.posY; i++) {
                BlockPos pos = new BlockPos(mc.player.posX, i,mc.player.posZ);
                if(!(Objects.requireNonNull(pos.getState()).getBlock() instanceof BlockAir)){
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
