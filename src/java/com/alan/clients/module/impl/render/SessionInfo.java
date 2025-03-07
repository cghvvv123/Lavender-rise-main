package com.alan.clients.module.impl.render;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.render.sessionInfo.SessionStats;
import com.alan.clients.module.impl.render.sessionInfo.Sessioninfo1;
import com.alan.clients.module.impl.render.sessionInfo.Sessioninfo2;
import com.alan.clients.module.impl.render.sessionInfo.Sessioninfo3;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.value.impl.DragValue;
import com.alan.clients.value.impl.ModeValue;
import net.minecraft.client.gui.GuiChat;

@ModuleInfo(name = "module.render.sessionstats.name",category = Category.RENDER,description = "module.render.sessioninfo.description")
public class SessionInfo extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new Sessioninfo1("Sessioninfo1", this))
            .add(new Sessioninfo2("Sessioninfo2", this))
            .add(new Sessioninfo3("Sessioninfo3", this))
            .add(new SessionStats("SessionStats", this))
            .setDefault("Sessioninfo1");
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
