package loftily.command.impl;

import loftily.Client;
import loftily.command.Command;
import loftily.utils.client.MessageUtils;
import net.minecraft.util.text.TextFormatting;

public class HelpCommand extends Command {
    public HelpCommand() {
        super(new int[]{-1}, "help");
    }
    
    @Override
    public void execCommand(String[] args) {
        MessageUtils.clientMessage("\n");
        for (Command command : Client.INSTANCE.getCommandManager().getAll()) {
            String className = command.getClass().getSimpleName();
            String usage = command.usage() != null ? command.usage() : "";
            String[] usageLines = usage.split("\n");
            
            String prefix = className + " - ";
            
            MessageUtils.clientMessage(prefix + TextFormatting.GRAY + usageLines[0]);
            
            String indent = new String(new char[prefix.length()]).replace('\0', ' ');
            for (int i = 1; i < usageLines.length; i++) {
                MessageUtils.clientMessage(indent + TextFormatting.GRAY + usageLines[i]);
            }
        }
    }
    
    @Override
    public String usage() {
        return ".help";
    }
}
