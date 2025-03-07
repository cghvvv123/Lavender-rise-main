package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.Mode;
import net.minecraft.util.MathHelper;

public class LegitSprint extends Mode<Scaffold> {

    public LegitSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (Math.abs(MathHelper.wrapAngleTo180_float(InstanceAccess.mc.thePlayer.rotationYaw) -
                MathHelper.wrapAngleTo180_float(RotationComponent.rotations.x)) > 90) {
            InstanceAccess.mc.gameSettings.keyBindSprint.setPressed(false);
            InstanceAccess.mc.thePlayer.setSprinting(false);
        }
    };
}
