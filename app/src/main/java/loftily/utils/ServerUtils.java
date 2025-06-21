package loftily.utils;

import loftily.gui.menu.mainmenu.MainMenu;
import loftily.utils.client.ClientUtils;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

public class ServerUtils implements ClientUtils {
    public static ServerData lastServerData;
    
    public static void connectToLastServer() {
        if (lastServerData != null) {
            mc.displayGuiScreen(new GuiConnecting(new GuiMultiplayer(new MainMenu()), mc, lastServerData));
        }
    }
    
    public static String getServerIp() {
        String serverIp = null;
        
        if (mc.isIntegratedServerRunning()) {
            serverIp = "SinglePlayer";
        } else if (mc.world != null && mc.world.isRemote) {
            ServerData serverData = mc.getCurrentServerData();
            if (serverData != null) {
                serverIp = serverData.serverIP;
            }
        }
        return serverIp;
    }
    
    public static String getServerPing() {
        if (mc.player == null || mc.getConnection().getPlayerInfo(mc.player.getUniqueID()) == null || mc.isIntegratedServerRunning())
            return "0 ms";
        
        if (mc.world != null && mc.world.isRemote) {
            return mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime() + " ms";
        }
        return "";
    }
}
