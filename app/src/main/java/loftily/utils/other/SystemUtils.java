package loftily.utils.other;

import loftily.utils.client.ClientUtils;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

public class SystemUtils implements ClientUtils {
    public static String getCpuId() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String[] cmd;
        
        if (os.contains("win")) cmd = new String[]{"wmic", "cpu", "get", "ProcessorId"};
        else if (os.contains("mac")) cmd = new String[]{"/bin/sh", "-c", "sysctl -n machdep.cpu.brand_string"};
        else cmd = new String[]{"/bin/sh", "-c", "cat /proc/cpuinfo | grep 'Serial\\|ID'"};
        
        Process process = Runtime.getRuntime().exec(cmd);
        try (Scanner sc = new Scanner(process.getInputStream())) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (!line.isEmpty() && !line.toLowerCase().contains("processorid")) {
                    String[] parts = line.split(":");
                    return parts.length > 1 ? parts[1].trim() : parts[0].trim();
                }
            }
        }
        
        return null;
    }
    
    public static byte[] getCpuIdSha256() throws Exception {
        return CryptoUtils.sha256(Objects.requireNonNull(SystemUtils.getCpuId()).getBytes(StandardCharsets.UTF_8));
    }
    
    
    public static void copyToClipboard(String text) {
        StringSelection ss = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }
}
