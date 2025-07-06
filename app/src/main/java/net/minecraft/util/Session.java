package net.minecraft.util;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class Session
{
    private final String username;
    private final String playerID;
    private final String token;
    @Setter
    private long sessionStartTime;
    
    public Session(String usernameIn, String playerIDIn, String tokenIn)
    {
        this.username = usernameIn;
        this.playerID = playerIDIn;
        this.token = tokenIn;
        this.sessionStartTime = System.currentTimeMillis();
    }

    public String getSessionID()
    {
        return "token:" + this.token + ":" + this.playerID;
    }

    public GameProfile getProfile()
    {
        try
        {
            UUID uuid = UUIDTypeAdapter.fromString(this.getPlayerID());
            return new GameProfile(uuid, this.getUsername());
        }
        catch (IllegalArgumentException var2)
        {
            return new GameProfile(null, this.getUsername());
        }
    }
}
