package loftily.utils.render;

public enum Shader {
    RoundedRect("rounded_rect.frag"),
    RoundRectOutline("round_rect_outline.frag"),
    Vertex("vertex.vert"),
    BackGround("background.frag");
    
    public final String fileName;
    
    Shader(String fileName) {
        this.fileName = fileName;
    }
}
