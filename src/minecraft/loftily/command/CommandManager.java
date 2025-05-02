package loftily.command;

import loftily.Client;
import loftily.core.AbstractManager;
import loftily.event.impl.client.ChatEvent;
import loftily.utils.client.MessageUtils;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.util.text.TextFormatting;

public class CommandManager extends AbstractManager<Command> {
    public final String PreFix = ".";
    
    public CommandManager() {
        super("impl", Command.class);
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
            MessageUtils.clientMessageWithWaterMark("Usage: " + command.usage());
            return;
        }
        MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + "Unknown command! Please enter .help to view help.");
    }
    
    public Command get(String commandName) {
        return this.stream()
                .filter(command -> command.getCommand().stream()
                        .anyMatch(name -> name.equalsIgnoreCase(commandName)))
                .findFirst()
                .orElse(null);
    }
}
