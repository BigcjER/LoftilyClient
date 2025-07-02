package loftily.command.impl;

import loftily.Client;
import loftily.command.Command;
import loftily.module.Module;
import loftily.utils.client.MessageUtils;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.util.Map;

public class BindCommand extends Command {
    public BindCommand() {
        super(new int[]{2, 3}, "bind", "b");
    }
    
    @Override
    public void execCommand(String[] args) {
        /* .bind list */
        if (args[1].equalsIgnoreCase("list")) {
            StringBuilder bindList = new StringBuilder("Bound Modules:\n");
            
            Map<Module, Integer> keybinds = Client.INSTANCE.getModuleManager().getAllKeyBinds();
            for (Map.Entry<Module, Integer> entry : keybinds.entrySet()) {
                Module module = entry.getKey();
                int key = entry.getValue();
                String keyName = Keyboard.getKeyName(key);
                
                if (keyName != null) {
                    bindList.append(module.getName())
                            .append(" -> ")
                            .append(keyName)
                            .append("\n");
                }
            }
            
            MessageUtils.clientMessageWithWaterMark(bindList.toString().trim());
            return;
        }
        
        /* .bind <Module> */
        Module module = Client.INSTANCE.getModuleManager().get(args[1].replace(" ", ""));
        
        if (module != null) {
            String keyName = args[2].toUpperCase();
            int key = Keyboard.getKeyIndex(keyName);
            
            if (key == Keyboard.KEY_NONE) {
                MessageUtils.clientMessageWithWaterMark(String.format("Module %s is unbound.", module.getName()));
                module.setKey(key);
                return;
            }
            
            module.setKey(key);
            MessageUtils.clientMessageWithWaterMark(String.format("Module %s is bound to %s.", module.getName(), keyName));
            return;
        }
        
        /* Module not found */
        MessageUtils.clientMessageWithWaterMark(String.format("%sModule '%s' %s",
                TextFormatting.RED,
                args[1],
                "not found!"));
    }
    
    @Override
    public String usage() {
        return ".bind <module> <key> or .bind list";
    }
    
    
}