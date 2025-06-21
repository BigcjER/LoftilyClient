package loftily.gui.clickgui;

import loftily.Client;
import loftily.config.impl.json.ModuleJsonConfig;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.clickgui.components.impl.CategoryButton;
import loftily.gui.clickgui.components.impl.ModuleButton;
import loftily.gui.clickgui.value.ValuePanel;
import loftily.gui.components.CustomTextField;
import loftily.gui.components.MaterialIcons;
import loftily.gui.font.FontManager;
import loftily.gui.interaction.Scrollable;
import loftily.gui.interaction.draggable.Draggable;
import loftily.gui.interaction.draggable.IDraggable;
import loftily.module.ModuleCategory;
import loftily.utils.render.ColorUtils;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClickGui extends GuiScreen implements IDraggable {
    //Positions
    public static final int CORNER_RADIUS = 3, PADDING = 5;
    private final int width, height;
    private final Scrollable scrollableModuleButtons;
    private final float valuePanelWidth = 160;
    private Draggable draggable;
    private int x, y;
    
    //CategoryButtons,ModuleButtons
    public final List<CategoryButton> categoryButtons;
    public List<ModuleButton> currentModuleButtons;
    public CategoryButton currentCategoryButton;
    public CategoryButton prevCategoryButton;
    @Getter
    private ValuePanel currentValuePanel;
    
    //Search
    private final CustomTextField searchBox;
    private final Animation searchButtonAnimation;
    private final Animation searchBoxInOutAnimation;
    private boolean isSearching, hoveringSearchButton;
    
    public ClickGui() {
        this.width = 430;
        this.height = 270;
        
        this.categoryButtons = new ArrayList<>();
        
        Arrays.stream(ModuleCategory.values()).forEach(category ->
                categoryButtons.add(new CategoryButton(90, 20, 98, 70, category, this)));
        this.currentCategoryButton = categoryButtons.get(0);
        
        this.currentModuleButtons = currentCategoryButton.moduleButtons;
        
        this.scrollableModuleButtons = new Scrollable(8);
        
        this.searchBox = new CustomTextField(0, Minecraft.getMinecraft().fontRendererObj, 0, 0, 140, 25);
        this.searchBox.setMaxStringLength(128);
        this.searchBox.setText("");
        this.searchButtonAnimation = new Animation(Easing.EaseOutCirc, 250);
        this.searchBoxInOutAnimation = new Animation(Easing.EaseOutCirc, 250);
        this.hoveringSearchButton = false;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        categoryButtons.forEach(categoryButton -> categoryButton
                .moduleButtons.forEach(moduleButton -> moduleButton.valuePanel.initGui()));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        getDraggable().updateDrag(mouseX, mouseY, width, height / 10, width, height, super.width, super.height);
        x = getDraggable().getPosX();
        y = getDraggable().getPosY();
        
        /* Background */
        RenderUtils.drawRoundedRect(x, y, width, height, CORNER_RADIUS, Colors.BackGround.color);
        
        /* CategoryButtons */
        drawCategoryButtons(mouseX, mouseY, partialTicks);
        
        /* Search */
        processAndRenderSearch(mouseX, mouseY);
        
        /* ModuleButtons */
        updateModuleButtonsScrolls();
        drawModuleButtons(mouseX, mouseY, partialTicks);
        
        /* Draw currentValuePanel */
        RenderUtils.startGlStencil(() -> RenderUtils.drawRoundedRect(x, y, width, height, CORNER_RADIUS, Colors.BackGround.color));
        if (currentValuePanel != null) {
            currentValuePanel.setX(x + width - (valuePanelWidth * currentValuePanel.animation.getValuef()));
            currentValuePanel.setY(y);
            currentValuePanel.setWidth(valuePanelWidth);
            currentValuePanel.setHeight(height);
            
            RenderUtils.drawRoundedRect(x, y, width, height, CORNER_RADIUS, ColorUtils.colorWithAlpha(Colors.BackGround.color, (int) (80 * currentValuePanel.animation.getValuef())));
            
            currentValuePanel.drawScreen(mouseX, mouseY, partialTicks);
            
            if (!currentValuePanel.out && currentValuePanel.animation.isFinished()) currentValuePanel = null;
        }
        RenderUtils.stopGlStencil();
    }
    
    private void processAndRenderSearch(int mouseX, int mouseY) {
        final float buttonYOffset = 23F;
        hoveringSearchButton = RenderUtils.isHovering(mouseX, mouseY, x + 15, y + buttonYOffset, 98, 20);
        
        searchButtonAnimation.run(hoveringSearchButton || isSearching ? 7 : 0);
        //SearchButton
        FontManager.MaterialSymbolsSharp.of(30).drawString(MaterialIcons.get("search"), x + 17 + (searchButtonAnimation.getValuef() * 1.05F),
                y + buttonYOffset + 4, Colors.Text.color);
        FontManager.NotoSans.of(18).drawString("Search", x + 34 + searchButtonAnimation.getValuef(),
                y + buttonYOffset + 9.5F - FontManager.NotoSans.of(16).getHeight() / 3F, Colors.Text.color);
        
        //TextBox
        searchBoxInOutAnimation.run(isSearching ? 1 : -0.5);
        searchBox.xPosition = x + PADDING;
        searchBox.yPosition = (int) (y - (searchBoxInOutAnimation.getValuef() * 32F));
        searchBox.setBackGroundColor(Colors.BackGround.color.brighter());
        searchBox.setDrawRipple(true);
        
        if (searchBox.yPosition + 12 <= y) {
            RenderUtils.startGlStencil(() -> RenderUtils.drawRoundedRect(x, y, width, height, CORNER_RADIUS, new Color(255, 255, 255)), false);
            searchBox.drawTextBox();
            RenderUtils.stopGlStencil();
        }
        
        if (!isSearching) return;
        //filter moduleButtons
        List<ModuleButton> result = categoryButtons.stream()
                .flatMap(categoryButton -> categoryButton.moduleButtons.stream())
                .filter(moduleButton -> moduleButton.module.getName().toLowerCase()
                        .contains(searchBox.getText().toLowerCase()))
                .sorted((mbtn1, mbtn2) -> mbtn1.module.getName().compareToIgnoreCase(mbtn2.module.getName()))
                .collect(Collectors.toList());
        
        if (result.isEmpty()) {
            currentModuleButtons.clear();
            FontManager.NotoSans.of(20).drawCenteredString("No modules found", x + width / 2F + 60, y + height / 2F - 20, Colors.Text.color);
            return;
        }
        
        currentModuleButtons = result;
    }
    
    private void drawModuleButtons(int mouseX, int mouseY, float partialTicks) {
        float baseXOffset = 10.5F;
        float panelStartX = x + CategoryButton.width + baseXOffset + PADDING * 3;
        float panelStartY = y + PADDING;
        
        float moduleButtonXOffset = 0.0F;
        float moduleButtonYOffset = scrollableModuleButtons.getValuef();
        int buttonCounts = 0;
        
        RenderUtils.startGlScissor((int) (panelStartX + PADDING),
                (int) panelStartY,
                (int) (width - (CategoryButton.width + baseXOffset) - PADDING * 4.5F),
                height - PADDING * 2
        );
        
        for (ModuleButton moduleButton : currentModuleButtons) {
            moduleButton.setPosition(panelStartX + PADDING + moduleButtonXOffset, panelStartY + moduleButtonYOffset);
            moduleButton.drawScreen(mouseX, mouseY, partialTicks);
            
            moduleButtonXOffset += moduleButton.width + PADDING + 0.5F;
            buttonCounts++;
            
            if (buttonCounts % 3 == 0) {
                buttonCounts = 0;
                moduleButtonXOffset = 0.0F;
                moduleButtonYOffset += moduleButton.height + PADDING;
            }
        }
        
        RenderUtils.stopGlScissor();
    }
    
    private void updateModuleButtonsScrolls() {
        //计算总高度，然后更新滚轮
        float buttonsHeight = 0.0F;
        int buttonsInRow = 0;
        float rowHeight = 0.0F;
        for (ModuleButton moduleButton : currentModuleButtons) {
            rowHeight = Math.max(rowHeight, moduleButton.height);
            if (++buttonsInRow == 3) {
                buttonsHeight += rowHeight + PADDING;
                buttonsInRow = 0;
                rowHeight = 0;
            }
        }
        if (buttonsInRow > 0) buttonsHeight += rowHeight;
        buttonsHeight = Math.max(0, buttonsHeight - PADDING - 140);
        
        if (currentValuePanel == null) {//确保当前没有ValuePanel避免冲突
            scrollableModuleButtons.setMax(buttonsHeight);
            scrollableModuleButtons.updateScroll();
        }
    }
    
    private void drawCategoryButtons(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRoundedRect(
                x + PADDING,
                y + PADDING,
                PADDING + CategoryButton.width + PADDING + 10,
                height - PADDING * 2,
                CORNER_RADIUS - 1,
                Colors.OnBackGround.color);//backgound
        
        float categoryButtonsYOffset = 0;
        int categoryButtonX = x + 14;
        int categoryButtonY = y + 50;
        for (CategoryButton categoryButton : categoryButtons) {
            categoryButton.setPosition(categoryButtonX, categoryButtonY + categoryButtonsYOffset);
            categoryButton.drawScreen(mouseX, mouseY, partialTicks);
            categoryButtonsYOffset += categoryButton.getHeight() + 8;
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        //是否点击搜索框区域
        boolean clickedSearchBox = RenderUtils.isHovering(
                mouseX, mouseY,
                searchBox.xPosition, searchBox.yPosition,
                searchBox.getWidth(), searchBox.getHeight());
        
        //是否点击了搜索按钮
        boolean clickedSearchButton = hoveringSearchButton && mouseButton == 0;
        
        if (clickedSearchBox) searchBox.setFocused(true);
        
        //判断是否点击了某个CategoryButton
        for (CategoryButton categoryButton : categoryButtons) {
            if (RenderUtils.isHovering(mouseX, mouseY, categoryButton.getX(), categoryButton.getY(), categoryButton.getWidth(), categoryButton.getHeight())) {
                //关闭搜索状态，清理
                isSearching = false;
                searchBox.setText("");
                currentCategoryButton = categoryButton;
                currentModuleButtons = currentCategoryButton.moduleButtons;
                prevCategoryButton = null;
                scrollableModuleButtons.setTarget(0);
                scrollableModuleButtons.setValue(0);
                break;
            }
        }
        
        //如果点击了搜索框或搜索按钮就开启搜索状态
        if (mouseButton == 0) {
            if (clickedSearchBox || clickedSearchButton) {
                isSearching = true;
                //取消当前Category，保存之前的Category
                if (currentCategoryButton != null) {
                    prevCategoryButton = currentCategoryButton;
                    currentCategoryButton = null;
                }
            } else if (RenderUtils.isHovering(mouseX, mouseY, x, y, width, height)) {
                //如果点击了ClickGui内部但是不是搜索框，则关闭搜索状态（除了搜索框已聚焦）
                if (!searchBox.isFocused() && currentValuePanel == null) {
                    isSearching = false;
                }
            } else {
                //点击ClickGui外部时关闭搜索状态，并恢复之前的Category
                isSearching = false;
                if (currentCategoryButton == null && prevCategoryButton != null) {
                    currentCategoryButton = prevCategoryButton;
                    prevCategoryButton = null;
                    
                    currentModuleButtons = currentCategoryButton.moduleButtons;
                }
            }
        }
        
        //再次处理搜索状态与分类按钮
        if (isSearching) {
            if (currentCategoryButton != null) {
                prevCategoryButton = currentCategoryButton;
                currentCategoryButton = null;
            }
        } else {
            if (currentCategoryButton == null && prevCategoryButton != null) {
                currentCategoryButton = prevCategoryButton;
                prevCategoryButton = null;
                
                currentModuleButtons = currentCategoryButton.moduleButtons;
            }
        }
        
        if (currentValuePanel != null) {
            currentValuePanel.mouseClicked(mouseX, mouseY, mouseButton);
            //点击ValuePanel外部但在ClickGui内部，退出ValuePanel
            if (!RenderUtils.isHovering(mouseX, mouseY, x + width - valuePanelWidth, y, valuePanelWidth, height) //不在ValuePanel上
                    && !RenderUtils.isHovering(mouseX, mouseY, x, y, width, height / 10F) //不在拖动区域
                    && RenderUtils.isHovering(mouseX, mouseY, x, y, width, height)) //在ClickGui内
                currentValuePanel.out = false;
            return;
        }
        
        super.mouseClicked(mouseX, mouseY, mouseButton);
        categoryButtons.forEach(categoryButton -> categoryButton.mouseClicked(mouseX, mouseY, mouseButton));
        currentModuleButtons.forEach(moduleButton -> {
            if (moduleButton.mouseClicked2(mouseX, mouseY, mouseButton)) {
                searchBox.setFocused(false);
            }
        });
        
    }
    
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (currentValuePanel != null) currentValuePanel.mouseReleased(mouseX, mouseY, state);
        
        Client.INSTANCE.getFileManager().get(ModuleJsonConfig.class).write();
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        searchBox.textboxKeyTyped(typedChar, keyCode);
        currentModuleButtons.forEach(moduleButton -> moduleButton.keyTyped(typedChar, keyCode));
        
        if (keyCode == Keyboard.KEY_TAB) {
            searchBox.setFocused(!searchBox.isFocused());
        }
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    public void setValuePanel(ValuePanel currentValuePanel) {
        if (currentValuePanel == null) {
            this.currentValuePanel.out = false;
            return;
        }
        this.currentValuePanel = currentValuePanel;
        this.currentValuePanel.out = true;
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (currentValuePanel != null) currentValuePanel.onGuiClosed();
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
        searchBox.updateCursorCounter();
        if (currentValuePanel != null) currentValuePanel.updateScreen();
    }
    
    @Override
    public Draggable getDraggable() {
        if (draggable == null) {
            draggable = new Draggable(100, 100, 1, null);
        }
        return draggable;
    }
}
