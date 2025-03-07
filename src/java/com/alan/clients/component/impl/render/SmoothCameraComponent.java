package com.alan.clients.component.impl.render;

import com.alan.clients.api.Rise;
import com.alan.clients.component.Component;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import util.time.StopWatch;

@Rise
public class SmoothCameraComponent extends Component {
    public static float bobbing;
    public static double y;
    public static StopWatch stopWatch = new StopWatch();

    public static void setY(double y) {
        stopWatch.reset();
        SmoothCameraComponent.y = y;
        bobbing = 0;
    }

    public static void setY() {
        if (stopWatch.finished(80)) SmoothCameraComponent.y = mc.thePlayer.lastTickPosY;
        stopWatch.reset();
        bobbing = 0;
    }

    public static void setY(double y, float bobbing) {
        stopWatch.reset();
        SmoothCameraComponent.y = y;
        SmoothCameraComponent.bobbing = bobbing;
    }

    public static void setY(float bobbing) {
        if (stopWatch.finished(80)) SmoothCameraComponent.y = mc.thePlayer.lastTickPosY;
        stopWatch.reset();
        SmoothCameraComponent.bobbing = bobbing;
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (stopWatch.finished(80)) return;
        mc.thePlayer.cameraYaw = bobbing;
        mc.thePlayer.cameraPitch = bobbing;
    };
}
