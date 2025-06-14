package loftily.module.impl.render;

import loftily.event.impl.render.Render3DEvent;
import loftily.handlers.impl.client.TargetsHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.render.ESPUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import lombok.NonNull;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;

@ModuleInfo(name = "ESP", category = ModuleCategory.RENDER, defaultToggled = true)
public class ESP extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Box", this,
            new StringMode("Box"));
    
    private final BooleanValue positionalInterpolation = new BooleanValue("PositionalInterpolation", true);
    
    private final NumberValue colorRed = new NumberValue("ColorRed", 27, 0, 255);
    private final NumberValue colorGreen = new NumberValue("ColorRed", 27, 0, 255);
    private final NumberValue colorBlue = new NumberValue("ColorRed", 27, 0, 255);
    private final NumberValue colorAlpha = new NumberValue("ColorAlpha", 80, 0, 255);
    
    private final BooleanValue anotherColorOnDamage = new BooleanValue("AnotherColorOnDamage", true);
    private final NumberValue damageColorRed = new NumberValue("DamageColorRed", 80, 0, 255).setVisible(anotherColorOnDamage::getValue);
    private final NumberValue damageColorGreen = new NumberValue("DamageColorRed", 22, 0, 255).setVisible(anotherColorOnDamage::getValue);
    private final NumberValue damageColorBlue = new NumberValue("DamageColorRed", 22, 0, 255).setVisible(anotherColorOnDamage::getValue);
    private final NumberValue damageColorAlpha = new NumberValue("DamageColorAlpha", 80, 0, 255).setVisible(anotherColorOnDamage::getValue);
    
    @EventHandler
    public void onRender3DEvent(Render3DEvent event) {
        
        for (EntityLivingBase entity : TargetsHandler.getTargets(Double.MAX_VALUE)) {
            if (entity == null || !ESPUtils.isInView(entity)) continue;
            
            Color color = new Color(
                    colorRed.getValue().intValue(),
                    colorGreen.getValue().intValue(),
                    colorBlue.getValue().intValue(),
                    colorAlpha.getValue().intValue());
            
            if (anotherColorOnDamage.getValue() && entity.hurtTime > 0) {
                color = new Color(
                        damageColorRed.getValue().intValue(),
                        damageColorGreen.getValue().intValue(),
                        damageColorBlue.getValue().intValue(),
                        damageColorAlpha.getValue().intValue());
            }
            
            switch (mode.getValueByName().toLowerCase()) {
                case "box":
                    ESPUtils.drawEntityBox(entity, color, positionalInterpolation.getValue());
                    break;
            }
        }
        
    }
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}