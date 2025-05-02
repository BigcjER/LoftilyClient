package loftily.gui.components.md;

import loftily.gui.components.Component;

public abstract class MD3Component extends Component {
    public final float Width, Height, scaleFactor;
    
    public MD3Component(float Width, float Height, float scaleFactor) {
        super(Width * scaleFactor, Height * scaleFactor);
        this.Width = Width;
        this.Height = Height;
        this.scaleFactor = scaleFactor;
    }
}
