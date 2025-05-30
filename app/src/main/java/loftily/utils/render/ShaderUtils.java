package loftily.utils.render;

import loftily.utils.client.ClientUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static net.minecraft.client.renderer.OpenGlHelper.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

public class ShaderUtils implements ClientUtils {
    public final int programID;
    
    public ShaderUtils(Shader shader) {
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, compileShader(shader.fileName, GL_FRAGMENT_SHADER));
        GL20.glAttachShader(program, compileShader(Shader.Vertex.fileName, GL_VERTEX_SHADER));
        GL20.glLinkProgram(program);
        
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("Shader linking failed: " + GL20.glGetProgramInfoLog(program, 500));
        }
        
        this.programID = program;
    }
    
    public static void drawQuad(double x, double y, double width, double height) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0F, 0.0F);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(1.0F, 0.0F);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2f(1.0F, 1.0F);
        GL11.glVertex2d(x + width, y);
        GL11.glTexCoord2f(0.0F, 1.0F);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }
    
    public static void drawQuad() {
        final ScaledResolution sr = new ScaledResolution(mc);
        final float width = (float) sr.getScaledWidth_double();
        final float height = (float) sr.getScaledHeight_double();
        drawQuad(0, 0, width, height);
    }
    
    public void start() {
        GL20.glUseProgram(this.programID);
    }
    
    public void stop() {
        GL20.glUseProgram(0);
    }
    
    public void setUniformf(String name, float... args) {
        final int loc = GL20.glGetUniformLocation(this.programID, name);
        switch (args.length) {
            case 1: {
                GL20.glUniform1f(loc, args[0]);
                break;
            }
            case 2: {
                GL20.glUniform2f(loc, args[0], args[1]);
                break;
            }
            case 3: {
                GL20.glUniform3f(loc, args[0], args[1], args[2]);
                break;
            }
            case 4: {
                GL20.glUniform4f(loc, args[0], args[1], args[2], args[3]);
                break;
            }
        }
    }
    
    private int compileShader(String fileName, int shaderType) {
        int shader = GL20.glCreateShader(shaderType);
        try (InputStream input = ShaderUtils.class.getResourceAsStream("/assets/minecraft/loftily/shaders/" + fileName)) {
            if (input == null) {
                throw new IllegalArgumentException("Shader file not found: " + fileName);
            }
            String source = IOUtils.toString(input, StandardCharsets.UTF_8);
            GL20.glShaderSource(shader, source);
            GL20.glCompileShader(shader);
            
            if (GL20.glGetShaderi(shader, GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                throw new IllegalStateException(String.format("Shader compilation failed! Type: %d, File: %s, Details: %s",
                        shaderType, fileName, GL20.glGetShaderInfoLog(shader, 4096)));
            }
            
            return shader;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader: " + fileName, e);
        }
    }
    
    
}