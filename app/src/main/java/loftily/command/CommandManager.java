package loftily.command;

import loftily.Client;
import loftily.command.impl.ModuleCommand;
import loftily.core.AbstractManager;
import loftily.event.impl.client.ChatEvent;
import loftily.module.Module;
import loftily.utils.client.MessageUtils;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.Map;

public class CommandManager extends AbstractManager<Command> {
    public final String PreFix = ".";
    private final Map<String, Command> nameToCommandMap = new HashMap<>();
    
    public CommandManager() {
        super("impl", Command.class);
        
        for (Command command : getAll()) {
            for (String alias : command.getCommand()) {
                nameToCommandMap.put(alias.toLowerCase(), command);
            }
        }
        
        for (Module module : Client.INSTANCE.getModuleManager().getAll()) {
            nameToCommandMap.put(module.getName().toLowerCase(), new ModuleCommand(module));
        }
        
        Client.INSTANCE.getEventManager().register(this);
    }
    
    @EventHandler
    public void onChatEvent(ChatEvent event) {
        String message = event.getMessage();
        
        if (!message.startsWith(PreFix)) return;
        else event.setCancelled(true);
        
        
        String[] parts = message.split(" ");
        
        String commandName = parts[0].toLowerCase().replaceFirst(".", "");
        
        String[] args = parts.length > 1 ? message.substring(commandName.length()).trim().split(" ") : new String[0];
        
        Command command = get(commandName);
        
        if (command != null) {
            if (command.validLength.contains(args.length) || command.validLength.contains(-1)) {
                command.execCommand(args);
                return;
            }
            mc.ingameGUI.getChatGUI().printChatMessage(command.usage(parts));
            return;
        }
        MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + "Unknown command! Please enter .help to view help.");
    }
    
    public Command get(String commandName) {
        return nameToCommandMap.get(commandName.toLowerCase());
    }
}
