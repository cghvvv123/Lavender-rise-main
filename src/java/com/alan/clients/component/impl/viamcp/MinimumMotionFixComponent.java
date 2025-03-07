package com.alan.clients.component.impl.viamcp;

import com.alan.clients.component.Component;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.MinimumMotionEvent;
import net.minecraft.viamcp.ViaMCP;

public final class MinimumMotionFixComponent extends Component {

    // 跳跃修复
    @EventLink()
    public final Listener<MinimumMotionEvent> onMinimumMotion = event -> {
        if (ViaMCP.getInstance().getVersion() <= 47) {
            event.setMinimumMotion(0.005D);
        } else {
            event.setMinimumMotion(0.003D);
        }
    };
}
