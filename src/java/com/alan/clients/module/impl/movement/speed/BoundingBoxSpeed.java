package com.alan.clients.module.impl.movement.speed;

import com.alan.clients.module.impl.movement.Speed;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class BoundingBoxSpeed extends Mode<Speed> {

    private BooleanValue hurtCancel; 

    public BoundingBoxSpeed(String name, Speed parent) {
        super(name, parent);
        hurtCancel = new BooleanValue("Hurt Cancel", this, true);
    }

    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (isNull()) return;
        if (isPlayerValid() && mc.thePlayer.hurtTime > 0 && hurtCancel.getValue()) {
            return;
        }
        for (Entity en : mc.theWorld.loadedEntityList) {
            if (isValidEntity(en)) {
                applySpeedEffect();
                break;
            }
        }
    };

    private boolean isPlayerValid() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    private boolean isValidEntity(Entity entity) {
        return entity instanceof EntityLivingBase && entity.getEntityId() != mc.thePlayer.getEntityId() &&
                entity.getDistanceSqToEntity(mc.thePlayer) <= 3f;
    }

    private void applySpeedEffect() {
        mc.thePlayer.motionX *= 1.1;
        mc.thePlayer.motionZ *= 1.1;
    }
}
