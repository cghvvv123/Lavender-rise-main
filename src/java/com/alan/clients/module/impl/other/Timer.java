package com.alan.clients.module.impl.other;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.value.impl.BoundsNumberValue;

@Rise
@ModuleInfo(name = "module.other.timer.name", description = "module.other.timer.description", category = Category.OTHER)
public final class Timer extends Module {

    private final BoundsNumberValue timer =
            new BoundsNumberValue("Timer", this, 1, 2, 0.1, 10, 0.05);

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> InstanceAccess.mc.timer.timerSpeed = (float) MathUtil.getRandom(timer.getValue().floatValue(), timer.getSecondValue().floatValue());
}
