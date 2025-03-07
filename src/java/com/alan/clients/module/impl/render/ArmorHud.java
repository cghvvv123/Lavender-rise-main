package com.alan.clients.module.impl.render;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.render.armorhud.armorhud1;
import com.alan.clients.module.impl.render.armorhud.armorhud2;
import com.alan.clients.module.impl.render.armorhud.armorhud3;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.value.impl.DragValue;
import com.alan.clients.value.impl.ModeValue;
import net.minecraft.client.gui.GuiChat;

@ModuleInfo(name = "module.render.armorhud.name",category = Category.RENDER,description = "ArmorHud")
public class ArmorHud extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new armorhud1("ArmorHud", this))
            .add(new armorhud2("ArmorHud2", this))
            .add(new armorhud3("ArmorHud3", this))
            .setDefault("ArmorHud");

    public final DragValue positionValue = new DragValue("Position", this, new Vector2d(200, 200));

    public Vector2d position = new Vector2d(0, 0);
    public boolean rendertitle() {
        return mc.currentScreen instanceof GuiChat;
    }
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        this.position = positionValue.position;
    };
}
