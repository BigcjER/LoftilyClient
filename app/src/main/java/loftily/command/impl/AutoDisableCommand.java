package loftily.command.impl;

import loftily.Client;
import loftily.command.Command;
import loftily.config.impl.ModuleConfig;
import loftily.module.AutoDisableType;
import loftily.module.Module;
import loftily.utils.client.MessageUtils;
import net.minecraft.util.text.TextFormatting;

public class AutoDisableCommand extends Command {
    public AutoDisableCommand() {
        super(new int[]{2, 3}, "autodisable");
    }
    
    @Override
    public void execCommand(String[] args) {
        switch (args.length) {
            
            case 2: {
                Module module = Client.INSTANCE.getModuleManager().get(args[1].replace(" ", ""));
                
                if (module != null) {
                    MessageUtils.clientMessageWithWaterMark(String.format("Module %s's auto disable is set to %s", module.getName(), module.getAutoDisableType().name));
                    return;
                }
                
                /* Module not found */
                MessageUtils.clientMessageWithWaterMark(String.format("%sModule '%s' %s",
                        TextFormatting.RED,
                        args[1],
                        "not found!"));
                break;
            }
            
            case 3: {
                Module module = Client.INSTANCE.getModuleManager().get(args[1].replace(" ", ""));
                
                if (module != null) {
                    AutoDisableType autoDisableType = AutoDisableType.fromName(args[2]);
                    module.setAutoDisableType(autoDisableType);
                    Client.INSTANCE.getFileManager().get(ModuleConfig.class).write();
                    
                    MessageUtils.clientMessageWithWaterMark(String.format("Module %s's auto disable has been set to %s.", module.getName(), autoDisableType.name));
                    return;
                }
                
                /* Module not found */
                MessageUtils.clientMessageWithWaterMark(String.format("%sModule '%s' %s",
                        TextFormatting.RED,
                        args[1],
                        "not found!"));
                break;
            }
        }
    }
    
    @Override
    public String usage() {
        StringBuilder stringBuilder = new StringBuilder();
        AutoDisableType[] values = AutoDisableType.values();
        
        for (int i = 0; i < values.length; i++) {
            AutoDisableType autoDisableType = values[i];
            stringBuilder.append(autoDisableType.name);
            
            if (i < values.length - 1) {
                stringBuilder.append("/");
            }
        }
        
        return String.format(
                ".autodisable <Module> <%s>", stringBuilder) + "\n" +
                ".autodisable <Module>";
    }
}
