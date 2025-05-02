package loftily.command;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class Command {
    protected final List<Integer> validLength;
    private final List<String> name;
    
    public Command(int[] validLength, String... name) {
        this.validLength = new ArrayList<>();
        for (int length : validLength) {
            this.validLength.add(length);
        }
        this.name = Arrays.asList(name);
    }
    
    public abstract void execCommand(String[] args);
    
    public abstract String usage();
}
