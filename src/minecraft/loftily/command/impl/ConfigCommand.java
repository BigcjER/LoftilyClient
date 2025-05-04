package loftily.command.impl;

import loftily.Client;
import loftily.command.Command;
import loftily.config.ConfigManager;
import loftily.config.impl.ModuleConfig;
import loftily.settings.ClientSettings;
import loftily.utils.client.MessageUtils;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConfigCommand extends Command {
    
    public ConfigCommand() {
        super(new int[]{2, 3}, "config");
    }
    
    @Override
    public void execCommand(String[] args) {
        ModuleConfig moduleConfig = Client.INSTANCE.getConfigManager().get(ModuleConfig.class);
        /* .config <folder/list>  */
        if (args.length == 2) {
            String command = args[1].toLowerCase();
            switch (command) {
                case "folder":
                    try {
                        Desktop.getDesktop().open(ConfigManager.configDir);
                        MessageUtils.clientMessageWithWaterMark("Configuration folder opened.");
                    } catch (IOException e) {
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + "Failed to open configuration folder!");
                        throw new RuntimeException(e);
                    }
                    break;
                
                case "list":
                    List<File> configFiles = Arrays.asList(
                            Objects.requireNonNull(ConfigManager.configDir.listFiles((dir, name) -> name.endsWith(".json"))));
                    
                    if (configFiles.isEmpty()) {
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + "No configurations found.");
                        break;
                    }
                    
                    MessageUtils.clientMessageWithWaterMark("Available configurations:");
                    String currentConfig = moduleConfig.getConfigFile().getName().replace(".json", "");
                    
                    for (File configFile : configFiles) {
                        String configFileName = configFile.getName().replace(".json", "");
                        
                        //点击加载
                        TextComponentString text = new TextComponentString(configFileName);
                        Style style = text.getStyle();
                        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Load config " + TextFormatting.GOLD + configFileName)));
                        style.setItalic(true);
                        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".config load " + configFileName));
                        if (configFileName.equals(currentConfig)) style.setColor(TextFormatting.GREEN);
                        
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
            String configName = args[2];
            File configFile = new File(ConfigManager.configDir, configName + ".json");
            
            switch (command) {
                case "load":
                    if (configFile.exists()) {
                        moduleConfig.load(configFile);
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.GREEN + String.format("Configuration %s loaded.", configName));
                        break;
                    }
                    MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + String.format("Configuration %s not found!", configName));
                    break;
                
                case "save":
                    File originFile = moduleConfig.getConfigFile();
                    moduleConfig.write();
                    moduleConfig.setConfigFile(new File(ConfigManager.configDir, configName + ".json"));
                    moduleConfig.write();
                    moduleConfig.setConfigFile(originFile);
                    MessageUtils.clientMessageWithWaterMark(TextFormatting.GREEN + String.format("Configuration %s saved.", configName));
                    break;
                
                case "create":
                    if (configFile.exists()) {
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.YELLOW + String.format("Configuration %s already exists.", configName));
                        break;
                    }
                    moduleConfig.write();
                    
                    Client.INSTANCE.getModuleManager().forEach(module -> module.getValues().forEach(value -> value.setValue(value.getDefaultValue())));
                    Client.INSTANCE.getModuleManager().forEach(module -> {
                        if (!module.isDefaultToggled() || !module.isToggled()) module.setToggled(false, false);
                    });
                    
                    moduleConfig.setConfigFile(new File(ConfigManager.configDir, configName + ".json"));
                    moduleConfig.write();
                    ClientSettings.lastModuleConfig.set(moduleConfig.getConfigFile().getName());
                    
                    MessageUtils.clientMessageWithWaterMark(TextFormatting.GREEN + String.format("Configuration %s created.", configName));
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
                ".config <load/save/create> <ConfigName> \n" +
                        ".config <folder/list>";
    }
}