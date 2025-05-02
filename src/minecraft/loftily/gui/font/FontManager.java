package loftily.gui.font;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class FontManager {
    public static FontWrapper NotoSans;
    private static Map<String, Map<Float, FontRenderer>> FontCache;
    
    public static void init() {
        FontCache = new HashMap<>();
        
        NotoSans = new FontWrapper("NotoSansSC-Regular.ttf");
    }
    
    public static class FontWrapper {
        private final String fontName;
        
        private FontWrapper(String fontName) {
            this.fontName = fontName;
        }
        
        public FontRenderer size(float fontSize) {
            fontSize = Math.max(0, Math.min(62, fontSize));
            
            return FontCache
                    .computeIfAbsent(fontName, k -> new HashMap<>())
                    .computeIfAbsent(fontSize, this::createFont);
        }
        
        private FontRenderer createFont(float size) {
            try (InputStream is = FontManager.class.getResourceAsStream(
                    "/assets/minecraft/loftily/fonts/" + fontName)) {
                
                Font font = Font.createFont(Font.PLAIN, is);
                font = font.deriveFont(size);
                return new FontRenderer(font);
                
            } catch (Exception e) {
                e.printStackTrace();
                return new FontRenderer(new Font("SansSerif", Font.PLAIN, (int) size));
            }
        }
    }
}
