package loftily.command.impl;


import loftily.Client;
import loftily.command.Command;
import loftily.module.Module;
import loftily.utils.client.MessageUtils;
import net.minecraft.util.text.TextFormatting;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super(new int[]{2}, "toggle", "t");
    }
    
    
    @Override
    public void execCommand(String[] args) {
        Module module = Client.INSTANCE.getModuleManager().get(args[1].replace(" ", ""));
        
        if (module != null) {
            module.toggle();
            MessageUtils.clientMessageWithWaterMark(
                    String.format("Module %s is toggled to %s", module.getName(), (module.isToggled() ? "Enabled." : "Disabled.")));
            return;
        }
        
        MessageUtils.clientMessageWithWaterMark(String.format("%sModule '%s' %s",
                TextFormatting.RED,
                args[1],
                "not found!"));
    }
    
    @Override
    public String usage() {
        return ".<t/toggle> <Module>.";
    }
}
