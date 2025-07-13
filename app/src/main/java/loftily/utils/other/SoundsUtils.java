package loftily.utils.other;

import loftily.utils.client.ClientUtils;
import loftily.utils.client.MessageUtils;
import net.minecraft.util.text.TextFormatting;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundsUtils implements ClientUtils {
    private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    
    
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down sound player thread pool...");
            singleThreadExecutor.shutdown();
        }, "Client thread"));
    }
    
    public static void playSound(File soundFile) {
        if (!soundFile.exists()) {
            return;
        }
        
        singleThreadExecutor.submit(() -> {
            AudioInputStream audioStream = null;
            Clip clip = null;
            
            try {
                audioStream = AudioSystem.getAudioInputStream(soundFile);
                clip = AudioSystem.getClip();
                
                final Clip finalClip = clip;
                final AudioInputStream finalAudioStream = audioStream;
                
                //Close if sound stop
                finalClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        finalClip.close();
                        try {
                            finalAudioStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                
                clip.open(audioStream);
                clip.start();
                
            } catch (Exception e) {
                //Clean up
                if (clip != null) {
                    clip.close();
                }
                
                if (audioStream != null) {
                    try {
                        audioStream.close();
                    } catch (IOException ex) {
                        MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + ex.toString() + ", please read the log file.");
                        ex.printStackTrace();
                    }
                }
                
                MessageUtils.clientMessageWithWaterMark(TextFormatting.RED + e.toString() + ", please read the log file.");
                e.printStackTrace();
            }
        });
    }
}