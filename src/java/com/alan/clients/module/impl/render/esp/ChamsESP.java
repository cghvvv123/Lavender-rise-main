package com.alan.clients.module.impl.render.esp;

import com.alan.clients.component.impl.render.ESPComponent;
import com.alan.clients.component.impl.render.espcomponent.api.ESPColor;
import com.alan.clients.component.impl.render.espcomponent.impl.PlayerChams;
import com.alan.clients.module.impl.render.ESP;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.value.Mode;
import net.minecraft.entity.Entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class ChamsESP extends Mode<ESP> {

    public ChamsESP(String name, ESP parent) {
        super(name, parent);
    }
    public static List<Entity> flaggedEntity = new ArrayList<Entity>();
    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        flaggedEntity.clear();
    };
    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        Color color = getTheme().getAccentColor();
                    ESPComponent.add(new PlayerChams(new ESPColor(color, color, color)));
    };
}
