package loftily.utils.render;

import loftily.utils.client.ClientUtils;
import loftily.utils.client.FileUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static net.minecraft.client.renderer.OpenGlHelper.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

public class ShaderUtils implements ClientUtils {
    public final int programID;
    
    public ShaderUtils(Shader shader) {
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program,
                compileShader(new ByteArrayInputStream(shader.code.getBytes()), GL_FRAGMENT_SHADER));
        
        GL20.glAttachShader(program,
                compileShader(new ByteArrayInputStream(Shader.Vertex.code.getBytes()), GL_VERTEX_SHADER));
        
        GL20.glLinkProgram(program);
        
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException(String.format("Shader program linking failed! Details: %s", GL20.glGetProgramInfoLog(program, 500)));
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
    
    private int compileShader(InputStream inputStream, int shaderType) {
        int shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, FileUtils.readInputStream(inputStream));
        GL20.glCompileShader(shader);
        
        if (GL20.glGetShaderi(shader, GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException(String.format("Shader compilation failed! Type: %d, Details: %s", shaderType, GL20.glGetShaderInfoLog(shader, 4096)));
        }
        
        return shader;
    }
}