package loftily.alt.microsoft;

import loftily.Client;
import loftily.alt.Alt;
import loftily.utils.client.ClientUtils;
import loftily.utils.other.CryptoUtils;
import loftily.utils.other.SystemUtils;
import lombok.Getter;
import net.minecraft.util.Session;
import net.minecraft.util.text.TextFormatting;

import javax.crypto.spec.SecretKeySpec;
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
            
            SystemUtils.copyToClipboard(authUrl);
            currentText = "Check out your clipboard...";
            
            try {
                String code = callbackServer.getCodeFuture().get(1, TimeUnit.MINUTES);
                setAndPrintCurrentText("Authorization code received");
                
                setAndPrintCurrentText("Getting access token and refresh token...");
                String[] tokens = getAccessTokenAndRefreshToken(code, codeVerifier);
                String accessToken = tokens[0];
                String refreshToken = tokens[1];
                
                doLogin(accessToken, refreshToken, true);
            } catch (InterruptedException e) {
                LOGGER.warn("Microsoft login thread interrupted.");
                Thread.currentThread().interrupt();
            } catch (TimeoutException te) {
                LOGGER.error("Timed out waiting for authorization code", te);
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
                    LOGGER.error(e);
                }
            }
        }
    }
    
    public void loginWithRefreshToken(String encryptedRefreshToken) throws Exception {
        setAndPrintCurrentText("Decrypting refresh token...");
        byte[] cpuid = SystemUtils.getCpuIdSha256();
        SecretKeySpec key = new SecretKeySpec(cpuid, 0, 16, "AES");
        String refreshToken = CryptoUtils.aesDecrypt(encryptedRefreshToken, key);
        
        setAndPrintCurrentText("Refreshing access token...");
        String newAccessToken = refreshAccessToken(refreshToken);
        
        doLogin(newAccessToken, refreshToken, false);
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
    
    public void setAndPrintCurrentText(String currentText) {
        this.currentText = currentText;
        LOGGER.info(currentText);
    }
    
    private void doLogin(String accessToken, String refreshToken, boolean add) throws Exception {
        String xblToken = getXBLToken(accessToken);
        
        setAndPrintCurrentText("Getting XSTS token and user hash...");
        String[] XSTSTokenAndUHS = getXSTSTokenAndUHS(xblToken);
        String XSTSToken = XSTSTokenAndUHS[0];
        String uhs = XSTSTokenAndUHS[1];
        
        setAndPrintCurrentText("Getting Minecraft access token...");
        String minecraftAccessToken = getMinecraftAccessToken(uhs, XSTSToken);
        
        setAndPrintCurrentText("Checking Minecraft ownership...");
        if (!checkMinecraftOwnership(minecraftAccessToken)) {
            currentText = TextFormatting.YELLOW + "This Microsoft account does not own Minecraft.";
            return;
        }
        
        setAndPrintCurrentText("Retrieving Minecraft profile...");
        String[] profile = getMinecraftProfile(minecraftAccessToken);
        String name = profile[0];
        String uuid = profile[1];
        
        mc.addScheduledTask(() -> mc.setSession(new Session(name, uuid, minecraftAccessToken, "mojang")));
        
        if (add) {
            byte[] cpuid = SystemUtils.getCpuIdSha256();
            SecretKeySpec key = new SecretKeySpec(cpuid, 0, 16, "AES");
            String encryptedRefreshToken = CryptoUtils.aesEncrypt(refreshToken, key);
            Alt alt = new Alt(name, uuid, encryptedRefreshToken);
            Client.INSTANCE.getAltManager().add(alt);
        }
        
        setAndPrintCurrentText("Login successful! Logged in as " + name);
    }
}
