package com.alan.clients.module.impl.render;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.render.Render3DEvent;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.util.render.GLUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.value.impl.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
@ModuleInfo(name = "module.render.trail.name",category = Category.RENDER,description = "CNM")
public class Trail extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Line"))
            .add(new SubMode("Rise"))
            .setDefault("Rise");
    private final NumberValue particleAmount = new NumberValue("Particle Amount",this, 15.0, 1.0, 500.0, 1.0);
    private final BooleanValue seeThroughWalls = new BooleanValue("Walls",this, true);
    private final ColorValue color = new ColorValue("Color",this, Color.WHITE);
    private final List<Vec3> path = new ArrayList<>();
    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (Trail.mc.thePlayer.lastTickPosX != Trail.mc.thePlayer.posX || Trail.mc.thePlayer.lastTickPosY != Trail.mc.thePlayer.posY || Trail.mc.thePlayer.lastTickPosZ != Trail.mc.thePlayer.posZ) {
            this.path.add(new Vec3(Trail.mc.thePlayer.posX, Trail.mc.thePlayer.posY, Trail.mc.thePlayer.posZ));
        }
        while (this.path.size() > this.particleAmount.getValue().doubleValue()) {
            this.path.remove(0);
        }
    };
    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {
        int i = 0;
        final Pair<Color, Color> colors = Pair.of(this.color.getValue(), this.color.getValue());
        switch (this.mode.getValue().getName()) {
            case "Rise": {
                if (this.seeThroughWalls.getValue()) {
                    GlStateManager.disableDepth();
                }
                GL11.glEnable(3042);
                GL11.glDisable(3553);
                GL11.glEnable(2848);
                GL11.glBlendFunc(770, 771);
                for (final Vec3 v : this.path) {
                    ++i;
                    boolean draw = true;
                    final double x = v.xCoord - Trail.mc.getRenderManager().renderPosX;
                    final double y = v.yCoord - Trail.mc.getRenderManager().renderPosY;
                    final double z = v.zCoord - Trail.mc.getRenderManager().renderPosZ;
                    final double distanceFromPlayer = Trail.mc.thePlayer.getDistance(v.xCoord, v.yCoord - 1.0, v.zCoord);
                    int quality = (int)(distanceFromPlayer * 4.0 + 10.0);
                    if (quality > 350) {
                        quality = 350;
                    }
                    if (i % 10 != 0 && distanceFromPlayer > 25.0) {
                        draw = false;
                    }
                    if (i % 3 == 0 && distanceFromPlayer > 15.0) {
                        draw = false;
                    }
                    if (draw) {
                        GL11.glPushMatrix();
                        GL11.glTranslated(x, y, z);
                        final float scale = 0.06f;
                        GL11.glScalef(-0.06f, -0.06f, -0.06f);
                        GL11.glRotated(-Trail.mc.getRenderManager().playerViewY, 0.0, 1.0, 0.0);
                        GL11.glRotated(Trail.mc.getRenderManager().playerViewX, 1.0, 0.0, 0.0);
                        final Color c = ColorUtil.interpolateColorsBackAndForth(7, 3 + i * 20, getTheme().getFirstColor(), getTheme().getSecondColor(), false);
                        RenderUtil.drawFilledCircleNoGL(0, -2, 0.7, ColorUtil.applyOpacity(c.getRGB(), 0.6f), quality);
                        if (distanceFromPlayer < 4.0) {
                            RenderUtil.drawFilledCircleNoGL(0, -2, 1.4, ColorUtil.applyOpacity(c.getRGB(), 0.25f), quality);
                        }
                        if (distanceFromPlayer < 20.0) {
                            RenderUtil.drawFilledCircleNoGL(0, -2, 2.3, ColorUtil.applyOpacity(c.getRGB(), 0.15f), quality);
                        }
                        GL11.glScalef(0.8f, 0.8f, 0.8f);
                        GL11.glPopMatrix();
                    }
                }
                GL11.glDisable(2848);
                GL11.glEnable(3553);
                GL11.glDisable(3042);
                if (this.seeThroughWalls.getValue()) {
                    GlStateManager.enableDepth();
                }
                GL11.glColor3d(255.0, 255.0, 255.0);
                break;
            }
            case "Line": {
                this.renderLine(this.path, colors);
                break;
            }
        }
    };

    @Override
    protected void onEnable() {
        this.path.clear();
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        this.path.clear();
        super.onDisable();
    }

    public void renderLine(final List<Vec3> path, final Pair<Color, Color> colors) {
        GlStateManager.disableDepth();
        RenderUtil.setAlphaLimit(0.0f);
        RenderUtil.resetColor();
        GLUtil.setup2DRendering();
        GLUtil.startBlend();
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glShadeModel(7425);
        GL11.glLineWidth(3.0f);
        GL11.glBegin(3);
        int count = 0;
        int alpha = 200;
        final int fadeOffset = 15;
        for (final Vec3 v : path) {
            if (fadeOffset > count) {
                alpha = count * (200 / fadeOffset);
            }
            RenderUtil.resetColor();
            RenderUtil.color(RenderUtil.reAlpha(ColorUtil.interpolateColorsBackAndForth(15, count * 5, getTheme().getFirstColor(), getTheme().getSecondColor(), false), alpha).getRGB());
            final double x = v.xCoord - Trail.mc.getRenderManager().renderPosX;
            final double y = v.yCoord - Trail.mc.getRenderManager().renderPosY;
            final double z = v.zCoord - Trail.mc.getRenderManager().renderPosZ;
            GL11.glVertex3d(x, y, z);
            ++count;
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GLUtil.end2DRendering();
        GlStateManager.enableDepth();
    }
}
