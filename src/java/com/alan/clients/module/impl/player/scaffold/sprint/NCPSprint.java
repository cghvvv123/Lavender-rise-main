package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.Mode;
import net.minecraft.potion.Potion;

public class NCPSprint extends Mode<Scaffold> {
    public NCPSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        event.setOnGround(false);
        InstanceAccess.mc.gameSettings.keyBindSprint.setPressed(true);
        InstanceAccess.mc.thePlayer.setSprinting(true);
        if (InstanceAccess.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            InstanceAccess.mc.thePlayer.motionX *= 0.95;
            InstanceAccess.mc.thePlayer.motionZ *= 0.95;
        } else {
            InstanceAccess.mc.thePlayer.motionX *= 0.99;
            InstanceAccess.mc.thePlayer.motionZ *= 0.99;
        }
    };
}
