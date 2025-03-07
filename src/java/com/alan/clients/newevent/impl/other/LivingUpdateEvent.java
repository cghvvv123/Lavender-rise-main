package com.alan.clients.newevent.impl.other;

import com.alan.clients.newevent.Event;
import net.minecraft.entity.Entity;

public class LivingUpdateEvent implements Event {
    private final Entity entity;

    public LivingUpdateEvent(Entity entity) {
        this.entity = entity;

    }

    public Entity getEntity() {
        return this.entity;
    }
}
