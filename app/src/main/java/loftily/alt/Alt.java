package loftily.alt;

import lombok.Getter;

@Getter
public class Alt {
    private final String name, uuid, refreshToken;
    private final AltType type;
    
    public Alt(String name, String uuid, String refreshToken) {
        this.name = name;
        this.uuid = uuid;
        this.refreshToken = refreshToken;
        this.type = AltType.Microsoft;
    }
    
    public Alt(String name, AltType type) {
        this.name = name;
        this.type = type;
        this.uuid = null;
        this.refreshToken = null;
    }
    
    public Alt(String name) {
        this(name, AltType.Offline);
    }
    
    public String getUuid() {
        if (type == AltType.Microsoft)
            throw new UnsupportedOperationException("Offline alts don't have a UUID.");
        return uuid;
    }
    
    public String getRefreshToken() {
        if (type == AltType.Offline) {
            throw new UnsupportedOperationException("Offline alts don't have a refresh token.");
        }
        return refreshToken;
    }
}
