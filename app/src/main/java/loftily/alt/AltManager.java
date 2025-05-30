package loftily.alt;

import loftily.Client;
import loftily.config.impl.AccountConfig;
import loftily.utils.client.ClientUtils;
import lombok.Getter;
import net.minecraft.util.Session;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AltManager implements ClientUtils {
    private final List<Alt> alts = new ArrayList<>();
    private Alt aurrentAlt;
    
    public AltManager() {
        Session session = mc.getSession();
        boolean isOffline = session.getToken().equals(session.getPlayerID());
        aurrentAlt = new Alt(session.getUsername(), isOffline ? AltType.Offline : AltType.Microsoft);
        
    }
    
    public void login(Alt alt) {
        if (alt.getType() == AltType.Microsoft) {
            //microsoftLogin.login(account.getRefreshToken());
        } else {
            mc.setSession(new Session(alt.getName(), "", "", Session.Type.LEGACY.sessionType));
        }
        this.aurrentAlt = alt;
    }
    
    public void add(Alt alt) {
        boolean exists = alt.getType() == AltType.Microsoft
                ? alts.stream().anyMatch(existingAlt -> existingAlt.getUuid().equals(alt.getUuid()))
                : alts.stream().anyMatch(existingAlt -> existingAlt.getName().equals(alt.getName()));
        
        if (!exists) {
            alts.add(alt);
            Client.INSTANCE.getConfigManager().get(AccountConfig.class).write();
        }
    }
    
    public void remove(Alt alt) {
        alts.remove(alt);
    }
}
