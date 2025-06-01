package loftily.alt;

import loftily.Client;
import loftily.alt.microsoft.MicrosoftLoginThread;
import loftily.config.impl.AltConfig;
import loftily.utils.client.ClientUtils;
import lombok.Getter;
import net.minecraft.util.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class AltManager implements ClientUtils {
    private final List<Alt> alts = new ArrayList<>();
    private final MicrosoftLoginThread loginThread;
    private Alt currentAlt;
    
    public AltManager() {
        Session session = mc.getSession();
        boolean isOffline = session.getToken().equals("0") || session.getToken().equals(session.getPlayerID());
        this.currentAlt = new Alt(session.getUsername(), isOffline ? AltType.Offline : AltType.Microsoft);
        
        this.loginThread = new MicrosoftLoginThread();
    }
    
    public void login(Alt alt, Consumer<String> callback) {
        boolean microsoft = alt.getType() == AltType.Microsoft;
        
        if (microsoft) {
            Thread thread = new Thread(() -> {
                try {
                    loginThread.loginWithRefreshToken(alt.getRefreshToken());
                    callback.accept(loginThread.getCurrentText());
                    this.currentAlt = alt;
                } catch (Exception e) {
                    callback.accept("Login failed: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            thread.start();
            
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        } else {
            mc.setSession(new Session(alt.getName(), "", "", Session.Type.LEGACY.sessionType));
            this.currentAlt = alt;
            callback.accept("");
        }
    }
    
    public void login(Alt alt) {
        login(alt, s -> {
        });
    }
    
    
    public void add(Alt alt) {
        boolean exists;
        
        if (alt.getType() == AltType.Microsoft) {
            exists = alts.stream()
                    .filter(existingAlt -> existingAlt.getType() == AltType.Microsoft)
                    .anyMatch(existingAlt -> existingAlt.getUuid().equals(alt.getUuid()));
        } else {
            exists = alts.stream()
                    .filter(existingAlt -> existingAlt.getType() == AltType.Offline)
                    .anyMatch(existingAlt -> existingAlt.getName().equalsIgnoreCase(alt.getName()));
        }
        
        if (!exists) {
            alts.add(alt);
            Client.INSTANCE.getFileManager().get(AltConfig.class).write();
        }
    }
    
    
    public void remove(Alt alt) {
        alts.remove(alt);
        Client.INSTANCE.getFileManager().get(AltConfig.class).write();
    }
}
