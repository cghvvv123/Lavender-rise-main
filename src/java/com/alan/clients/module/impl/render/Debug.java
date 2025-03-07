package com.alan.clients.module.impl.render;

import com.alan.clients.fontRender.FontManager;
import com.alan.clients.fontRender.RapeMasterFontManager;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.DragValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.alan.clients.module.impl.render.Interface.mixColors2;

@ModuleInfo(name = "module.render.debug.name", category = Category.RENDER, description = "CNM")
public class Debug extends Module {
    private final BooleanValue showTitle = new BooleanValue("Title", this, false);
    private final DragValue position = new DragValue("Position", this, new Vector2d(150, 200));
    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        Vector2d position = this.position.position;
        final String titleString = showTitle.getValue() ? Localization.get("Debug") : "";
        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            final float titleWidth = nunitoNormal.width(titleString);
            final double Y = position.y;
            float x = (float) position.x + 20;
            float y = (float) position.y + 60;
            final double textX = position.x + 24;
            final double textY = Y + scale.y / 2.0F - nunitoNormal.height() / 8.0F + 58;
            RenderUtil.resetColor();
            final EntityPlayerSP player = Debug.mc.thePlayer;
            final RapeMasterFontManager A16 = FontManager.arial18;
            String block = player.isBlocking() ? "True" : "False";
            String sprint = player.isSprinting() ? "True" : "False";
            String eat = player.isEating() ? "True" : "False";
            String move = MoveUtil.isMoving() ? "True" : "False";
            long time = System.currentTimeMillis();
            Date date = new Date(time);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = format.format(date);
            RenderUtil.roundedRectangle(x, y, 150, 150, 4, getTheme().getBackgroundShade());
            GradientUtil.applyGradientHorizontal((float) textX, (float) textY, titleWidth, 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                RenderUtil.setAlphaLimit(0);
                nunitoNormal.drawString(titleString, textX, textY, 0);
            });
            x += 5.0F;
            y -= -7;
            A16.drawStringWithShadow("Health: " + this.toFloat(player.getHealth()), x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow("X:" + this.toFloat(player.posX) + " Y:" + this.toFloat(player.posY) + " Z:" + this.toFloat(player.posZ), x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow("Motion X:" + this.toDouble(player.motionX) + " Y:" + this.toDouble(player.motionY) + " Z:" + this.toDouble(player.motionZ), x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow("Hurt Time: " + player.hurtTime, x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow("Hurt ResistantTime Time: " + player.hurtResistantTime, x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow("Yaw: " + this.toFloat(player.rotationYaw) + " Pitch: " + this.toFloat(player.rotationPitch), x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow("Head: " + this.toFloat(player.rotationYawHead) + " Body: " + this.toFloat(player.renderYawOffset), x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow("Block: " + block, x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow("Sprint: " + sprint, x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow("Eat: " + eat, x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow("Move: " + move, x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow("Direction: " + this.getPlayerFacingDirection(player), x, y += 10.0F, new Color(255, 255, 255).getRGB());
            A16.drawStringWithShadow(formattedDate, x, y += 10.0F, new Color(255, 255, 255).getRGB());
        });
        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle((float) position.x + 20, (float) position.y + 60, 150, 150, 4, Color.BLACK));
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle((float) position.x + 20, (float) position.y + 60, 150, 150, 4 + 1, getTheme().getDropShadow()));

    };
    private float toFloat(final double value) {
        return (int)value + (int)(value * 10.0) % 10 / 10.0f;
    }
    private float toDouble(final double value) {
        return (int)value + (int)(value * 100.0) % 100 / 100.0f;
    }
    String getPlayerFacingDirection(Entity player) {
        Vec3 lookVec = player.getLookVec();
        double x = lookVec.xCoord;
        double z = lookVec.zCoord;
        if (Math.abs(x) > Math.abs(z)) {
            if (x > 0) {
                if (z > 0) {
                    return "SE";
                } else if (z < 0) {
                    return "NE";
                } else {
                    return "E";
                }
            } else if (x < 0) {
                if (z > 0) {
                    return "SW";
                } else if (z < 0) {
                    return "NW";
                } else {
                    return "W";
                }
            } else {
                if (z > 0) {
                    return "S";
                } else if (z < 0) {
                    return "N";
                } else {
                    return "None";
                }
            }
        } else {
            if (z > 0) {
                return "S";
            } else if (z < 0) {
                return "N";
            } else {
                return "None";
            }
        }
    }
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
