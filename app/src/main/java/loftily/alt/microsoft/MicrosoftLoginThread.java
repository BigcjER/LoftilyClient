package loftily.alt.microsoft;

import loftily.Client;
import loftily.alt.Alt;
import loftily.utils.client.ClientUtils;
import lombok.Getter;
import net.minecraft.util.Session;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static loftily.alt.microsoft.MicrosoftLoginUtils.*;

public class MicrosoftLoginThread extends Thread implements ClientUtils {
    @Getter
    private String currentText = "";
    private MicrosoftLoginCallbackServer callbackServer;
    
    public MicrosoftLoginThread() {
        super("Microsoft login thread");
    }
    
    @Override
    public void run() {
        try {
            callbackServer = new MicrosoftLoginCallbackServer(PORT, CALLBACK_PATH);
            callbackServer.start();
            
            String codeVerifier = generateCodeVerifier();
            String codeChallenge = generateCodeChallenge(codeVerifier);
            
            String authUrl = "https://login.live.com/oauth20_authorize.srf" +
                    "?client_id=" + CLIENT_ID +
                    "&response_type=code" +
                    "&redirect_uri=" + REDIRECT_URI +
                    "&scope=XboxLive.signin%20offline_access" +
                    "&code_challenge=" + codeChallenge +
                    "&code_challenge_method=S256";
            
            setCurrentText(copyToClipboard(authUrl));
            
            try {
                String code = callbackServer.getCodeFuture().get(1, TimeUnit.MINUTES);
                setCurrentText("Authorization code received");
                
                setCurrentText("Getting access token and refresh token...");
                String[] tokens = getAccessTokenAndRefreshToken(code, codeVerifier);
                String accessToken = tokens[0];
                String refreshToken = tokens[1];
                
                setCurrentText("Getting xbox live token...");
                String xblToken = getXBLToken(accessToken);
                
                setCurrentText("Getting XSTS token and user hash...");
                String[] XSTSTokenAndUHS = getXSTSTokenAndUHS(xblToken);
                String XSTSToken = XSTSTokenAndUHS[0];
                String uhs = XSTSTokenAndUHS[1];
                
                setCurrentText("Getting Minecraft access token...");
                String minecraftAccessToken = getMinecraftAccessToken(uhs, XSTSToken);
                
                setCurrentText("Checking Minecraft ownership...");
                if (!checkMinecraftOwnership(minecraftAccessToken)) {
                    currentText = TextFormatting.YELLOW + "This Microsoft account does not own Minecraft. Please check the account";
                    return;
                }
                
                setCurrentText("Retrieving Minecraft profile...");
                String[] profile = getMinecraftProfile(minecraftAccessToken);
                String name = profile[0];
                String uuid = profile[1];
                
                mc.addScheduledTask(() -> mc.setSession(new Session(name, uuid, minecraftAccessToken, "mojang")));
                Client.INSTANCE.getAltManager().add(new Alt(name, uuid, refreshToken));
                currentText = TextFormatting.GREEN + "Login successful!Logged in to " + name;
            } catch (InterruptedException e) {
                Logger.warn("Microsoft login thread interrupted.");
                Thread.currentThread().interrupt();
            } catch (TimeoutException te) {
                Logger.error("Timed out waiting for authorization code", te);
                currentText = TextFormatting.RED + "Login timed out. Please try again.";
            } catch (Exception e) {
                currentText = TextFormatting.RED + "Error: " + e.getMessage();
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (callbackServer != null) {
                try {
                    callbackServer.close();
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
        }
    }
    
    @Override
    public void interrupt() {
        super.interrupt();
        if (callbackServer != null) {
            try {
                callbackServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void setCurrentText(String currentText) {
        this.currentText = currentText;
        Logger.info(currentText);
    }
}
