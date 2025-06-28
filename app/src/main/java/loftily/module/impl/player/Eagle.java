package loftily.module.impl.player;


import loftily.event.impl.world.PreUpdateEvent;
import loftily.handlers.impl.player.MoveHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.RandomUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

@ModuleInfo(name = "Eagle",category = ModuleCategory.PLAYER)
public class Eagle extends Module {
    private final RangeSelectionNumberValue sneakTime = new RangeSelectionNumberValue("SneakTime",20,60,0,500);
    private final BooleanValue predict = new BooleanValue("Predict",false);
    private final NumberValue predictMotion = new NumberValue("PredictAmount",0.5,-1.0,1.0,0.01).setVisible(predict::getValue);
    private final BooleanValue onlyGround = new BooleanValue("OnlyGround",false);

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        IBlockState blockState = !predict.getValue() ? mc.world.getBlockState(new BlockPos(mc.player.posX,
                mc.player.posY - 1.0,
                mc.player.posZ)) : mc.world.getBlockState(new BlockPos(mc.player.posX + mc.player.motionX * predictMotion.getValue(),
                mc.player.posY - 1.0,
                mc.player.posZ + mc.player.motionZ * predictMotion.getValue()));
        if(!(blockState.getBlock() instanceof BlockAir))return;

        if(!onlyGround.getValue() || mc.player.onGround)
        {
            MoveHandler.setSneak(true,RandomUtils.randomInt((int)sneakTime.getFirst(),(int)sneakTime.getSecond()));
        }
    }
}
