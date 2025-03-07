package com.alan.clients.module.impl.movement.flight;

import com.alan.clients.module.impl.movement.Flight;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PostMotionEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.value.Mode;

/**
 * @author Alan
 * @since 03.07.2022
 */
public class GrimFlight extends Mode<Flight> {
    private boolean canfly = false;

    public GrimFlight(String name, Flight parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        canfly = false;
    }

    @Override
    public void onDisable() {
        canfly = false;
    }

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (!isNull() && !mc.thePlayer.onGround) {
            canfly = true;
        }
    };
    @EventLink
    private final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (canfly) {
            mc.thePlayer.setPositionAndRotation(
                    mc.thePlayer.posX + 1000,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch
            );
        }
    };
    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        if (canfly) {
            mc.thePlayer.setPositionAndRotation(
                    mc.thePlayer.posX - 1000,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch
            );
        }
    };
}
