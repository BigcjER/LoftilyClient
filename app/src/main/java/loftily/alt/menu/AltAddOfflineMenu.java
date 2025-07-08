package loftily.alt.menu;

import loftily.Client;
import loftily.alt.Alt;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.components.CustomButton;
import loftily.gui.components.CustomTextField;
import loftily.gui.font.FontManager;
import loftily.utils.render.ColorUtils;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.utils.timer.DelayTimer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class AltAddOfflineMenu extends GuiScreen {
    private final DelayTimer timer = new DelayTimer();
    private final CustomTextField nameTextField;
    private final GuiScreen prevScreen;
    private final Animation textAnimation;
    private GuiButton loginButton, addButon;
    private String currentText = "";
    
    public AltAddOfflineMenu(GuiScreen prevScreen, FontRenderer fontRendererObj) {
        this.prevScreen = prevScreen;
        this.nameTextField = new CustomTextField(0, fontRendererObj, 0, 0, 200, 25);
        this.nameTextField.setMaxStringLength(128);
        this.nameTextField.setText("");
        this.nameTextField.setFocused(true);
        this.textAnimation = new Animation(Easing.EaseOutExpo, 250);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        
        this.buttonList.clear();
        int startY = height / 2 - 10;
        this.buttonList.add(loginButton = new CustomButton(1, width / 2 - 100, startY, 200, 25, "Direct login", Colors.OnBackGround.color.brighter()));
        this.buttonList.add(addButon = new CustomButton(2, width / 2 - 100, startY + 30, 200, 25, "Add", Colors.OnBackGround.color.brighter()));
        this.buttonList.add(new CustomButton(3, width / 2 - 100, startY + 60, 200, 25, "Back", Colors.OnBackGround.color.brighter()));
        loginButton.enabled = !nameTextField.getText().isEmpty();
        addButon.enabled = !nameTextField.getText().isEmpty();
        
        Keyboard.enableRepeatEvents(true);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        String name = nameTextField.getText();
        currentText = "";
        timer.reset();
        super.actionPerformed(button);
        switch (button.id) {
            case 1:
                Client.INSTANCE.getAltManager().login(new Alt(name));
                currentText = "Logged in to " + name;
                break;
            
            case 2:
                boolean nameExists = Client.INSTANCE.getAltManager().getAlts().stream()
                        .anyMatch(alt -> alt.getName().equalsIgnoreCase(name));
                
                if (nameExists) {
                    currentText = TextFormatting.RED + "Username already exists!";
                    break;
                }
                
                Client.INSTANCE.getAltManager().add(new Alt(name));
                currentText = "Added " + name;
                break;
            
            case 3:
                mc.displayGuiScreen(prevScreen);
                break;
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRectHW(0, 0, width, height, Colors.BackGround.color);
        
        int x = this.width / 2 - 100;
        int y = height / 5;
        
        textAnimation.run(timer.hasTimeElapsed(3000) ? 1 : 254);
        FontManager.NotoSans.of(16).drawString("Name", x, y, Colors.Text.color);
        this.nameTextField.xPosition = x;
        this.nameTextField.yPosition = y + 12;
        this.nameTextField.drawTextBox();
        
        if (!textAnimation.isFinished() || !timer.hasTimeElapsed(3000)) {
            FontManager.NotoSans.of(16).drawCenteredString(currentText, width / 2F, height / 10F, ColorUtils.colorWithAlpha(Colors.Text.color, textAnimation.getValuei()));
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(prevScreen);
            return;
        }
        
        this.nameTextField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_TAB) {
            this.nameTextField.setFocused(!this.nameTextField.isFocused());
        }
        
        loginButton.enabled = !nameTextField.getText().isEmpty();
        addButon.enabled = !nameTextField.getText().isEmpty();
        super.keyTyped(typedChar, keyCode);
    }
    
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameTextField.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public void updateScreen() {
        this.nameTextField.updateCursorCounter();
    }
    
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        currentText = "";
        super.onGuiClosed();
    }
}
