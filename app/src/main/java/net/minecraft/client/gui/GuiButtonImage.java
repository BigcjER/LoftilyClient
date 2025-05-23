package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiButtonImage extends GuiButton
{
    private final ResourceLocation field_191750_o;
    private final int field_191747_p;
    private final int field_191748_q;
    private final int field_191749_r;

    public GuiButtonImage(int p_i47392_1_, int p_i47392_2_, int p_i47392_3_, int p_i47392_4_, int p_i47392_5_, int p_i47392_6_, int p_i47392_7_, int p_i47392_8_, ResourceLocation p_i47392_9_)
    {
        super(p_i47392_1_, p_i47392_2_, p_i47392_3_, p_i47392_4_, p_i47392_5_, "");
        this.field_191747_p = p_i47392_6_;
        this.field_191748_q = p_i47392_7_;
        this.field_191749_r = p_i47392_8_;
        this.field_191750_o = p_i47392_9_;
    }

    public void func_191746_c(int p_191746_1_, int p_191746_2_)
    {
        this.xPosition = p_191746_1_;
        this.yPosition = p_191746_2_;
    }
    
    public void drawScreen(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            mc.getTextureManager().bindTexture(this.field_191750_o);
            GlStateManager.disableDepth();
            int i = this.field_191747_p;
            int j = this.field_191748_q;

            if (this.hovered)
            {
                j += this.field_191749_r;
            }

            this.drawTexturedModalRect(this.xPosition, this.yPosition, i, j, this.width, this.height);
            GlStateManager.enableDepth();
        }
    }
}
