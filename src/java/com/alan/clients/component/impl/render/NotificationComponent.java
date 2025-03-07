package com.alan.clients.component.impl.render;

import com.alan.clients.api.Rise;
import com.alan.clients.component.Component;
import com.alan.clients.fontRender.FontManager;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.Priorities;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.animations.Animation;
import com.alan.clients.util.animations.Easing;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.tuples.Triple;
import com.alan.clients.util.vector.Vector2d;
import net.minecraft.client.renderer.GlStateManager;
import util.time.StopWatch;
import util.type.EvictingList;

import java.awt.*;

import static com.alan.clients.util.animations.Easing.EASE_OUT_ELASTIC;

@Rise
public class NotificationComponent extends Component {

    private static EvictingList<Triple<String, String, Integer>> queue = new EvictingList<>(5);
    public static StopWatch time = new StopWatch();
    public static Triple<String, String, Integer> current;
    private static Animation animation = new Animation(EASE_OUT_ELASTIC, 500);

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<Render2DEvent> onRender2DEvent = event -> {
        if (current == null) return;

        Vector2d SCALE = new Vector2d(140, 30);
        Vector2d ICON_SCALE = new Vector2d(20, 20);
        Vector2d POSITION = new Vector2d(5, 27);

        double SPACER = (SCALE.y - ICON_SCALE.y) / 2f;

        boolean out = time.finished(current.getThird());

        final double destinationY = out  ? -88 :-20;
        animation.run(destinationY);
        animation.setDuration(1000);
        animation.setEasing(Easing.EASE_OUT_EXPO);
        double scale = 1;
        double opacity = 1 - 10 * Math.abs(1 - 1.0);
        final float titleWidth = FontManager.arial16.getStringWidth(current.getSecond());
        final float xw = 100;
        final float tWidth = titleWidth-100;
        if (animation.isFinished() && out) return;
        double x = (POSITION.x + SCALE.x / 2) *(1 - 1.0)+809;
        double y = (POSITION.y + SCALE.y / 2) * (1 - 1.0)+animation.getValue();

        NORMAL_RENDER_RUNNABLES.add(() -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(scale, scale, 0);

            RenderUtil.roundedRectangle(POSITION.x-titleWidth+xw, POSITION.y, SCALE.x+tWidth, SCALE.y, getTheme().getRound(), ColorUtil.withAlpha(getTheme().getBackgroundShade(), (int) (getTheme().getBackgroundShade().getAlpha() * opacity)));

            RenderUtil.roundedRectangle(POSITION.x + SPACER-titleWidth+xw, POSITION.y + SPACER, ICON_SCALE.x, ICON_SCALE.y, 3, ColorUtil.withAlpha(Color.WHITE, (int) (255 * opacity)));

            FontManager.arial18.drawString(current.getFirst(), (float) (POSITION.x + SPACER + ICON_SCALE.x + SPACER-titleWidth+xw), (float) (POSITION.y + SPACER ), ColorUtil.withAlpha(getTheme().getFirstColor(), (int) (255 * opacity)).getRGB());

            FontManager.arial16.drawString(current.getSecond(), (float) (POSITION.x + SPACER + ICON_SCALE.x + SPACER-titleWidth+xw), (float) (POSITION.y + SPACER + 0.5 + SPACER * 0.7 +  FontManager.arial16.getFontHeight()-4), ColorUtil.withAlpha(Color.WHITE, (int) (255 * opacity)).getRGB());

            GlStateManager.popMatrix();
        });

        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(scale, scale, 0);

            RenderUtil.roundedRectangle(POSITION.x-titleWidth+xw, POSITION.y, SCALE.x+tWidth, SCALE.y,  getTheme().getRound(), ColorUtil.withAlpha(getTheme().getBackgroundShade(), (int) (getTheme().getBackgroundShade().getAlpha() * opacity)));

            GlStateManager.popMatrix();
        });

        NORMAL_BLUR_RUNNABLES.add(() -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(scale, scale, 0);

            RenderUtil.roundedRectangle(POSITION.x-titleWidth+xw, POSITION.y, SCALE.x+tWidth, SCALE.y,  getTheme().getRound(), Color.BLACK);

            GlStateManager.popMatrix();
        });

    };

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.thePlayer.ticksExisted % 5 != 0) return;

        if (!queue.isEmpty() && (current == null || time.finished(current.getThird() + 200))) {
            if (current != null) queue.remove(current);
            current = queue.get(0);
            time.reset();
        }
    };

    public static void post(String title, String description) {
        post(title, description, 3000);
    }

    public static void post(String title, String description, Integer time) {
        queue.add(new Triple<>(title, description, time));
    }

}
