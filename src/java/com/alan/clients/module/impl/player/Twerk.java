package com.alan.clients.module.impl.player;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.input.MoveInputEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;

@Rise
@ModuleInfo(name = "module.player.twerk.name", description = "module.player.twerk.description", category = Category.PLAYER)
public class Twerk extends Module {

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        InstanceAccess.mc.gameSettings.keyBindSneak.setPressed(Math.random() < 0.5 && InstanceAccess.mc.thePlayer.ticksExisted % 2 == 0);
    };


    @EventLink()
    public final Listener<MoveInputEvent> onMove = event -> {
        event.setSneakSlowDownMultiplier(0);
    };
}
