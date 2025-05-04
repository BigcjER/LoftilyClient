package loftily.utils.client;

import loftily.Client;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class MessageUtils implements ClientUtils {
    public static void clientMessageWithWaterMark(Object message) {
        clientMessage(String.format("%s %s",
                Client.StringPreFix,
                TextFormatting.WHITE + message.toString()));
    }
    
    public static void clientMessage(Object message) {
        if (mc.player != null) {
            mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message == null ? "null" : message.toString()));
        }
    }
    
    public static void sendMessage(String message) {
        mc.player.sendChatMessage(message);
    }
}
