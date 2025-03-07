package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.Mode;

public class DisabledSprint extends Mode<Scaffold> {

    public DisabledSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        InstanceAccess.mc.gameSettings.keyBindSprint.setPressed(false);
        InstanceAccess.mc.thePlayer.setSprinting(false);
    };
}
