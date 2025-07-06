package loftily.module.impl.render;

import loftily.Client;
import loftily.config.impl.json.DragsJsonConfig;
import loftily.event.impl.player.AttackEvent;
import loftily.event.impl.render.Render2DEvent;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.font.FontManager;
import loftily.gui.interaction.draggable.Draggable;
import loftily.gui.interaction.draggable.IDraggable;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.MathUtils;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;

@ModuleInfo(name = "TargetHUD", category = ModuleCategory.RENDER, defaultToggled = true)
public class TargetHUD extends Module implements IDraggable {
    //target
    private final DelayTimer delayTimer = new DelayTimer();
    private EntityLivingBase target = null;
    private boolean inWorld;
    //pos
    private final int WIDTH = 125, HEIGHT = 40;
    private Draggable draggable;
    //animation
    private final Animation healthArcAnimation = new Animation(Easing.EaseOutCirc, 500);
    private final Animation inOutAnimation = new Animation(Easing.EaseOutExpo, 300);
    //value
    private final ModeValue animationMode = new ModeValue("AnimationMode", "SlideIn", this,
            new StringMode("SlideIn"),
            new StringMode("ScaleIn"));
    
    private final NumberValue animationDuring = new NumberValue("OpeningAnimationDuring", 300, 100, 1000);
    
    private final ModeValue slideDirection = new ModeValue("SlideDirection", "Down", this,
            new StringMode("Up"),
            new StringMode("Down"),
            new StringMode("Left"),
            new StringMode("Right"))
            .setVisible(() -> animationMode.getValueByName().equalsIgnoreCase("SlideIn"));
    
    private final BooleanValue clip = new BooleanValue("Clip", true);
    
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        final int screenWidth = event.getScaledResolution().getScaledWidth();
        final int screenHeight = event.getScaledResolution().getScaledHeight();
        
        if (draggable == null) {
            draggable = new Draggable(screenWidth / 2 + 10, screenHeight / 2 + 10, event.getScaledResolution(), 1);
            Client.INSTANCE.getFileManager().get(DragsJsonConfig.class).read();
        }
        
        
        //更低优先级的target
        if (delayTimer.hasTimeElapsed(1000)) {
            if (mc.currentScreen instanceof GuiChat) {
                target = mc.player;
                delayTimer.reset();
            }
        }
        
        if (target == null) return;
        
        //更新动画
        boolean in = (!inWorld || delayTimer.hasTimeElapsed(1000));
        inWorld = mc.world.loadedEntityList.contains(target);
        inOutAnimation.run(in ? 0 : 1);
        inOutAnimation.setEasing(in ? Easing.EaseInExpo : Easing.EaseOutExpo);
        inOutAnimation.setDuration(animationDuring.getValue().longValue());
        if (inOutAnimation.getValue() <= 0) return;
        
        final int x = getDraggable().getPosX(screenWidth);
        final int y = getDraggable().getPosY(screenHeight);
        
        double health = target.getHealth() + target.getAbsorptionAmount();
        double maxHealth = target.getMaxHealth() + target.getAbsorptionAmount();
        double roundedHealth = MathUtils.round(health, 1);
        double roundedMaxHealth = MathUtils.round(maxHealth, 1);
        double myHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        String name = target.getName();
        String healthText = String.format("%s/%s", roundedHealth, roundedMaxHealth);
        int width = Math.max(WIDTH,
                Math.max(FontManager.NotoSans.of(18).getWidth(name), FontManager.NotoSans.of(16).getWidth(healthText))
                        + 80);
        
        Runnable backGround = () -> RenderUtils.drawRoundedRect(x, y, width, HEIGHT, 3, Colors.BackGround.color);
        
        //更新Draggable
        Point mouse = RenderUtils.getMouse(event.getScaledResolution());
        getDraggable().updateDrag(
                mouse.x,
                mouse.y,
                width,
                HEIGHT,
                event.getScaledResolution().getScaledWidth(),
                event.getScaledResolution().getScaledHeight());
        
        getDraggable().applyDragEffect(() -> {
            //计算Animation
            switch (animationMode.getValueByName()) {
                case "SlideIn": {
                    if (clip.getValue()) {
                        RenderUtils.startGlStencil(backGround);
                    }
                    double translateX = 0;
                    double translateY = 0;
                    switch (slideDirection.getValueByName()) {
                        case "Up":
                            translateY = -(y + (double) HEIGHT / 2) * (1 - inOutAnimation.getValue());
                            break;
                        case "Down":
                            translateY = (event.getScaledResolution().getScaledHeight() - y) * (1 - inOutAnimation.getValuef());
                            break;
                        case "Left":
                            translateX = -(x + (double) width / 2) * (1 - inOutAnimation.getValue());
                            break;
                        case "Right":
                            translateX = (event.getScaledResolution().getScaledWidth() - x) * (1 - inOutAnimation.getValuef());
                            break;
                    }
                    GlStateManager.translate(translateX, translateY, 0);
                    break;
                }
                case "ScaleIn": {
                    if (!clip.getValue()) {
                        GlStateManager.translate((x + (double) width / 2) * (1 - inOutAnimation.getValue()), (y + (double) HEIGHT / 2) * (1 - inOutAnimation.getValue()), 0);
                        GlStateManager.scale(inOutAnimation.getValue(), inOutAnimation.getValue(), 0);
                    } else {
                        inOutAnimation.setEasing(in ? Easing.EaseInCubic : Easing.EaseOutCubic);//更换一个缓动系数小的Ease让动画看起来更好
                        RenderUtils.startGlStencil(() -> RenderUtils.drawRoundedRect(
                                x + (width / 2F * (1 - inOutAnimation.getValuef())),
                                y + (HEIGHT / 2F * (1 - inOutAnimation.getValuef())),
                                width * inOutAnimation.getValuef(),
                                HEIGHT * inOutAnimation.getValuef(), 3, Colors.BackGround.color));
                    }
                    break;
                }
            }
            
            
            /* 绘制 */
            backGround.run();
            
            //Head
            int headSize = 30;
            RenderUtils.drawPlayerHead(x, y, headSize, target);
            
            //Text
            FontManager.NotoSans.of(18).drawString(name, x + headSize + 11, y + 5, Colors.Text.color);
            
            FontManager.NotoSans.of(16).drawString(healthText, x + headSize + 11, y + 18, Colors.Text.color.darker());
            if (health != myHealth) {
                String text = health <= myHealth ? "Winning" : "Losing";
                
                FontManager.NotoSans.of(16).drawString(text, x + headSize + 11, y + 27, Colors.Text.color.darker());
            }
            
            
            //health arc
            float startAngle = (float) -90;
            float endAngle = (float) (startAngle + 360F * (health / maxHealth));
            healthArcAnimation.run(endAngle);
            RenderUtils.drawArc(
                    getDraggable().getPosX(event.getScaledResolution().getScaledWidth()) + width - 20,
                    getDraggable().getPosY(event.getScaledResolution().getScaledHeight()) + HEIGHT / 2f,
                    headSize / 2F - 3,
                    startAngle,
                    healthArcAnimation.getValuef(),
                    3.0f, Colors.Active.color);
        }, 3, screenWidth, screenHeight);
        if (clip.getValue()) RenderUtils.stopGlStencil();
    }
    
    @EventHandler
    public void onAttack(AttackEvent event) {
        if (event.getTarget() instanceof EntityLivingBase) {
            target = (EntityLivingBase) event.getTarget();
        }
        delayTimer.reset();
    }
    
    @Override
    public Draggable getDraggable() {
        return draggable;
    }
    
    @Override
    public String getName() {
        return "TargetHUD";
    }
}