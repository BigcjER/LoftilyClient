package loftily.utils.client;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface ClientUtils {
    Minecraft mc = Minecraft.getMinecraft();
    Logger LOGGER = LogManager.getLogger(ClientUtils.class);
    
    
    default void info(Object o) {
        LOGGER.info(o);
    }
    
    default void println(Object o) {
        System.out.println(o);
    }
}
