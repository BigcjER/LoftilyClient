package loftily.module.impl.other;

import loftily.Client;
import loftily.event.impl.client.ClientTickEvent;
import loftily.gui.notification.NotificationType;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.ServerUtils;
import loftily.utils.client.MessageUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.TextValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.util.text.TextFormatting;

@ModuleInfo(name = "Spammer", category = ModuleCategory.OTHER)
public class Spammer extends Module {
    private final RangeSelectionNumberValue delay = new RangeSelectionNumberValue("Delay", 1000, 2000, 0, 10000);
    private final TextValue text = new TextValue("Text", "");
    private final BooleanValue randomSuffix = new BooleanValue("RandomSuffix", false);
    private final DelayTimer timer = new DelayTimer();
    
    @EventHandler
    public void onTick(ClientTickEvent event) {
        if (ServerUtils.getServerIp().contains("loyisa.cn")) {
            if (isToggled())
                setToggled(false, true, false, true);
            
            Client.INSTANCE.getNotificationManager().add(NotificationType.WARNING, "Spammer", "Auto disabled Spammer on loyisa.cn", 0);
            
            return;
        }
        
        if (text.getValue().isEmpty()) {
            MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + "Text couldn't be empty!");
            if (isToggled())
                setToggled(false, true, true, true);
            return;
        }
        
        if (!timer.hasTimeElapsed(RandomUtils.randomInt((int) delay.getFirst(), (int) delay.getSecond()))) return;
        timer.reset();
        
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(text.getValue());
        if (randomSuffix.getValue())
            stringBuilder.append(" >").append(RandomUtils.randomString(5)).append("<");
        
        MessageUtils.sendMessage(stringBuilder.toString());
    }
}
