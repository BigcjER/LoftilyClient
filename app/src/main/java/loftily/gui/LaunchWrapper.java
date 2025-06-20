package loftily.gui;

import loftily.utils.render.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Class1337
 * @since 10/18/2024 - 10:38 PM
 */
public class LaunchWrapper {
    /**
     * dotCount is used to track the number of dots in the "Authenticating" message.
     * It cycles through values from 0 to 3, representing the number of dots displayed
     * in the authentication text (up to three dots). This creates a loading effect
     * during the splash screen.
     */
    static int dotCount = 0;
    /**
     * RotatingLoader Speed.
     */
    static double speed = 0.0;
    /**
     * Launch time.
     */
    static int launch = 3500;
    /**
     * Animation Speed.
     */
    static final float ANIMATION_SPEED = 0.03F;
    /**
     * Cache font.
     */
    static Font font;
    
    /**
     * The entry point of the application. This method initializes the splash screen,
     * sets its content and size, centers it on the screen, and configures its appearance.
     * It also starts timers for closing the splash screen and for updating the
     * dot animation and rotation effect during the splash screen display.
     *
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        JWindow splashScreen = new JWindow();
        JPanel content = createContentPanel();
        
        splashScreen.setContentPane(content);
        splashScreen.setSize(265, 300);
        centerWindow(splashScreen);
        configureSplashScreen(splashScreen);
        
        Timer closeTimer = createCloseTimer(splashScreen);
        closeTimer.start();
        startDotTimer(content);
        startRotationTimer(content);
    }
    
    /**
     * Centers the given window on the screen.
     * It calculates the appropriate x and y coordinates based
     * on the screen size and the window size, then sets the
     * window's location accordingly.
     *
     * @param window The JWindow to be centered.
     */
    private static void centerWindow(JWindow window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - window.getWidth()) / 2;
        int y = (screenSize.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }
    
    /**
     * Configures the appearance of the splash screen.
     * This includes setting the background color to transparent,
     * ensuring the splash screen is always on top, setting its
     * initial opacity to 0, and starting the fade-in effect.
     *
     * @param splashScreen The JWindow representing the splash screen.
     */
    private static void configureSplashScreen(JWindow splashScreen) {
        splashScreen.setBackground(new Color(0, 0, 0, 0));
        splashScreen.setAlwaysOnTop(true);
        splashScreen.setOpacity(0f);
        splashScreen.setVisible(true);
        fadeIn(splashScreen);
    }
    
    /**
     * Starts a timer that updates the dotCount variable
     * to create a loading effect in the authentication message.
     * It cycles the dotCount between 0 and 3 and repaints the
     * content panel at regular intervals.
     *
     * @param content The JPanel that displays the authentication text.
     */
    private static void startDotTimer(JPanel content) {
        new Timer(255, e -> {
            dotCount = (dotCount + 1) % 4;
            content.repaint();
        }).start();
    }
    
    /**
     * Starts a timer that updates the rotation speed for the loader.
     * This timer increments the speed variable and repaints the
     * content panel at regular intervals, creating a rotation effect.
     *
     * @param content The JPanel that displays the rotating loader.
     */
    private static void startRotationTimer(JPanel content) {
        new Timer(25, e -> {
            speed += Math.PI / 35;
            content.repaint();
        }).start();
    }
    
    /**
     * Creates and returns a JPanel for the splash screen content.
     * This panel handles the painting of the rotating loader and
     * the authentication text, managing the graphical display.
     *
     * @return The JPanel used for the splash screen.
     */
    private static JPanel createContentPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Colors.OnBackGround.color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
                
                drawRotatingLoader(g2);
                drawText(g2);
                g2.dispose();
            }
            
            private void drawText(Graphics2D g2) {
                g2.setFont(font);
                g2.setColor(Colors.Text.color);
                
                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < dotCount; i++) {
                    dots.append(".");
                }
                String baseText = "Initializing" + dots;
                
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(baseText);
                int y = (getHeight() + fm.getAscent()) / 2 + 85;
                g2.drawString(baseText, (getWidth() - textWidth) / 2 + 2, y);
            }
            
            private void drawRotatingLoader(Graphics2D g2) {
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2 - 20;
                int radius = 5;
                int loaderRadius = 32;
                int numCircles = 12;
                int visibleCount = 9;
                
                speed += 0.01;
                
                for (int i = 0; i < numCircles; i++) {
                    double angle = speed + (i * 2 * Math.PI / numCircles);
                    int x = centerX + (int) (loaderRadius * Math.cos(angle)) - radius;
                    int y = centerY + (int) (loaderRadius * Math.sin(angle)) - radius;
                    
                    float alpha = (i < visibleCount) ? Math.max(0, 1 - ((dotCount + visibleCount - i) % visibleCount) / (float) visibleCount) : 0;
                    g2.setColor(new Color(255, 255, 255, (int) (alpha * 255)));
                    g2.fillOval(x, y, radius * 2, radius * 2);
                }
                
                // keep [0, 2π)
                speed = (speed + Math.PI * 2) % (Math.PI * 2);
            }
        };
    }
    
    /**
     * Initiates a fade-in effect for the splash screen.
     * This method increases the opacity of the splash screen
     * gradually until it becomes fully visible.
     *
     * @param splashScreen The JWindow representing the splash screen.
     */
    private static void fadeIn(JWindow splashScreen) {
        Timer fadeInTimer = new Timer(10, e -> {
            float newOpacity = Math.min(splashScreen.getOpacity() + ANIMATION_SPEED, 1.0f);
            splashScreen.setOpacity(newOpacity);
            if (newOpacity >= 1.0f) ((Timer) e.getSource()).stop();
        });
        fadeInTimer.start();
    }
    
    /**
     * Creates a timer to manage the closing of the splash screen.
     * This timer gradually decreases the opacity until the splash
     * screen is fully transparent, then disposes of it and
     * starts the main application.
     *
     * @param splashScreen The JWindow representing the splash screen.
     * @return A Timer that handles the closing of the splash screen.
     */
    private static Timer createCloseTimer(JWindow splashScreen) {
        return new Timer(launch, e -> new Timer(10, event -> {
            float newOpacity = Math.max(splashScreen.getOpacity() - ANIMATION_SPEED, 0.0f);
            splashScreen.setOpacity(newOpacity);
            if (newOpacity <= 0.0f) {
                splashScreen.setVisible(false);
                splashScreen.dispose();
                
                ((Timer) event.getSource()).stop();
            }
        }).start());
    }
    
    static {
        try (InputStream is = LaunchWrapper.class.getResourceAsStream("/assets/minecraft/loftily/fonts/NotoSansSC-Regular.ttf")) {
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            font = baseFont.deriveFont(Font.PLAIN, 18f);
        } catch (IOException | FontFormatException e) {
            font = new Font("SansSerif", Font.PLAIN, 18);
            throw new RuntimeException(e);
        }
    }
}