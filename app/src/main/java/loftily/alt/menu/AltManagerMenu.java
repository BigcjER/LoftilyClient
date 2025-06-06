package loftily.alt.menu;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import loftily.Client;
import loftily.alt.Alt;
import loftily.alt.AltType;
import loftily.config.FileManager;
import loftily.gui.animation.Ripple;
import loftily.gui.components.CustomButton;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.gui.interaction.Scrollable;
import loftily.utils.client.ClientUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.render.ColorUtils;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.utils.timer.DelayTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AltManagerMenu extends GuiScreen {
    //Button
    private static final ExecutorService SkinDownloadExecutor = Executors.newCachedThreadPool();
    private static final int ButtonWidth = 180, ButtonHeight = 40, Spacing = 10;
    public static final Color BUTTON_COLOR = Colors.OnBackGround.color.brighter();
    private GuiButton removeButton, loginButton, randomAltButton;
    private AltButton focusedButton;
    //Async
    private static final Set<String> downloadingSkins = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, ResourceLocation> skinCache = new HashMap<>();
    //Text
    private final DelayTimer timer = new DelayTimer();
    private String currentText = "";
    //other
    private final List<Alt> alts = Client.INSTANCE.getAltManager().getAlts();
    private final Scrollable scrollable = new Scrollable(8);
    
    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        
        int buttonsPerRow = Math.max(1, (width - 115 - Spacing) / (ButtonWidth + Spacing));
        
        for (int i = 0; i < alts.size(); i++) {
            Alt alt = alts.get(i);
            this.buttonList.add(new AltButton(i, 0, 0, ButtonWidth, ButtonHeight, alt));
        }
        
        int btnY = 10;
        int size = alts.size();
        this.buttonList.add(new CustomButton(size, 10, btnY, 80, 20, "Add", BUTTON_COLOR));
        this.buttonList.add(this.loginButton = new CustomButton(size + 1, 10, btnY + 25, 80, 20, "Login", BUTTON_COLOR));
        this.buttonList.add(this.removeButton = new CustomButton(size + 2, 10, btnY + 50, 80, 20, "Remove", BUTTON_COLOR));
        this.buttonList.add(this.randomAltButton = new CustomButton(size + 3, 10, btnY + 75, 80, 20, "RandomAlt", BUTTON_COLOR));
        this.buttonList.add(new CustomButton(size + 4, 10, btnY + 100, 80, 20, "RandomOffline", BUTTON_COLOR));
        
        removeButton.enabled = false;
        loginButton.enabled = false;
        randomAltButton.enabled = !alts.isEmpty();
        
        int rows = (alts.size() + buttonsPerRow - 1) / buttonsPerRow;
        int height = rows * (ButtonHeight + Spacing);
        scrollable.setMax(Math.max(0, height - (this.height - 20)));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRectHW(0, 0, width, height, Colors.BackGround.color);
        RenderUtils.drawRectHW(100, 0, width - 100, height, Colors.BackGround.color.brighter());
        
        scrollable.updateScroll();
        
        int buttonsPerRow = Math.max(1, (width - 110 - Spacing) / (ButtonWidth + Spacing));
        int i = 0;
        
        for (GuiButton button : buttonList) {
            if (button instanceof AltButton) {
                Point pos = getGridPosition(i, buttonsPerRow, 115, (int) scrollable.getValuef() + 10, ButtonWidth, ButtonHeight, Spacing);
                button.xPosition = pos.x;
                button.yPosition = pos.y;
                button.drawScreen(mc, mouseX, mouseY, partialTicks);
                drawPlayerHead(pos.x + 10, pos.y + 7, ((AltButton) button).alt);
                i++;
            } else {
                button.drawScreen(mc, mouseX, mouseY, partialTicks);
            }
        }
        FontRenderer font = FontManager.NotoSans.of(16);
        
        if (!timer.hasTimeElapsed(3000)) {
            font.drawString(currentText, 10, height - 30, Colors.Text.color);
        }
        
        String visibleText = font.trimStringToWidth(
                "Current: " + Client.INSTANCE.getAltManager().getCurrentAlt().getName(), 65, false);
        if (font.getWidth(visibleText) >= 65) visibleText = visibleText + "...";
        font.drawString(visibleText, 10, height - 15, Colors.Text.color);
        
    }
    
    private void drawPlayerHead(int x, int y, Alt alt) {
        try {
            ResourceLocation resourceLocation;
            if (alt.getType() == AltType.Microsoft) {
                resourceLocation = getSkinForAlt(alt);
            } else {
                resourceLocation = DefaultPlayerSkin.getDefaultSkin(new UUID(0L, 0L));
            }
            
            int size = 25;
            if (resourceLocation != null) {
                GL11.glColor4f(1, 1, 1, 1);
                mc.getTextureManager().bindTexture(resourceLocation);
                //头部主层
                drawScaledCustomSizeModalRect(x, y, 8.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
                //头部第二层
                drawScaledCustomSizeModalRect(x, y, 40.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
                GlStateManager.resetColor();
            } else if (alt.getType() == AltType.Microsoft)
                RenderUtils.drawRectHW(x, y, size, size, Colors.BackGround.color.brighter().brighter());//fallback
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private ResourceLocation getSkinForAlt(Alt alt) {
        final String uuidString = alt.getUuid();
        //格式化UUID
        UUID playerUUID;
        try {
            if (uuidString.length() == 32 && !uuidString.contains("-")) {
                playerUUID = UUID.fromString(
                        uuidString.substring(0, 8) + "-" +
                                uuidString.substring(8, 12) + "-" +
                                uuidString.substring(12, 16) + "-" +
                                uuidString.substring(16, 20) + "-" +
                                uuidString.substring(20, 32)
                );
            } else {
                playerUUID = UUID.fromString(uuidString);
            }
        } catch (IllegalArgumentException e) {
            ClientUtils.LOGGER.error("Invalid UUID for alt {} ({}): {}", alt.getName(), uuidString, e.getMessage());
            return null;
        }
        
        File cacheFile = new File(FileManager.SkinsCacheDir, playerUUID + ".png");
        //如果有缓存直接返回
        if (skinCache.containsKey(uuidString)) return skinCache.get(uuidString);
        //如果在下载直接返回
        if (downloadingSkins.contains(uuidString)) return null;
        
        downloadingSkins.add(uuidString); //标记为正在下载
        SkinDownloadExecutor.submit(() -> {
            GameProfile profile = new GameProfile(playerUUID, alt.getName());
            
            try {
                mc.getSessionService().fillProfileProperties(profile, true);
                
                if (profile.getProperties().containsKey("textures")) {
                    MinecraftProfileTexture skinTexture = mc.getSessionService().getTextures(profile, false).get(MinecraftProfileTexture.Type.SKIN);
                    
                    ResourceLocation location = new ResourceLocation("loftily", "skins/" + playerUUID.toString().toLowerCase());
                    ResourceLocation defaultSkin = DefaultPlayerSkin.getDefaultSkin(profile.getId());
                    
                    mc.addScheduledTask(() -> {//加载放到主线程
                        try {
                            mc.getTextureManager().loadTexture(location, new ThreadDownloadImageData(
                                    cacheFile,
                                    skinTexture.getUrl(),
                                    defaultSkin,
                                    new ImageBufferDownload()
                            ));
                            
                            skinCache.put(uuidString, location);
                        } catch (Exception e) {
                            throw new RuntimeException("Error loading texture: " + e.getMessage());
                        } finally {
                            downloadingSkins.remove(uuidString);
                        }
                    });
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to fill profile properties:" + e.getMessage());
            } finally {
                downloadingSkins.remove(uuidString);
            }
        });
        
        return null;
    }
    
    
    private Point getGridPosition(int index, int buttonsPerRow, int startX, int startY, int buttonWidth, int buttonHeight, int spacing) {
        int row = index / buttonsPerRow;
        int col = index % buttonsPerRow;
        int x = startX + col * (buttonWidth + spacing);
        int y = startY + row * (buttonHeight + spacing);
        return new Point(x, y);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        randomAltButton.enabled = !alts.isEmpty();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button instanceof AltButton) {
            //设置所有button的focused为false
            for (GuiButton guiButton : buttonList) {
                if (guiButton instanceof AltButton) {
                    ((AltButton) guiButton).focused = false;
                }
            }
            
            AltButton clickedButton = (AltButton) button;
            clickedButton.focused = true;
            focusedButton = clickedButton;
            removeButton.enabled = true;
            loginButton.enabled = true;
            
            //双击登录
            if (clickedButton.doubleClick) {
                Alt account = alts.get(clickedButton.id);
                Client.INSTANCE.getAltManager().login(account, text -> this.currentText = text);
                clickedButton.doubleClick = false;
                timer.reset();
            }
            return;
        }
        System.out.println(button.id - alts.size());
        switch (button.id - alts.size()) {
            case 0:
                mc.displayGuiScreen(new AltSelectTypeMenu(this));
                break;
            case 1:
                if (focusedButton != null) {
                    Alt accountToLogin = alts.get(focusedButton.id);
                    Client.INSTANCE.getAltManager().login(accountToLogin);
                }
                break;
            case 2:
                if (focusedButton != null) {
                    Alt accountToRemove = alts.get(focusedButton.id);
                    Client.INSTANCE.getAltManager().remove(accountToRemove);
                    focusedButton = null;
                    initGui();
                }
                break;
            case 3:
                if (!alts.isEmpty()) {
                    Alt account = alts.get(RandomUtils.randomInt(0, alts.size()));
                    Client.INSTANCE.getAltManager().login(account);
                }
                break;
            case 4:
                Client.INSTANCE.getAltManager().login(new Alt(RandomUtils.randomString(8)));
                break;
        }
    }
    
    static class AltButton extends GuiButton {
        public boolean focused, doubleClick;
        private final Alt alt;
        private final Ripple ripple;
        
        public AltButton(int buttonId, int x, int y, int widthIn, int heightIn, Alt alt) {
            super(buttonId, x, y, widthIn, heightIn, alt.getName());
            this.focused = false;
            this.doubleClick = false;
            this.alt = alt;
            this.ripple = new Ripple();
        }
        
        @Override
        public void drawScreen(Minecraft mc, int mouseX, int mouseY, float p_191745_4_) {
            if (!this.visible) return;
            this.hovered = RenderUtils.isHovering(mouseX, mouseY, xPosition, yPosition, width, height);
            Runnable backGroundRunnable = () -> RenderUtils.drawRoundedRect(
                    this.xPosition, this.yPosition, width, height, 2,
                    hovered ? ColorUtils.colorWithAlpha(Colors.Text.color, 20) : Colors.OnBackGround.color);
            
            backGroundRunnable.run();
            
            RenderUtils.startGlStencil(backGroundRunnable);
            ripple.draw();
            RenderUtils.stopGlStencil();
            
            
            if (focused) {
                RenderUtils.drawRoundedRectOutline(this.xPosition - 1, this.yPosition - 1, width + 2, height + 2,
                        2, 0.5f, new Color(255, 255, 255, 0),
                        Colors.Active.color);
            }
            
            
            Color color = Colors.Text.color;
            
            if (hovered)
                color = new Color(16777120);
            
            FontManager.NotoSans.of(16).drawCenteredString(this.displayString,
                    this.xPosition + (float) this.width / 2, this.yPosition + (float) (this.height - 8) / 2 - 7, color);
            FontManager.NotoSans.of(18).drawCenteredString(alt.getType().name(),
                    this.xPosition + (float) this.width / 2, this.yPosition + (float) (this.height - 8) / 2 + 6, Colors.Text.color.darker().darker());
            
        }
        
        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (hovered)
                ripple.add(mouseX, mouseY,
                        250, 500, 60,
                        Colors.OnBackGround.color.brighter().brighter().brighter());
            if (hovered && focused) {
                doubleClick = true;
            }
            return super.mousePressed(mc, mouseX, mouseY);
        }
    }
}