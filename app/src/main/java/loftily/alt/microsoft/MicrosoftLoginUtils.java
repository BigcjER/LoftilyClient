package loftily.alt.microsoft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import loftily.utils.other.CryptoUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class MicrosoftLoginUtils {
    public static final int PORT = 10721;
    public static final String CLIENT_ID = "e5ea49ae-6e77-4fe5-903a-ac377789f020";
    public static final String CALLBACK_PATH = "/callback";
    public static final String REDIRECT_URI = "http://localhost:" + PORT + CALLBACK_PATH;
    
    public static String generateCodeVerifier() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        return CryptoUtils.base64UrlEncode(code);
    }
    
    public static String generateCodeChallenge(String verifier) {
        try {
            byte[] hash = CryptoUtils.sha256(verifier.getBytes(StandardCharsets.US_ASCII));
            return CryptoUtils.base64UrlEncode(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate code challenge", e);
        }
    }
    
    static String getXBLToken(String accessToken) throws Exception {
        HttpURLConnection connection = createPost("https://user.auth.xboxlive.com/user/authenticate", "application/x-www-form-urlencoded");
        
        String body = "{\n" +
                "    \"Properties\": {\n" +
                "        \"AuthMethod\": \"RPS\",\n" +
                "        \"SiteName\": \"user.auth.xboxlive.com\",\n" +
                "        \"RpsTicket\": \"d=" + accessToken + "\"\n" +
                "    },\n" +
                "    \"RelyingParty\": \"http://auth.xboxlive.com\",\n" +
                "    \"TokenType\": \"JWT\"\n" +
                "}";
        
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        
        try (InputStream is = connection.getInputStream()) {
            JsonObject json = inputStreamToJsonObject(is);
            return json.get("Token").getAsString();
        }
    }
    
    /**
     * @return String[0] = access_token,
     * String[1] = refresh_token
     */
    static String[] getAccessTokenAndRefreshToken(String code, String codeVerifier) throws IOException {
        String body =
                "client_id=" + CLIENT_ID +
                        "&response_type=code" +
                        "&redirect_uri=" + REDIRECT_URI +
                        "&grant_type=authorization_code" +
                        "&code=" + code +
                        "&code_verifier=" + codeVerifier;
        
        HttpURLConnection connection = createPost("https://login.live.com/oauth20_token.srf", "application/x-www-form-urlencoded");
        
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        
        try (InputStream is = connection.getInputStream()) {
            JsonObject json = inputStreamToJsonObject(is);
            
            String accessToken = json.get("access_token").getAsString();
            String refreshToken = json.get("refresh_token").getAsString();
            
            return new String[]{accessToken, refreshToken};
        }
    }
    
    /**
     * @return String[0] = XSTSToken,
     * String[1] = UHS
     */
    static String[] getXSTSTokenAndUHS(String xblToken) throws Exception {
        HttpURLConnection connection = createPost("https://xsts.auth.xboxlive.com/xsts/authorize", "application/x-www-form-urlencoded");
        
        String body = "{\n" +
                "  \"Properties\": {\n" +
                "    \"SandboxId\": \"RETAIL\",\n" +
                "    \"UserTokens\": [\n" +
                "      \"" + xblToken + "\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"RelyingParty\": \"rp://api.minecraftservices.com/\",\n" +
                "  \"TokenType\": \"JWT\"\n" +
                "}\n";
        
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        
        try (InputStream is = connection.getInputStream()) {
            JsonObject json = inputStreamToJsonObject(is);
            
            String token = json.get("Token").getAsString();
            String uhs = json
                    .getAsJsonObject("DisplayClaims")
                    .getAsJsonArray("xui")
                    .get(0).getAsJsonObject()
                    .get("uhs").getAsString();
            return new String[]{token, uhs};
        }
    }
    
    static String getMinecraftAccessToken(String uhs, String xstsToken) throws Exception {
        HttpURLConnection connection = createPost("https://api.minecraftservices.com/authentication/login_with_xbox", "application/json");
        
        String body = "{\n" +
                "    \"identityToken\": \"XBL3.0 x=" + uhs + ";" + xstsToken + "\"\n" +
                "}";
        
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        
        try (InputStream is = connection.getInputStream()) {
            JsonObject json = inputStreamToJsonObject(is);
            return json.get("access_token").getAsString();
        }
    }
    
    static String refreshAccessToken(String refreshToken) throws Exception {
        HttpURLConnection connection = createPost("https://login.microsoftonline.com/consumers/oauth2/v2.0/token", "application/x-www-form-urlencoded");
        
        String body =
                "client_id=" + CLIENT_ID +
                        "&refresh_token=" + refreshToken +
                        "&grant_type=refresh_token" +
                        "&scope=" +
                        "XboxLive.signin%20offline_access";
        
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        
        try (InputStream is = connection.getInputStream()) {
            JsonObject json = inputStreamToJsonObject(is);
            
            if (json.has("access_token")) {
                return json.get("access_token").getAsString();
            } else {
                throw new IOException("Failed to refresh token: " + json.toString());
            }
        }
    }
    
    static boolean checkMinecraftOwnership(String accessToken) throws IOException {
        HttpURLConnection connection = createGet("https://api.minecraftservices.com/entitlements/mcstore", accessToken);
        
        try (InputStream is = connection.getInputStream()) {
            JsonObject json = inputStreamToJsonObject(is);
            JsonArray items = json.getAsJsonArray("items");
            if (items != null) {
                for (JsonElement element : items) {
                    String name = element.getAsJsonObject().get("name").getAsString();
                    if ("product_minecraft".equals(name) || "game_minecraft".equals(name)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * @return String[0] = name,
     * String[1] = uuid
     */
    static String[] getMinecraftProfile(String accessToken) throws Exception {
        HttpURLConnection connection = createGet("https://api.minecraftservices.com/minecraft/profile", accessToken);
        
        try (InputStream is = connection.getInputStream()) {
            JsonObject json = inputStreamToJsonObject(is);
            String name = json.get("name").getAsString();
            String uuid = json.get("id").getAsString();
            return new String[]{name, uuid};
        }
    }
    
    private static HttpURLConnection createPost(String url, String contentType) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", contentType);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        return connection;
    }
    
    
    private static HttpURLConnection createGet(String url, String accessToken) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        return connection;
    }
    
    private static JsonObject inputStreamToJsonObject(InputStream is) throws IOException {
        String responseBody = new String(IOUtils.toByteArray(is), StandardCharsets.UTF_8);
        return JsonParser.parseString(responseBody).getAsJsonObject();
    }
}
