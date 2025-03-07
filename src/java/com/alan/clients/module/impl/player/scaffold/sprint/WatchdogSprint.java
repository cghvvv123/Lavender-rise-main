package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.module.impl.exploit.Disabler;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.other.MoveEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.Mode;

public class WatchdogSprint extends Mode<Scaffold> {
    public WatchdogSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        InstanceAccess.mc.gameSettings.keyBindSprint.setPressed(this.getModule(Disabler.class).isEnabled());
        InstanceAccess.mc.thePlayer.setSprinting(this.getModule(Disabler.class).isEnabled());
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        InstanceAccess.mc.gameSettings.keyBindSprint.setPressed(this.getModule(Disabler.class).isEnabled());
        InstanceAccess.mc.thePlayer.setSprinting(this.getModule(Disabler.class).isEnabled());
    };

    @EventLink
    public final Listener<MoveEvent> onMove = event -> {
        if (InstanceAccess.mc.thePlayer.ticksExisted % 10 == 0) {
            MoveUtil.strafe(MoveUtil.speed() - 0.03);
        }
    };
}
