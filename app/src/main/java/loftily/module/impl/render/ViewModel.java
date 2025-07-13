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
    private final NumberValue itemScale = new NumberValue("ItemScale", 1, 0.1, 1.5, 0.01);
    private final NumberValue itemPosXOffset = new NumberValue("ItemPosXOffset", 0, -1, 1, 0.01);
    private final NumberValue itemPosYOffset = new NumberValue("ItemPosYOffset", 0, -1, 1, 0.01);
    private final NumberValue itemPosZOffset = new NumberValue("ItemPosZOffset", 0, -1, 1, 0.01);
    
    public static ViewModel getInstance() {
        return Client.INSTANCE.getModuleManager().get(ViewModel.class);
    }
}
