import loftily.Client;
import net.minecraft.client.main.Main;

import java.util.Arrays;

public class Start {
    public static void main(String[] args) {
        Main.main(concat(new String[]{"--version", Client.NAME, "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.12", "--userProperties", "{}"}, args));
    }
    
    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
