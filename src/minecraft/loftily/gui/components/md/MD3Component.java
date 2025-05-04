package loftily.gui.components.md;

import loftily.Client;
import loftily.gui.components.Component;
import loftily.gui.theme.Theme;

public abstract class MD3Component extends Component {
    public final float Width, Height, scaleFactor;
    
    public MD3Component(float Width, float Height, float scaleFactor) {
        super(Width * scaleFactor, Height * scaleFactor);
        this.Width = Width;
        this.Height = Height;
        this.scaleFactor = scaleFactor;
    }
    
    protected final Theme getTheme() {
        return Client.INSTANCE.getTheme();
    }
}
