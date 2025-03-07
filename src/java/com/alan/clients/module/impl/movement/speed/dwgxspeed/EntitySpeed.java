package com.alan.clients.module.impl.movement.speed.dwgxspeed;

import com.alan.clients.module.impl.movement.Speed;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.Random;

public class EntitySpeed extends Mode<Speed> {

    private final BooleanValue hurtCancel;
    private final Random random;

    public EntitySpeed(String name, Speed parent) {
        super(name, parent);
        hurtCancel = new BooleanValue("Hurt Cancel", this, true);
        random = new Random();
    }

    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (!isPlayerValid()) return;

        boolean shouldCancel = shouldCancelHurt();

        if (!shouldCancel) {
            for (Entity entity : InstanceAccess.mc.theWorld.loadedEntityList) {
                if (isValidEntity(entity)) {
                    applySpeedEffect();
                    break;
                }
            }
        }
    };

    private boolean shouldCancelHurt() {
        return InstanceAccess.mc.thePlayer.hurtTime > 0 && hurtCancel.getValue();
    }

    private boolean isPlayerValid() {
        return InstanceAccess.mc.thePlayer != null && InstanceAccess.mc.theWorld != null;
    }

    private boolean isValidEntity(Entity entity) {
        return entity instanceof EntityLivingBase
                && entity.getEntityId() != InstanceAccess.mc.thePlayer.getEntityId()
                && entity.getDistanceSqToEntity(InstanceAccess.mc.thePlayer) <= 4.0;
    }

    private void applySpeedEffect() {
        // 随机增加速度
        double multiplier = 1.0 + random.nextDouble() * 0.15; //燃动 1-1.15
        InstanceAccess.mc.thePlayer.motionX *= multiplier;
        InstanceAccess.mc.thePlayer.motionZ *= multiplier;
    }
}
