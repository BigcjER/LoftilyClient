package loftily.config.impl;

import loftily.Client;
import loftily.config.Config;
import loftily.config.ConfigManager;
import loftily.gui.theme.Theme;
import loftily.settings.ClientSettings;
import loftily.utils.client.ClientUtils;
import loftily.utils.client.FileUtils;
import loftily.utils.client.MessageUtils;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 主题配置文件管理类，负责主题配置的读写操作
 */
public class ThemeConfig extends Config {
    private static final Pattern CSS_VAR_PATTERN = Pattern.compile("--md-sys-color-([^:]+):\\s*([^;]+);");
    private static final String DEFAULT_THEME_PATH = "assets/minecraft/loftily/DefaultTheme.css";
    
    public ThemeConfig() {
        super(new File(ConfigManager.themeDir, ClientSettings.lastThemeConfig.get()));
    }
    
    /**
     * 读取并解析CSS主题文件
     */
    @Override
    public void read() {
        try {
            if (!configFile.exists()) return;
            
            // 读取CSS文件内容
            String cssContent = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
            
            // 解析亮色/暗色主题变量
            Map<String, String> lightVars = parseCssVars(cssContent, ":root, .light-theme");
            Map<String, String> darkVars = parseCssVars(cssContent, ".dark-theme");
            
            // 构建Theme对象并设置到客户端
            Client.INSTANCE.setTheme(new Theme(
                    /* 亮色主题参数 */
                    parseColor(lightVars.get("primary"), "primary"),
                    parseColor(lightVars.get("on-primary"), "on-primary"),
                    parseColor(lightVars.get("primary-container"), "primary-container"),
                    parseColor(lightVars.get("on-primary-container"), "on-primary-container"),
                    parseColor(lightVars.get("secondary"), "secondary"),
                    parseColor(lightVars.get("on-secondary"), "on-secondary"),
                    parseColor(lightVars.get("secondary-container"), "secondary-container"),
                    parseColor(lightVars.get("on-secondary-container"), "on-secondary-container"),
                    parseColor(lightVars.get("tertiary"), "tertiary"),
                    parseColor(lightVars.get("on-tertiary"), "on-tertiary"),
                    parseColor(lightVars.get("tertiary-container"), "tertiary-container"),
                    parseColor(lightVars.get("on-tertiary-container"), "on-tertiary-container"),
                    parseColor(lightVars.get("error"), "error"),
                    parseColor(lightVars.get("on-error"), "on-error"),
                    parseColor(lightVars.get("error-container"), "error-container"),
                    parseColor(lightVars.get("on-error-container"), "on-error-container"),
                    parseColor(lightVars.get("background"), "background"),
                    parseColor(lightVars.get("on-background"), "on-background"),
                    parseColor(lightVars.get("surface"), "surface"),
                    parseColor(lightVars.get("on-surface"), "on-surface"),
                    parseColor(lightVars.get("surface-variant"), "surface-variant"),
                    parseColor(lightVars.get("on-surface-variant"), "on-surface-variant"),
                    parseColor(lightVars.get("outline"), "outline"),
                    parseColor(lightVars.get("outline-variant"), "outline-variant"),
                    parseColor(lightVars.get("scrim"), "scrim"),
                    parseColor(lightVars.get("inverse-surface"), "inverse-surface"),
                    parseColor(lightVars.get("inverse-on-surface"), "inverse-on-surface"),
                    parseColor(lightVars.get("inverse-primary"), "inverse-primary"),
                    
                    /* 暗色主题参数 */
                    parseColor(darkVars.get("primary"), "dark-primary"),
                    parseColor(darkVars.get("on-primary"), "dark-on-primary"),
                    parseColor(darkVars.get("primary-container"), "dark-primary-container"),
                    parseColor(darkVars.get("on-primary-container"), "dark-on-primary-container"),
                    parseColor(darkVars.get("secondary"), "dark-secondary"),
                    parseColor(darkVars.get("on-secondary"), "dark-on-secondary"),
                    parseColor(darkVars.get("secondary-container"), "dark-secondary-container"),
                    parseColor(darkVars.get("on-secondary-container"), "dark-on-secondary-container"),
                    parseColor(darkVars.get("tertiary"), "dark-tertiary"),
                    parseColor(darkVars.get("on-tertiary"), "dark-on-tertiary"),
                    parseColor(darkVars.get("tertiary-container"), "dark-tertiary-container"),
                    parseColor(darkVars.get("on-tertiary-container"), "dark-on-tertiary-container"),
                    parseColor(darkVars.get("error"), "dark-error"),
                    parseColor(darkVars.get("on-error"), "dark-on-error"),
                    parseColor(darkVars.get("error-container"), "dark-error-container"),
                    parseColor(darkVars.get("on-error-container"), "dark-on-error-container"),
                    parseColor(darkVars.get("background"), "dark-background"),
                    parseColor(darkVars.get("on-background"), "dark-on-background"),
                    parseColor(darkVars.get("surface"), "dark-surface"),
                    parseColor(darkVars.get("on-surface"), "dark-on-surface"),
                    parseColor(darkVars.get("surface-variant"), "dark-surface-variant"),
                    parseColor(darkVars.get("on-surface-variant"), "dark-on-surface-variant"),
                    parseColor(darkVars.get("outline"), "dark-outline"),
                    parseColor(darkVars.get("outline-variant"), "dark-outline-variant"),
                    parseColor(darkVars.get("scrim"), "dark-scrim"),
                    parseColor(darkVars.get("inverse-surface"), "dark-inverse-surface"),
                    parseColor(darkVars.get("inverse-on-surface"), "dark-inverse-on-surface"),
                    parseColor(darkVars.get("inverse-primary"), "dark-inverse-primary")
            ));
        } catch (IOException e) {
            throw new RuntimeException("主题配置文件读取失败", e);
        }
    }
    
    @Override
    public void write() {
        //如果没有主题就解压默认
        try {
            if (Client.INSTANCE.getTheme() == null) {
                if (!configFile.exists()) {
                    FileUtils.unpackFile(configFile, DEFAULT_THEME_PATH);
                }
                read();
                return;
            } else {
                List<String> nullFields = new ArrayList<>();
                
                for (Field field : this.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    
                    if (field.getType().equals(Color.class)) {
                        try {
                            Color color = (Color) field.get(this);
                            if (color == null || color.equals(new Color(0, 0, 0, 0))) {
                                nullFields.add(field.getName());
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                if (!nullFields.isEmpty()) {
                    nullFields.forEach(s -> MessageUtils.sendMessage(TextFormatting.RED + s + " is an invalid field and has stopped writing to the theme."));
                    return;
                }
            }
            
            // 生成CSS内容并写入文件
            String cssContent = convertThemeToCss(Client.INSTANCE.getTheme());
            Files.write(configFile.toPath(), cssContent.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 将Theme对象转换为CSS字符串
     */
    private String convertThemeToCss(Theme theme) {
        StringBuilder sb = new StringBuilder();
        
        // 亮色主题区块
        sb.append(":root, .light-theme {\n");
        appendThemeBlock(sb, theme, false);
        sb.append("}\n\n");
        
        // 暗色主题区块
        sb.append(".dark-theme {\n");
        appendThemeBlock(sb, theme, true);
        sb.append("}\n");
        
        return sb.toString();
    }
    
    /**
     * 构建主题区块内容
     *
     * @param isDark 是否为暗色主题
     */
    private void appendThemeBlock(StringBuilder sb, Theme theme, boolean isDark) {
        // 颜色添加工具方法
        BiConsumer<String, Color> colorAdder = (varName, color) -> {
            // 生成RGB和HEX格式
            String rgb = String.format("%d, %d, %d",
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue()
            );
            String hex = String.format("#%02x%02x%02x",
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue()
            );
            
            sb.append("  --md-sys-color-").append(varName).append("-rgb: ").append(rgb).append(";\n");
            sb.append("  --md-sys-color-").append(varName).append(": ").append(hex).append(";\n");
        };
        
        // 基础颜色组
        addColorGroup(theme, isDark, colorAdder, new String[]{
                "primary", "on-primary", "primary-container", "on-primary-container",
                "secondary", "on-secondary", "secondary-container", "on-secondary-container",
                "tertiary", "on-tertiary", "tertiary-container", "on-tertiary-container",
                "error", "on-error", "error-container", "on-error-container"
        });
        
        // 表面颜色组
        addColorGroup(theme, isDark, colorAdder, new String[]{
                "background", "on-background",
                "surface", "on-surface",
                "surface-variant", "on-surface-variant"
        });
        
        // 边框和遮罩
        addColorGroup(theme, isDark, colorAdder, new String[]{
                "outline", "outline-variant",
                "scrim"
        });
        
        // 反色组
        addColorGroup(theme, isDark, colorAdder, new String[]{
                "inverse-surface", "inverse-on-surface", "inverse-primary"
        });
    }
    
    /**
     * 添加颜色组到CSS区块
     */
    private void addColorGroup(Theme theme, boolean isDark,
                               BiConsumer<String, Color> colorAdder, String[] keys) {
        for (String key : keys) {
            try {
                // 通过反射获取对应方法
                String methodName = buildGetterName(key, isDark);
                Method method = Theme.class.getMethod(methodName);
                Color color = (Color) method.invoke(theme);
                
                colorAdder.accept(key, color);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * 构建Getter方法名称
     */
    private String buildGetterName(String cssVarName, boolean isDark) {
        // 转换CSS变量名到Java方法名（例如：on-primary -> getOnPrimary）
        String[] parts = cssVarName.split("-");
        StringBuilder name = new StringBuilder("get");
        if (isDark) name.append("Dark");
        
        for (String part : parts) {
            name.append(part.substring(0, 1).toUpperCase())
                    .append(part.substring(1));
        }
        return name.toString();
    }
    
    /**
     * 解析CSS变量
     */
    private Map<String, String> parseCssVars(String cssContent, String selector) {
        Map<String, String> vars = new HashMap<>();
        String pattern = selector + "\\s*\\{([^}]*)\\}";
        Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(cssContent);
        
        if (matcher.find()) {
            String block = matcher.group(1);
            Matcher varMatcher = CSS_VAR_PATTERN.matcher(block);
            while (varMatcher.find()) {
                // 移除-rgb后缀，统一变量名
                String varName = varMatcher.group(1).replace("-rgb", "");
                String value = varMatcher.group(2).trim();
                vars.put(varName, value);
            }
        }
        return vars;
    }
    
    /**
     * 解析颜色值（支持#RGB和rgb()格式）
     */
    private Color parseColor(String value, String keyName) {
        if (value == null) {
            System.out.print(1);
            String message = String.format(TextFormatting.RED + "Missing color value for key: %s. Please check your theme file!", keyName);
            if (ClientUtils.mc.player == null) Client.Logger.error(message);
            else MessageUtils.clientMessage(message);
            return new Color(0, 0, 0, 0); // 透明色作为默认值
        }
        
        try {
            if (value.startsWith("#")) {
                return Color.decode(value);
            } else if (value.startsWith("rgb(")) {
                String[] parts = value.substring(4, value.length() - 1).split(",");
                return new Color(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim())
                );
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
        return new Color(0, 0, 0, 0);
    }
    
    public void load(File themeFile) {
        write();
        this.configFile = themeFile;
        read();
        
        ClientSettings.lastThemeConfig.set(configFile.getName());
        Client.INSTANCE.getConfigManager().get(ClientSettingsConfig.class).write();
    }
}