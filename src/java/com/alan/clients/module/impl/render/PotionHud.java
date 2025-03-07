package com.alan.clients.module.impl.render;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.render.potionhud.potionhud1;
import com.alan.clients.module.impl.render.potionhud.potionhud2;
import com.alan.clients.module.impl.render.potionhud.potionhud3;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.value.impl.DragValue;
import com.alan.clients.value.impl.ModeValue;
import net.minecraft.client.gui.GuiChat;

@ModuleInfo(name = "module.render.potionhud.name",category = Category.RENDER,description = "PotionHUD")
public class PotionHud extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new potionhud1("PotionHud", this))
            .add(new potionhud2("PotionHud2", this))
            .add(new potionhud3("PotionHud3", this))
            .setDefault("PotionHud");

    public  final DragValue positionValue = new DragValue("Position", this, new Vector2d(200, 200));

    public Vector2d position = new Vector2d(0, 0);
    public boolean rendertitle() {
        return mc.currentScreen instanceof GuiChat;
    }
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        this.position = positionValue.position;
    };
}
