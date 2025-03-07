package com.alan.clients.module.impl.movement.step;

import com.alan.clients.module.impl.movement.Step;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.other.StepEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.value.Mode;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @author Alan
 * @since 22/3/2022
 */

public class VulcanStep extends Mode<Step> {

    public VulcanStep(String name, Step parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        InstanceAccess.mc.thePlayer.stepHeight = 0.6F;
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (InstanceAccess.mc.thePlayer.ticksSinceJump > 11) {
            InstanceAccess.mc.thePlayer.stepHeight = 1;
        } else {
            InstanceAccess.mc.thePlayer.stepHeight = 0.6F;
        }
    };

    @EventLink()
    public final Listener<StepEvent> onStep = event -> {

        if (event.getHeight() > 0.6) {
            InstanceAccess.mc.timer.timerSpeed = 0.5f;
            PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(InstanceAccess.mc.thePlayer.posX, InstanceAccess.mc.thePlayer.posY + 0.5F, InstanceAccess.mc.thePlayer.posZ, true));
        }
    };
}
