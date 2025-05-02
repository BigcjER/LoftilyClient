package loftily.gui.font;

import loftily.utils.client.MinecraftInstance;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class FontRenderer implements MinecraftInstance {
    private static final int[] colorCode;
    
    static {
        colorCode = new int[32];
        for (int i = 0; i < 32; ++i) {
            final int base = (i >> 3 & 0x1) * 85;
            int r = (i >> 2 & 0x1) * 170 + base;
            int g = (i >> 1 & 0x1) * 170 + base;
            int b = (i & 0x1) * 170 + base;
            if (i == 6) {
                r += 85;
            }
            if (i >= 16) {
                r /= 4;
                g /= 4;
                b /= 4;
            }
            FontRenderer.colorCode[i] = ((r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF));
        }
    }
    
    private final byte[][] charwidth;
    private final int[] textures;
    private final FontRenderContext context;
    private final Font font;
    private float size;
    private int fontWidth;
    private int fontHeight;
    private int textureWidth;
    private int textureHeight;
    
    public FontRenderer(final Font font) {
        this.charwidth = new byte[256][];
        this.textures = new int[256];
        this.context = new FontRenderContext(new AffineTransform(), true, true);
        this.size = 0.0f;
        this.fontWidth = 0;
        this.fontHeight = 0;
        this.textureWidth = 0;
        this.textureHeight = 0;
        this.font = font;
        this.size = font.getSize2D();
        Arrays.fill(this.textures, -1);
        final Rectangle2D maxBounds = font.getMaxCharBounds(this.context);
        this.fontWidth = (int) Math.ceil(maxBounds.getWidth());
        this.fontHeight = (int) Math.ceil(maxBounds.getHeight());
        if (this.fontWidth > 127 || this.fontHeight > 127) {
            throw new IllegalArgumentException("Font size to large!");
        }
        this.textureWidth = this.resizeToOpenGLSupportResolution(this.fontWidth * 16);
        this.textureHeight = this.resizeToOpenGLSupportResolution(this.fontHeight * 16);
    }
    
    private static ByteBuffer imageToBuffer(final BufferedImage img) {
        final int[] arr = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        final ByteBuffer buf = ByteBuffer.allocateDirect(4 * arr.length);
        for (final int i : arr) {
            buf.putInt(i << 8 | (i >> 24 & 0xFF));
        }
        buf.flip();
        return buf;
    }
    
    public static int getHeight(String text) {
        return mc.fontRendererObj.FONT_HEIGHT;
    }
    
    public final float drawCenteredString(final String text, final float x, final float y, final int color) {
        return (float) this.drawString(text, x - this.getStringWidth(text) / 2, y, color);
    }
    
    public final float drawCenteredStringNoFormat(final String text, final float x, final float y, final int color) {
        return (float) this.drawStringNoFormat(text, x - this.getStringWidth(text) / 2, y, color, false);
    }
    
    public final void drawCenteredStringWithShadow(final String text, final float x, final float y, final int color) {
        this.drawStringWithShadow(text, x - this.getStringWidth(text) / 2, y, color);
    }
    
    public String trimStringToWidth(final String p_trimStringToWidth_1_, final int p_trimStringToWidth_2_, final boolean p_trimStringToWidth_3_) {
        final StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        final int j = p_trimStringToWidth_3_ ? (p_trimStringToWidth_1_.length() - 1) : 0;
        final int k = p_trimStringToWidth_3_ ? -1 : 1;
        boolean flag = false;
        boolean flag2 = false;
        for (int l = j; l >= 0 && l < p_trimStringToWidth_1_.length() && i < p_trimStringToWidth_2_; l += k) {
            final char c0 = p_trimStringToWidth_1_.charAt(l);
            final int i2 = this.getStringWidth(String.valueOf(c0));
            if (flag) {
                flag = false;
                if (c0 != 'l' && c0 != 'L') {
                    if (c0 == 'r' || c0 == 'R') {
                        flag2 = false;
                    }
                } else {
                    flag2 = true;
                }
            } else if (i2 < 0) {
                flag = true;
            } else {
                i += i2;
                if (flag2) {
                    ++i;
                }
            }
            if (i > p_trimStringToWidth_2_) {
                break;
            }
            if (p_trimStringToWidth_3_) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }
        return stringbuilder.toString();
    }
    
    public final int getHeight() {
        return this.fontHeight / 2;
    }
    
    public final int getFontHeight() {
        return this.fontHeight / 2;
    }
    
    protected final int drawChar(final char chr, final float x, final float y) {
        final int region = chr >> 8;
        final int id = chr & 'ÿ';
        final int xTexCoord = (id & 0xF) * this.fontWidth;
        final int yTexCoord = (id >> 4) * this.fontHeight;
        final int width = this.getOrGenerateCharWidthMap(region)[id];
        GlStateManager.bindTexture(this.getOrGenerateCharTexture(region));
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GL11.glBegin(7);
        GL11.glTexCoord2d(this.wrapTextureCoord(xTexCoord, this.textureWidth), this.wrapTextureCoord(yTexCoord, this.textureHeight));
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2d(this.wrapTextureCoord(xTexCoord, this.textureWidth), this.wrapTextureCoord(yTexCoord + this.fontHeight, this.textureHeight));
        GL11.glVertex2f(x, y + this.fontHeight);
        GL11.glTexCoord2d(this.wrapTextureCoord(xTexCoord + width, this.textureWidth), this.wrapTextureCoord(yTexCoord + this.fontHeight, this.textureHeight));
        GL11.glVertex2f(x + width, y + this.fontHeight);
        GL11.glTexCoord2d(this.wrapTextureCoord(xTexCoord + width, this.textureWidth), this.wrapTextureCoord(yTexCoord, this.textureHeight));
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
        return width;
    }
    
    public int drawString(final String str, final float x, final float y, final int color) {
        return this.drawString(str, x, y, color, false);
    }
    
    public int drawString(final String str, final float x, final float y) {
        return this.drawString(str, x, y, -1);
    }
    
    public final int drawStringNoFormat(String str, float x, float y, int color, final boolean darken) {
        str = str.replace("\u25ac", "=");
        y -= 2.0f;
        x *= 2.0f;
        y *= 2.0f;
        y -= 2.0f;
        int offset = 0;
        if (darken) {
            color = ((color & 0xFCFCFC) >> 2 | (color & 0xFF000000));
        }
        final float r = (color >> 16 & 0xFF) / 255.0f;
        final float g = (color >> 8 & 0xFF) / 255.0f;
        final float b = (color & 0xFF) / 255.0f;
        float a = (color >> 24 & 0xFF) / 255.0f;
        if (a == 0.0f) {
            a = 1.0f;
        }
        GlStateManager.color(r, g, b, a);
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);
        final char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            final char chr = chars[i];
            if (chr == '§' && i != chars.length - 1) {
                ++i;
            } else {
                offset += this.drawChar(chr, x + offset, y);
            }
        }
        GL11.glPopMatrix();
        return offset;
    }
    
    public final int drawString(String str, float x, float y, int color, final boolean darken) {
        str = str.replace("\u25ac", "=");
        y -= 2.0f;
        x *= 2.0f;
        y *= 2.0f;
        y -= 2.0f;
        int offset = 0;
        if (darken) {
            color = ((color & 0xFCFCFC) >> 2 | (color & 0xFF000000));
        }
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = (color >> 24 & 0xFF) / 255.0f;
        if (a == 0.0f) {
            a = 1.0f;
        }
        GlStateManager.color(r, g, b, a);
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);
        final char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            final char chr = chars[i];
            if (chr == '§' && i != chars.length - 1) {
                ++i;
                color = "0123456789abcdef".indexOf(chars[i]);
                if (color != -1) {
                    if (darken) {
                        color |= 0x10;
                    }
                    color = FontRenderer.colorCode[color];
                    r = (color >> 16 & 0xFF) / 255.0f;
                    g = (color >> 8 & 0xFF) / 255.0f;
                    b = (color & 0xFF) / 255.0f;
                    GlStateManager.color(r, g, b, a);
                }
            } else {
                offset += this.drawChar(chr, x + offset, y);
            }
        }
        GL11.glPopMatrix();
        return offset;
    }
    
    public float getMiddleOfBox(final float height) {
        return height / 2.0f - this.getHeight() / 2.0f;
    }
    
    public final int getStringWidth(final String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        final char[] currentData = text.toCharArray();
        for (int size = text.length(), i = 0; i < size; ++i) {
            final char chr = currentData[i];
            final char character = text.charAt(i);
            if (character == '§') {
                ++i;
            } else {
                width += this.getOrGenerateCharWidthMap(chr >> 8)[chr & '\u00ff'];
            }
        }
        return width / 2;
    }
    
    public final float getSize() {
        return this.size;
    }
    
    private int generateCharTexture(final int id) {
        final int textureId = GL11.glGenTextures();
        final int offset = id << 8;
        final BufferedImage img = new BufferedImage(this.textureWidth, this.textureHeight, 2);
        final Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setFont(this.font);
        g.setColor(Color.WHITE);
        final FontMetrics fontMetrics = g.getFontMetrics();
        for (int y = 0; y < 16; ++y) {
            for (int x = 0; x < 16; ++x) {
                final String chr = String.valueOf((char) (y << 4 | x | offset));
                g.drawString(chr, x * this.fontWidth, y * this.fontHeight + fontMetrics.getAscent());
            }
        }
        GL11.glBindTexture(3553, textureId);
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glTexImage2D(3553, 0, 6408, this.textureWidth, this.textureHeight, 0, 6408, 5121, imageToBuffer(img));
        return textureId;
    }
    
    private int getOrGenerateCharTexture(final int id) {
        if (this.textures[id] == -1) {
            return this.textures[id] = this.generateCharTexture(id);
        }
        return this.textures[id];
    }
    
    private int resizeToOpenGLSupportResolution(final int size) {
        int power;
        for (power = 0; size > 1 << power; ++power) {
        }
        return 1 << power;
    }
    
    private byte[] generateCharWidthMap(final int id) {
        final int offset = id << 8;
        final byte[] widthmap = new byte[256];
        for (int i = 0; i < widthmap.length; ++i) {
            widthmap[i] = (byte) Math.ceil(this.font.getStringBounds(String.valueOf((char) (i | offset)), this.context).getWidth());
        }
        return widthmap;
    }
    
    private byte[] getOrGenerateCharWidthMap(final int id) {
        if (this.charwidth[id] == null) {
            return this.charwidth[id] = this.generateCharWidthMap(id);
        }
        return this.charwidth[id];
    }
    
    private double wrapTextureCoord(final int coord, final int size) {
        return coord / (double) size;
    }
    
    @Override
    protected final void finalize() {
        for (final int textureId : this.textures) {
            if (textureId != -1) {
                GL11.glDeleteTextures(textureId);
            }
        }
    }
    
    public final void drawStringWithShadow(final String newstr, final float i, final float i1, final int rgb) {
        this.drawString(newstr, i + 0.5f, i1 + 0.5f, rgb, true);
        this.drawString(newstr, i, i1, rgb);
    }
    
    public final void drawLimitedString(final String text, final float x, final float y, final int color, final float maxWidth) {
        this.drawLimitedStringWithAlpha(text, x, y, color, (color >> 24 & 0xFF) / 255.0f, maxWidth);
    }
    
    public final void drawLimitedStringWithAlpha(final String text, float x, float y, final int color, final float alpha, final float maxWidth) {
        x *= 2.0f;
        y *= 2.0f;
        final float originalX = x;
        float curWidth = 0.0f;
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);
        final boolean wasBlend = GL11.glGetBoolean(3042);
        GlStateManager.enableAlpha();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3553);
        int currentColor = color;
        final char[] characters = text.toCharArray();
        int index = 0;
        for (final char c : characters) {
            if (c == '\r') {
                x = originalX;
            }
            if (c == '\n') {
                y += this.getFontHeight() * 2.0f;
            }
            Label_0362:
            {
                if (c != '§' && (index == 0 || index == characters.length - 1 || characters[index - 1] != '§')) {
                    if (index >= 1 && characters[index - 1] == '§') {
                        break Label_0362;
                    }
                    GL11.glPushMatrix();
                    this.drawString(Character.toString(c), x, y, new Color(new Color(currentColor).getRed(), new Color(currentColor).getGreen(), new Color(currentColor).getBlue(), alpha).getRGB(), false);
                    GL11.glPopMatrix();
                    curWidth += this.getStringWidth(Character.toString(c)) * 2.0f;
                    x += this.getStringWidth(Character.toString(c)) * 2.0f;
                    if (curWidth > maxWidth) {
                        break;
                    }
                } else if (c == ' ') {
                    x += this.getStringWidth(" ");
                } else if (c == '§' && index != characters.length - 1) {
                    final int codeIndex = "0123456789abcdefklmnor".indexOf(text.charAt(index + 1));
                    if (codeIndex < 0) {
                        break Label_0362;
                    }
                    if (codeIndex < 16) {
                        currentColor = FontRenderer.colorCode[codeIndex];
                    } else if (codeIndex == 21) {
                        currentColor = Color.WHITE.getRGB();
                    }
                }
                ++index;
            }
        }
        if (!wasBlend) {
            GL11.glDisable(3042);
        }
        GL11.glPopMatrix();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public final void drawOutlinedString(final String str, final float x, final float y, final int internalCol, final int externalCol) {
        this.drawString(str, x - 0.5f, y, externalCol);
        this.drawString(str, x + 0.5f, y, externalCol);
        this.drawString(str, x, y - 0.5f, externalCol);
        this.drawString(str, x, y + 0.5f, externalCol);
        this.drawString(str, x, y, internalCol);
    }
    
    public void drawStringWithShadow(final String z, final double x, final double positionY, final int mainTextColor) {
        this.drawStringWithShadow(z, (float) x, (float) positionY, mainTextColor);
    }
    
    public double getStringHeight() {
        return this.getHeight();
    }
    
    public float drawStringWithShadow(final String text, final double x, final double y, final double sWidth, final int color) {
        final float shadowWidth = (float) this.drawString(text, (float) (x + sWidth), (float) (y + sWidth), color, true);
        return Math.max(shadowWidth, (float) this.drawString(text, (float) x, (float) y, color, false));
    }
    
    public int getWidth(String text) {
        return mc.fontRendererObj.getStringWidth(text);
    }
}
