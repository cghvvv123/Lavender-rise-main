package com.alan.clients.module.impl.movement.step;

import com.alan.clients.module.impl.movement.Step;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.Mode;
import net.minecraft.potion.Potion;

/**
 * @author Alan
 * @since 22/3/2022
 */

public class NCPPacketLessStep extends Mode<Step> {

    private boolean step;

    public NCPPacketLessStep(String name, Step parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        InstanceAccess.mc.thePlayer.stepHeight = 0.6F;
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (InstanceAccess.mc.thePlayer.onGround && InstanceAccess.mc.thePlayer.isCollidedHorizontally && !InstanceAccess.mc.thePlayer.isPotionActive(Potion.jump)) {
            InstanceAccess.mc.thePlayer.jump();
            MoveUtil.stop();
            step = true;
        }

        if (InstanceAccess.mc.thePlayer.offGroundTicks == 3 && step) {
            InstanceAccess.mc.thePlayer.motionY = MoveUtil.predictedMotion(InstanceAccess.mc.thePlayer.motionY, 2);
            MoveUtil.strafe(MoveUtil.WALK_SPEED);
            step = false;
        }
    };
}
