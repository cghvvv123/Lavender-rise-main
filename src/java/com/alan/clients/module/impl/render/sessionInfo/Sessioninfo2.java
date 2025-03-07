package com.alan.clients.module.impl.render.sessionInfo;

import com.alan.clients.Client;
import com.alan.clients.module.impl.movement.Flight;
import com.alan.clients.module.impl.other.HytAutoPlay;
import com.alan.clients.module.impl.other.HytHelper;
import com.alan.clients.module.impl.render.SessionInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.util.render.GLUtil;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.render.StencilUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.util.concurrent.TimeUnit;

import static com.alan.clients.module.impl.render.Interface.mixColors2;


public class Sessioninfo2 extends Mode<SessionInfo> {
    public Sessioninfo2(String name, SessionInfo parent) {
        super(name, parent);
    }
    private final BooleanValue showTitle = new BooleanValue("Title", this, true);

    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);
    private String time = "0 seconds";
    private SessionInfo SessionInfo;
    private int userBans, globalBans;
    private double distanceWalked, distanceFlown;
    private String username;
    private final long startTime = System.currentTimeMillis();

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (MoveUtil.isMoving() && MoveUtil.speed() < 0.5 && !mc.thePlayer.inWater &&
                !Client.INSTANCE.getModuleManager().get(Flight.class).isEnabled()) {

            double deltaX = mc.thePlayer.lastTickPosX - mc.thePlayer.posX;
            double deltaZ = mc.thePlayer.lastTickPosZ - mc.thePlayer.posZ;
            double distance = Math.hypot(deltaX, deltaZ);

            if (distance < 5) {
                this.distanceWalked += distance;
            }
        } else if (MoveUtil.isMoving() && Client.INSTANCE.getModuleManager().get(Flight.class).isEnabled()) {
            double deltaX = mc.thePlayer.lastTickPosX - mc.thePlayer.posX;
            double deltaZ = mc.thePlayer.lastTickPosZ - mc.thePlayer.posZ;
            double distance = Math.hypot(deltaX, deltaZ);

            this.distanceFlown += distance;
        }

        // Don't do this awful shit every frame
        if (mc.thePlayer.ticksExisted % 10 == 0) {
            long elapsed = System.currentTimeMillis() - this.startTime;
            long hours = TimeUnit.MILLISECONDS.toHours(elapsed);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60;

            String base = "";
            if (hours > 0) base += hours + (hours == 1 ? Localization.get("ui.sessionstats.hour") : Localization.get("ui.sessionstats.hours")) + ((minutes == 0 ? "" : ", "));
            if (minutes > 0)
                base += minutes + (minutes == 1 ? Localization.get("ui.sessionstats.minute") : Localization.get("ui.sessionstats.minutes")) + (seconds == 0 || hours > 0 ? "" : ", ");
            if (seconds > 0 && hours == 0) base += seconds + (seconds == 1 ? Localization.get("ui.sessionstats.second") : Localization.get("ui.sessionstats.seconds"));
            this.time = base;
        }
    };



    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        final SessionInfo SessionInfo =getModule(SessionInfo.class);
        if (this.SessionInfo == null) {
            this.SessionInfo = this.getModule(SessionInfo.class);
        }
        final String titleString = showTitle.getValue() ? Localization.get("SessionInfo") : "";
        double padding = 2;
        EntityPlayer player2 = mc.thePlayer;

        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            float width = scale.x + 147;
            float height = scale.y + 46;
            final double textX = SessionInfo.position.x +4;
            final double textY = SessionInfo.position.y + scale.y / 2.0F - FontManager.getNunitoBold(20).height() / 4.0F - 6F;
            username = mc.getSession() == null || mc.getSession().getUsername() == null ? "null" : mc.getSession().getUsername();
            SessionInfo.positionValue.setScale(new Vector2d(width, height));
            if(SessionInfo.rendertitle()){
                FontManager.getProductSansLight(20).drawString("SessionInfo", textX-2.5, textY-12,new Color(255,255,255,200).getRGB());
            }
            RenderUtil.rectangle(SessionInfo.position.x, SessionInfo.position.y, width, height,getTheme().getBackgroundShade());
            RenderUtil.resetColor();
            GradientUtil.applyGradientHorizontal((float) textX+2f, (float) ((float) textY+2.5), FontManager.getNunitoBold(20).width(titleString), 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                RenderUtil.setAlphaLimit(0);
                FontManager.getNunitoBold(20).drawString(titleString, textX+2, textY+4, 0);
            });
            if(showTitle.getValue()) RenderUtil.roundedRectangle(SessionInfo.position.x, SessionInfo.position.y+3, 2, 9.5,2,getClientColors()[0]);

            renderTargetHead(player2, (float) textX, (float) (SessionInfo.position.y + 16+padding),45);

            RenderUtil.circle(textX, SessionInfo.position.y + 16+padding, 45, 360, false, new Color(0, 0, 0, 70));
            RenderUtil.circle(textX, SessionInfo.position.y + 16+padding, 45, (System.currentTimeMillis() - this.startTime) * 0.00001,
                    false,  Color.white);

            FontManager.getProductSansBold(18).drawStringWithShadow(username, textX + padding+43, SessionInfo.position.y + padding + 19, new Color(255, 255, 255, 210).getRGB());

            FontManager.getProductSansBold(18).drawStringWithShadow(time, textX + padding+47, SessionInfo.position.y + padding + 35, new Color(255, 255, 255, 180).getRGB());

            FontManager.getProductSansMedium(18).drawStringWithShadow( "kills " + HytHelper.kill, textX + padding+44, SessionInfo.position.y + padding + 50, new Color(255, 255, 255, 180).getRGB());

            FontManager.getProductSansMedium(18).drawStringWithShadow( "wins " + HytAutoPlay.wins, textX + padding + 73, SessionInfo.position.y + padding + 50, new Color(255, 255, 255, 180).getRGB());

        });
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.rectangle(SessionInfo.position.x, SessionInfo.position.y, scale.x + 147, scale.y + 46,
                getTheme().getDropShadow()));
        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.rectangle(SessionInfo.position.x, SessionInfo.position.y, scale.x + 148, scale.y + 46, Color.BLACK));
    };
    private void renderTargetHead(EntityPlayer player, final float x, final float y, final float size) {
        StencilUtil.initStencilToWrite();
        RenderUtil.circle(x,
                y, size, 360, true, new Color(0, 0, 0, 40));
        StencilUtil.bindReadStencilBuffer(1);
        RenderUtil.resetColor();
        RenderUtil.setAlphaLimit(0);
        RenderUtil.resetColor();
        renderPlayer2D(x , y, size+1, size+1, (AbstractClientPlayer) player);
        StencilUtil.uninitStencilBuffer();
    }
    protected void renderPlayer2D(float x, float y, float width, float height, AbstractClientPlayer player) {
        GLUtil.startBlend();
        mc.getTextureManager().bindTexture(player.getLocationSkin());
        Gui.drawScaledCustomSizeModalRect(x, y, (float) 8.0, (float) 8.0, 8, 8, width, height, 64.0F, 64.0F);
        GLUtil.endBlend();
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
