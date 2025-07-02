package loftily.command.impl;

import loftily.Client;
import loftily.command.Command;
import loftily.module.Module;
import loftily.utils.client.MessageUtils;
import loftily.value.Value;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class ModuleCommand extends Command {
    private final Module module;
    
    public ModuleCommand(Module module) {
        super(new int[]{3}, module.getName());
        this.module = module;
    }
    
    @Override
    public void execCommand(String[] args) {
        String valueName = args[1];
        String valueToSetText = args[2];
        //Find value
        Value<?, ?> value = null;
        for (Value<?, ?> valueInForEach : module.getValues()) {
            if (valueInForEach.getName().equalsIgnoreCase(valueName)) {
                value = valueInForEach;
            }
        }
        
        if (value == null) {
            MessageUtils.clientMessageWithWaterMark(String.format("%sValue %s not found in module '%s'!", TextFormatting.RED, valueName, module.getName()));
            return;
        }
        
        MessageUtils.clientMessageWithWaterMark(value.handleCommand(valueToSetText));
    }
    
    @Override
    public ITextComponent usage(String[] args) {
        if (args.length == 1) {
            int valuesSize = module.getValues().size();
            if (valuesSize == 0)
                return new TextComponentString(Client.STRING_PREFIX + " Module '" + module.getName() + "' has no values.");
            
            TextComponentString mainComponent = new TextComponentString(Client.STRING_PREFIX + " All values of " + module.getName() + ":\n");
            
            for (int i = 0; i < valuesSize; i++) {
                Value<?, ?> value = module.getValues().get(i);
                //Create value component
                TextComponentString valueComponent = new TextComponentString(value.getName());
                Style valueStyle = new Style();
                valueStyle.setColor(TextFormatting.GRAY);
                valueStyle.setBold(true);
                
                //Create hover text
                String hoverText = String.format(
                        "%sValue: %s%s\n" +
                                "%sType: %s%s\n" +
                                "%sClick to suggest command",
                        
                        TextFormatting.GRAY, TextFormatting.WHITE, value.getValue().toString(),
                        TextFormatting.GRAY, TextFormatting.WHITE, value.getClass().getSimpleName(),
                        TextFormatting.GOLD
                );
                valueStyle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(hoverText)));
                
                //Command to suggest
                String commandToSuggest = String.format(".%s %s ", module.getName().toLowerCase(), value.getName());
                valueStyle.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, commandToSuggest));
                valueComponent.setStyle(valueStyle);
                
                //Append string
                mainComponent.appendSibling(valueComponent);
                if (i != valuesSize - 1)
                    mainComponent.appendSibling(new TextComponentString("\n"));
            }
            
            return mainComponent;
        }
        
        
        return new TextComponentString(
                Client.STRING_PREFIX + TextFormatting.WHITE + " Usage: ." + module.getName().toLowerCase() + " <ValueName> <NewValue> or ." + module.getName().toLowerCase());
    }
}