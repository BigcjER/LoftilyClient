package loftily.command.impl;

import loftily.Client;
import loftily.command.Command;
import loftily.config.ConfigManager;
import loftily.config.impl.ThemeConfig;
import loftily.utils.client.MessageUtils;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ThemeCommand extends Command {
    
    public ThemeCommand() {
        super(new int[]{2, 3}, "theme");
    }
    
    @Override
    public void execCommand(String[] args) {
        ThemeConfig themeConfig = Client.INSTANCE.getConfigManager().get(ThemeConfig.class);
        /* .config <folder/list>  */
        if (args.length == 2) {
            String command = args[1].toLowerCase();
            switch (command) {
                case "folder":
                    try {
                        Desktop.getDesktop().open(ConfigManager.themeDir);
                        MessageUtils.clientMessageWithWaterMark("Theme folder opened.");
                    } catch (IOException e) {
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + "Failed to open theme folder!");
                        throw new RuntimeException(e);
                    }
                    break;
                
                case "list":
                    List<File> configFiles = Arrays.asList(
                            Objects.requireNonNull(ConfigManager.themeDir.listFiles((dir, name) -> name.endsWith(".css"))));
                    
                    if (configFiles.isEmpty()) {
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + "No theme found.");
                        break;
                    }
                    
                    MessageUtils.clientMessageWithWaterMark("Available themes:");
                    String currentConfig = themeConfig.getConfigFile().getName().replace(".css", "");
                    
                    for (File configFile : configFiles) {
                        String themeFileName = configFile.getName().replace(".css", "");
                        
                        //点击加载
                        TextComponentString text = new TextComponentString(themeFileName);
                        Style style = text.getStyle();
                        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Load theme " + TextFormatting.GOLD + themeFileName)));
                        style.setItalic(true);
                        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".theme load " + themeFileName));
                        if (themeFileName.equals(currentConfig)) style.setColor(TextFormatting.GREEN);
                        
                        TextComponentString textWithWaterMark = new TextComponentString(TextFormatting.DARK_AQUA + Client.Name + " » §f");
                        textWithWaterMark.appendSibling(text);
                        mc.ingameGUI.getChatGUI().printChatMessage(textWithWaterMark);
                    }
                    break;
                
                default:
                    MessageUtils.clientMessageWithWaterMark("Usage: \n" + usage());
                    break;
            }
            return;
        }
        
        /* .config <load/save/create> <ConfigName> */
        if (args.length == 3) {
            String command = args[1].toLowerCase();
            String themeName = args[2];
            File configFile = new File(ConfigManager.themeDir, themeName + ".css");
            
            switch (command) {
                case "load":
                    if (configFile.exists()) {
                        themeConfig.load(configFile);
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.GREEN + String.format("Theme %s loaded.", themeName));
                        break;
                    }
                    MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + String.format("Theme %s not found!", themeName));
                    break;
                
                case "save":
                    File originFile = themeConfig.getConfigFile();
                    themeConfig.write();
                    themeConfig.setConfigFile(new File(ConfigManager.themeDir, themeName + ".css"));
                    themeConfig.write();
                    themeConfig.setConfigFile(originFile);
                    MessageUtils.clientMessageWithWaterMark(TextFormatting.GREEN + String.format("Theme %s saved.", themeName));
                    break;
                
                case "create":
                    if (configFile.exists()) {
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.YELLOW + String.format("Theme %s already exists.", themeName));
                        break;
                    }
                    
                    File newFile = new File(ConfigManager.themeDir, themeName + ".css");
                    
                    try {
                        newFile.createNewFile();
                        Desktop.getDesktop().open(newFile);
                        Desktop.getDesktop().browse(new URI("https://m3-theme.zeir.cc/custom/code"));
                        MessageUtils.clientMessageWithWaterMark("Create your theme and copy the content inside the Theme code box into the file, then load it!");
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    
                    break;
                
                default:
                    MessageUtils.clientMessageWithWaterMark("Usage: \n" + usage());
                    break;
            }
        }
    }
    
    @Override
    public String usage() {
        return
                ".theme <load/save/create> <ConfigName> \n" +
                        ".theme <folder/list>";
    }
}