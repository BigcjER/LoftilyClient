package loftily.handlers;

import loftily.utils.client.ClientUtils;
import lombok.Setter;

@Setter
public abstract class Handler implements ClientUtils {
    protected boolean needRegister() {
        return true;
    }
}
