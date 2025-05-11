package loftily.gui.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 加载Material Icons的Unicode映射
 * 图标:<a href="https://fonts.google.com/icons">...</a>
 */
public final class MaterialIcons {
    public static final Map<String, String> IconMap = loadIconMap();
    
    private MaterialIcons() {
    }
    
    private static Map<String, String> loadIconMap() {
        Map<String, String> map = new HashMap<>();
        try (InputStream is = MaterialIcons.class.getResourceAsStream("/minecraft/loftily/MaterialSymbolsSharp[FILL,GRAD,opsz,wght].codepoints");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //按照空格分割
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    String name = parts[0];
                    int code = Integer.parseInt(parts[1], 16);
                    map.put(name, new String(Character.toChars(code)));
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException(e);
        }
        return Collections.unmodifiableMap(map);
    }
    
    public static String get(String key) {
        String icon = IconMap.get(key);
        if (icon == null) throw new IllegalArgumentException(String.format("Invalid key '%s'!", key));
        return IconMap.get(key);
    }
}