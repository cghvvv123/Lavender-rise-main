package com.alan.clients.module.impl.movement.speed;

import com.alan.clients.module.impl.movement.Speed;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.Mode;

/**
 * @author Alan
 * @since 18/11/2022
 */

public class KoksCraftSpeed extends Mode<Speed> {

    int jumps;

    public KoksCraftSpeed(String name, Speed parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        jumps = 0;
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (InstanceAccess.mc.thePlayer.onGround) {
            if (InstanceAccess.mc.thePlayer.hurtTime == 0) MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() * 0.99);

            InstanceAccess.mc.thePlayer.jump();

            jumps++;
        }

        if (InstanceAccess.mc.thePlayer.offGroundTicks == 1 && InstanceAccess.mc.thePlayer.hurtTime == 0) {
            InstanceAccess.mc.thePlayer.motionY = MoveUtil.predictedMotion(InstanceAccess.mc.thePlayer.motionY, jumps % 2 == 0 ? 2 : 4);
        }
    };

}
