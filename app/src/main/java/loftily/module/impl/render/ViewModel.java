package loftily.module.impl.render;

import loftily.Client;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.NumberValue;
import lombok.Getter;

@Getter
@ModuleInfo(name = "ViewModel", category = ModuleCategory.RENDER)
public class ViewModel extends Module {
    private final NumberValue itemMainHandScale = new NumberValue("MainHandItemScale", 1, 0.1, 1.5, 0.01);
    private final NumberValue itemMainHandPosXOffset = new NumberValue("MainHandItemPosXOffset", 0, -1, 1, 0.01);
    private final NumberValue itemMainHandPosYOffset = new NumberValue("MainHandItemPosYOffset", 0, -1, 1, 0.01);
    private final NumberValue itemMainHandPosZOffset = new NumberValue("MainHandItemPosZOffset", 0, -1, 1, 0.01);
    
    private final NumberValue itemOffHandScale = new NumberValue("OffHandItemScale", 1, 0.1, 1.5, 0.01);
    private final NumberValue itemOffHandPosXOffset = new NumberValue("OffHandItemPosXOffset", 0, -1, 1, 0.01);
    private final NumberValue itemOffHandPosYOffset = new NumberValue("OffHandItemPosYOffset", 0, -1, 1, 0.01);
    private final NumberValue itemOffHandPosZOffset = new NumberValue("OffHandItemPosZOffset", 0, -1, 1, 0.01);
    
    public static ViewModel getInstance() {
        return Client.INSTANCE.getModuleManager().get(ViewModel.class);
    }
}
