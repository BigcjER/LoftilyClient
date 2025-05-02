package loftily.utils.client;

import loftily.Client;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class MessageUtils implements MinecraftInstance {
    public static void clientMessageWithWaterMark(Object message) {
        clientMessage(String.format("%s%s%s %s",
                TextFormatting.YELLOW + "[",
                TextFormatting.DARK_AQUA + Client.Name,
                TextFormatting.YELLOW + "]",
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
