package loftily.gui.theme;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@AllArgsConstructor
public class Theme {
    public static boolean isDarkMode;
    
    private Color primary;
    private Color onPrimary;
    private Color primaryContainer;
    private Color onPrimaryContainer;
    private Color secondary;
    private Color onSecondary;
    private Color secondaryContainer;
    private Color onSecondaryContainer;
    private Color tertiary;
    private Color onTertiary;
    private Color tertiaryContainer;
    private Color onTertiaryContainer;
    private Color error;
    private Color onError;
    private Color errorContainer;
    private Color onErrorContainer;
    private Color background;
    private Color onBackground;
    private Color surface;
    private Color onSurface;
    private Color surfaceVariant;
    private Color onSurfaceVariant;
    private Color outline;
    private Color outlineVariant;
    private Color scrim;
    private Color inverseSurface;
    private Color inverseOnSurface;
    private Color inversePrimary;
    
    @Getter
    private Color darkPrimary;
    @Getter
    private Color darkOnPrimary;
    @Getter
    private Color darkPrimaryContainer;
    @Getter
    private Color darkOnPrimaryContainer;
    @Getter
    private Color darkSecondary;
    @Getter
    private Color darkOnSecondary;
    @Getter
    private Color darkSecondaryContainer;
    @Getter
    private Color darkOnSecondaryContainer;
    @Getter
    private Color darkTertiary;
    @Getter
    private Color darkOnTertiary;
    @Getter
    private Color darkTertiaryContainer;
    @Getter
    private Color darkOnTertiaryContainer;
    @Getter
    private Color darkError;
    @Getter
    private Color darkOnError;
    @Getter
    private Color darkErrorContainer;
    @Getter
    private Color darkOnErrorContainer;
    @Getter
    private Color darkBackground;
    @Getter
    private Color darkOnBackground;
    @Getter
    private Color darkSurface;
    @Getter
    private Color darkOnSurface;
    @Getter
    private Color darkSurfaceVariant;
    @Getter
    private Color darkOnSurfaceVariant;
    @Getter
    private Color darkOutline;
    @Getter
    private Color darkOutlineVariant;
    @Getter
    private Color darkScrim;
    @Getter
    private Color darkInverseSurface;
    @Getter
    private Color darkInverseOnSurface;
    @Getter
    private Color darkInversePrimary;
    
    public Color getPrimary() {
        return isDarkMode ? darkPrimary : primary;
    }
    
    public Color getOnPrimary() {
        return isDarkMode ? darkOnPrimary : onPrimary;
    }
    
    public Color getPrimaryContainer() {
        return isDarkMode ? darkPrimaryContainer : primaryContainer;
    }
    
    public Color getOnPrimaryContainer() {
        return isDarkMode ? darkOnPrimaryContainer : onPrimaryContainer;
    }
    
    public Color getSecondary() {
        return isDarkMode ? darkSecondary : secondary;
    }
    
    public Color getOnSecondary() {
        return isDarkMode ? darkOnSecondary : onSecondary;
    }
    
    public Color getSecondaryContainer() {
        return isDarkMode ? darkSecondaryContainer : secondaryContainer;
    }
    
    public Color getOnSecondaryContainer() {
        return isDarkMode ? darkOnSecondaryContainer : onSecondaryContainer;
    }
    
    public Color getTertiary() {
        return isDarkMode ? darkTertiary : tertiary;
    }
    
    public Color getOnTertiary() {
        return isDarkMode ? darkOnTertiary : onTertiary;
    }
    
    public Color getTertiaryContainer() {
        return isDarkMode ? darkTertiaryContainer : tertiaryContainer;
    }
    
    public Color getOnTertiaryContainer() {
        return isDarkMode ? darkOnTertiaryContainer : onTertiaryContainer;
    }
    
    public Color getError() {
        return isDarkMode ? darkError : error;
    }
    
    public Color getOnError() {
        return isDarkMode ? darkOnError : onError;
    }
    
    public Color getErrorContainer() {
        return isDarkMode ? darkErrorContainer : errorContainer;
    }
    
    public Color getOnErrorContainer() {
        return isDarkMode ? darkOnErrorContainer : onErrorContainer;
    }
    
    public Color getBackground() {
        return isDarkMode ? darkBackground : background;
    }
    
    public Color getOnBackground() {
        return isDarkMode ? darkOnBackground : onBackground;
    }
    
    public Color getSurface() {
        return isDarkMode ? darkSurface : surface;
    }
    
    public Color getOnSurface() {
        return isDarkMode ? darkOnSurface : onSurface;
    }
    
    public Color getSurfaceVariant() {
        return isDarkMode ? darkSurfaceVariant : surfaceVariant;
    }
    
    public Color getOnSurfaceVariant() {
        return isDarkMode ? darkOnSurfaceVariant : onSurfaceVariant;
    }
    
    public Color getOutline() {
        return isDarkMode ? darkOutline : outline;
    }
    
    public Color getOutlineVariant() {
        return isDarkMode ? darkOutlineVariant : outlineVariant;
    }
    
    public Color getScrim() {
        return isDarkMode ? darkScrim : scrim;
    }
    
    public Color getInverseSurface() {
        return isDarkMode ? darkInverseSurface : inverseSurface;
    }
    
    public Color getInverseOnSurface() {
        return isDarkMode ? darkInverseOnSurface : inverseOnSurface;
    }
    
    public Color getInversePrimary() {
        return isDarkMode ? darkInversePrimary : inversePrimary;
    }
    
    
}
