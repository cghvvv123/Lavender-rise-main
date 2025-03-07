package com.alan.clients.module.impl.render;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.impl.DragValue;

import java.awt.*;

import static com.alan.clients.module.impl.render.Interface.mixColors2;

@ModuleInfo(name = "module.render.memory.name",category = Category.RENDER,description = "CNM")
public class Memory extends Module {
    private final DragValue position = new DragValue("Position", this, new Vector2d(200, 200));
    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        Vector2d position = this.position.position;
        Runtime runtime = Runtime.getRuntime();
        String mem = "Mem: " + (runtime.totalMemory() - runtime.freeMemory()) * 100L / runtime.maxMemory() + "%";
        scale.x =nunitoNormal.width(mem);

        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            RenderUtil.roundedRectangle(position.x, position.y, scale.x + 6, scale.y - 1, getTheme().getRound(), getTheme().getBackgroundShade());

            this.position.setScale(new Vector2d(scale.x + 6, scale.y - 1));

            final double textX = position.x + 3.0F;
            final double textY = position.y + scale.y / 2.5F - nunitoNormal.height() / 8.0F;
            GradientUtil.applyGradientHorizontal((float) textX, (float) textY,  nunitoNormal.width(mem), 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                RenderUtil.setAlphaLimit(0);
                nunitoNormal.drawString(mem, (float) textX, (float) textY, 0);
            });
        });
        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, position.y + 0.5, scale.x + 6, scale.y - 1, getTheme().getRound(), Color.BLACK));
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, position.y, scale.x + 6, scale.y - 1, getTheme().getRound() + 1, getTheme().getDropShadow()));
    };
    private Color getClientColor () {
        Color theme1 = this.getTheme().getFirstColor();
        return new Color(theme1.getRGB());

    }
    private Color getAlternateClientColor () {
        Color theme2 = this.getTheme().getSecondColor();
        return new Color(theme2.getRGB());
    }
    public Color[] getClientColors () {
        Color firstColor = mixColors2(getClientColor(), getAlternateClientColor());
        Color secondColor = mixColors2(getAlternateClientColor(), getClientColor());
        return new Color[]{firstColor, secondColor};
    }
}
