package loftily.utils.render;

import java.awt.*;

public enum Colors {
    BackGround(new Color(10, 10, 10)),
    OnBackGround(new Color(21, 21, 21)),
    Active(new Color(72, 145, 248)),
    Text(new Color(239, 239, 239)),
    SecondaryText(new Color(109, 109, 109));
    
    public final Color color;
    
    Colors(Color color) {
        this.color = color;
    }
}
