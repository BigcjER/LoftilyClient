package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import loftily.gui.LaunchWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.Session;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        LaunchWrapper.main(args);
        
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        optionParser.accepts("demo");
        optionParser.accepts("fullscreen");
        optionParser.accepts("checkGlErrors");
        OptionSpec<String> server = optionParser.accepts("server").withRequiredArg();
        OptionSpec<Integer> port = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(25565);
        OptionSpec<File> gameDir = optionParser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."));
        OptionSpec<File> assetsDir = optionParser.accepts("assetsDir").withRequiredArg().ofType(File.class);
        OptionSpec<File> resourcePackDir = optionParser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        OptionSpec<String> proxyHost = optionParser.accepts("proxyHost").withRequiredArg();
        OptionSpec<Integer> proxyPort = optionParser.accepts("proxyPort").withRequiredArg().defaultsTo("8080").ofType(Integer.class);
        OptionSpec<String> proxyUser = optionParser.accepts("proxyUser").withRequiredArg();
        OptionSpec<String> proxyPass = optionParser.accepts("proxyPass").withRequiredArg();
        OptionSpec<String> username = optionParser.accepts("username").withRequiredArg().defaultsTo("Player" + Minecraft.getSystemTime() % 1000L);
        OptionSpec<String> uuid = optionParser.accepts("uuid").withRequiredArg();
        OptionSpec<String> accessToken = optionParser.accepts("accessToken").withRequiredArg().required();
        OptionSpec<String> version = optionParser.accepts("version").withRequiredArg().required();
        OptionSpec<String> userProperties = optionParser.accepts("userProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> profileProperties = optionParser.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> assetIndex = optionParser.accepts("assetIndex").withRequiredArg();
        OptionSpec<String> userType = optionParser.accepts("userType").withRequiredArg().defaultsTo("legacy");
        OptionSpec<String> versionType = optionParser.accepts("versionType").withRequiredArg().defaultsTo("release");
        
        OptionSpec<String> nonOptionArgsSpec = optionParser.nonOptions();
        OptionSet optionset = optionParser.parse(args);
        List<String> list = optionset.valuesOf(nonOptionArgsSpec);
        
        if (!list.isEmpty()) {
            System.out.println("Completely ignored arguments: " + list);
        }
        
        String proxyHostString = optionset.valueOf(proxyHost);
        Proxy proxy = Proxy.NO_PROXY;
        
        if (proxyHostString != null) {
            try {
                proxy = new Proxy(Type.SOCKS, new InetSocketAddress(proxyHostString, (optionset.valueOf(proxyPort))));
            } catch (Exception ignored) {
            }
        }
        
        final String proxyUserString = optionset.valueOf(proxyUser);
        final String proxyPassString = optionset.valueOf(proxyPass);
        
        if (!proxy.equals(Proxy.NO_PROXY) && !proxyUserString.isEmpty() && proxyPassString.isEmpty()) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(proxyUserString, proxyPassString.toCharArray());
                }
            });
        }
        
        int screenWidth = 1200;
        int screenHeight = 675;
        boolean fullscreen = optionset.has("fullscreen");
        boolean checkGlErrors = optionset.has("checkGlErrors");
        boolean demo = optionset.has("demo");
        String versionString = optionset.valueOf(version);
        Gson gson = (new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new Serializer()).create();
        PropertyMap userPropertiesMap = JsonUtils.gsonDeserialize(gson, optionset.valueOf(userProperties), PropertyMap.class);
        PropertyMap profilePropertiesMap = JsonUtils.gsonDeserialize(gson, optionset.valueOf(profileProperties), PropertyMap.class);
        String versionTypeString = optionset.valueOf(versionType);
        
        File gameDirFile = optionset.valueOf(gameDir);
        File assetsDirFile = optionset.has(assetsDir) ? optionset.valueOf(assetsDir) : new File(gameDirFile, "assets/");
        File resourcePackDirFile = optionset.has(resourcePackDir) ? optionset.valueOf(resourcePackDir) : new File(gameDirFile, "resourcepacks/");
        
        String uuidString = optionset.has(uuid) ? uuid.value(optionset) : username.value(optionset);
        String assetsIndexString = optionset.has(assetIndex) ? assetIndex.value(optionset) : null;
        String serverString = optionset.valueOf(server);
        
        Session session = new Session(username.value(optionset), uuidString, accessToken.value(optionset), userType.value(optionset));
        GameConfiguration gameConfiguration =
                new GameConfiguration(
                        new GameConfiguration.UserInformation(session, userPropertiesMap, profilePropertiesMap, proxy),
                        new GameConfiguration.DisplayInformation(screenWidth, screenHeight, fullscreen, checkGlErrors),
                        new GameConfiguration.FolderInformation(gameDirFile, resourcePackDirFile, assetsDirFile, assetsIndexString),
                        new GameConfiguration.GameInformation(demo, versionString, versionTypeString),
                        new GameConfiguration.ServerInformation(serverString, optionset.valueOf(port)));
        
        Runtime.getRuntime().addShutdownHook(new Thread("Client Shutdown Thread") {
            public void run() {
                Minecraft.stopIntegratedServer();
            }
        });
        
        Thread.currentThread().setName("Client thread");
        (new Minecraft(gameConfiguration)).run();
    }
}