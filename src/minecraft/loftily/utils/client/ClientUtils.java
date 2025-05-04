package loftily.utils.client;

import loftily.Client;
import net.minecraft.client.Minecraft;

public interface ClientUtils {
    Minecraft mc = Minecraft.getMinecraft();
    
    default void info(Object o) {
        Client.Logger.info(o);
    }
    
    default void println(Object o) {
        System.out.println(o);
    }
}
