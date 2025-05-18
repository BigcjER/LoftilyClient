package loftily.module.impl.other;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.MessageUtils;
import loftily.value.impl.MultiBooleanValue;
import net.lenni0451.lambdaevents.EventHandler;

@SuppressWarnings("unused")
@ModuleInfo(name = "Test", category = ModuleCategory.Other)
public class Test extends Module {
    private final MultiBooleanValue multiBooleanValue = new MultiBooleanValue("TestMultiBoolean")
            .add("Test1", true)
            .add("Test2", false)
            .add("Test3", false)
            .add("Test4", true)
            .add("Test5", true);
    
    
    @EventHandler
    public void onPacket(PacketSendEvent event) {
        MessageUtils.clientMessage(event.getPacket());
    }
}
