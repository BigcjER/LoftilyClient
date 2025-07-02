package loftily.utils.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Pair<F, S> {
    private F first;
    private S second;
    
    //Override it for value command
    @Override
    public String toString() {
        if (first instanceof Double && second instanceof Double) {
            return Math.round((Double) first * 100) / 100.0 + " - " + Math.round((Double) second * 100) / 100.0;
        }
        return first + " " + second;
    }
}