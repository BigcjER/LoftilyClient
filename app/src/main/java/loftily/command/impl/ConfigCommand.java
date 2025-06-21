package loftily.command.impl;

import loftily.Client;
import loftily.command.Command;
import loftily.config.FileManager;
import loftily.config.impl.json.ModuleJsonConfig;
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
        ModuleJsonConfig moduleJsonConfig = Client.INSTANCE.getFileManager().get(ModuleJsonConfig.class);
        /* .config <folder/list>  */
        if (args.length == 2) {
            String command = args[1].toLowerCase();
            switch (command) {
                case "folder":
                    try {
                        Desktop.getDesktop().open(FileManager.CONFIG_DIR);
                        MessageUtils.clientMessageWithWaterMark("Configuration folder opened.");
                    } catch (IOException e) {
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + "Failed to open configuration folder!");
                        throw new RuntimeException(e);
                    }
                    break;
                
                case "list":
                    List<File> configFiles = Arrays.asList(
                            Objects.requireNonNull(FileManager.CONFIG_DIR.listFiles((dir, name) -> name.endsWith(".json"))));
                    
                    if (configFiles.isEmpty()) {
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + "No configurations found.");
                        break;
                    }
                    
                    MessageUtils.clientMessageWithWaterMark("Available configurations:");
                    String currentConfig = moduleJsonConfig.getFile().getName().replace(".json", "");
                    
                    for (File configFile : configFiles) {
                        String configFileName = configFile.getName().replace(".json", "");
                        
                        //点击加载
                        TextComponentString text = new TextComponentString(configFileName);
                        Style style = text.getStyle();
                        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Load config " + TextFormatting.GOLD + configFileName)));
                        style.setItalic(true);
                        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".config load " + configFileName));
                        if (configFileName.equals(currentConfig)) style.setColor(TextFormatting.GREEN);
                        
                        TextComponentString textWithWaterMark = new TextComponentString(TextFormatting.DARK_AQUA + Client.NAME + " » §f");
                        textWithWaterMark.appendSibling(text);
                        mc.ingameGUI.getChatGUI().printChatMessage(textWithWaterMark);
                    }
                    break;
                
                case "reload":
                    if (moduleJsonConfig.getFile().exists()) moduleJsonConfig.read();
                    else moduleJsonConfig.write();
                    MessageUtils.clientMessageWithWaterMark("Configuration reloaded.");
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
            File configFile = new File(FileManager.CONFIG_DIR, configName + ".json");
            
            switch (command) {
                case "load":
                    if (configFile.exists()) {
                        moduleJsonConfig.load(configFile);
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.GREEN + String.format("Configuration %s loaded.", configName));
                        break;
                    }
                    MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + String.format("Configuration %s not found!", configName));
                    break;
                
                case "save":
                    File originFile = moduleJsonConfig.getFile();
                    moduleJsonConfig.write();
                    moduleJsonConfig.setFile(new File(FileManager.CONFIG_DIR, configName + ".json"));
                    moduleJsonConfig.write();
                    moduleJsonConfig.setFile(originFile);
                    MessageUtils.clientMessageWithWaterMark(TextFormatting.GREEN + String.format("Configuration %s saved.", configName));
                    break;
                
                case "create":
                    if (configFile.exists()) {
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.YELLOW + String.format("Configuration %s already exists.", configName));
                        break;
                    }
                    moduleJsonConfig.write();
                    
                    Client.INSTANCE.getModuleManager().getAll().forEach(module -> module.getValues().forEach(value -> value.setValue(value.getDefaultValue())));
                    Client.INSTANCE.getModuleManager().getAll().forEach(module -> {
                        if (module.isToggled() != module.isDefaultToggled()) {
                            module.setToggled(module.isDefaultToggled(), false, false);
                        }
                    });
                    
                    moduleJsonConfig.setFile(new File(FileManager.CONFIG_DIR, configName + ".json"));
                    moduleJsonConfig.write();
                    ClientSettings.lastModuleConfig.set(moduleJsonConfig.getFile().getName());
                    
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
                        ".config <folder/list/reload>";
    }
}