package loftily.command;

import loftily.utils.client.ClientUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class Command implements ClientUtils {
    protected final List<Integer> validLength;
    private final List<String> command;
    
    /**
     * @param validLength -1 = unlimited
     */
    public Command(int[] validLength, String... command) {
        this.validLength = new ArrayList<>();
        for (int length : validLength) {
            this.validLength.add(length);
        }
        this.command = Arrays.asList(command);
    }
    
    public abstract void execCommand(String[] args);
    
    public abstract String usage();
}
