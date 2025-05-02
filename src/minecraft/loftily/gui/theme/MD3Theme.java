package loftily.gui.theme;

import lombok.AllArgsConstructor;

import java.awt.*;

import static loftily.gui.theme.ThemeInfo.isDarkMode;

@AllArgsConstructor
public class MD3Theme {
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
    
    private Color darkPrimary;
    private Color darkOnPrimary;
    private Color darkPrimaryContainer;
    private Color darkOnPrimaryContainer;
    private Color darkSecondary;
    private Color darkOnSecondary;
    private Color darkSecondaryContainer;
    private Color darkOnSecondaryContainer;
    private Color darkTertiary;
    private Color darkOnTertiary;
    private Color darkTertiaryContainer;
    private Color darkOnTertiaryContainer;
    private Color darkError;
    private Color darkOnError;
    private Color darkErrorContainer;
    private Color darkOnErrorContainer;
    private Color darkBackground;
    private Color darkOnBackground;
    private Color darkSurface;
    private Color darkOnSurface;
    private Color darkSurfaceVariant;
    private Color darkOnSurfaceVariant;
    private Color darkOutline;
    private Color darkOutlineVariant;
    private Color darkScrim;
    private Color darkInverseSurface;
    private Color darkInverseOnSurface;
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
