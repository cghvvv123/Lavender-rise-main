package com.alan.clients.module.impl.render.sessionInfo;

import com.alan.clients.Client;
import com.alan.clients.module.impl.other.HytAutoPlay;
import com.alan.clients.module.impl.render.KillEffect;
import com.alan.clients.module.impl.render.SessionInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.font.impl.rise.FontRenderer;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;

import java.awt.*;

import static com.alan.clients.module.impl.render.Interface.mixColors2;


public class Sessioninfo1 extends Mode<SessionInfo> {
    public Sessioninfo1(String name, SessionInfo parent) {
        super(name, parent);
    }
    private final BooleanValue showTitle = new BooleanValue("Title", this, false);
    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);

    private SessionInfo SessionInfo;
    private String getFormattedTime()
    {
        long durationInMillis = System.currentTimeMillis() - Client.START_TIME;
        long second = durationInMillis / 1000 % 60;
        long minute = durationInMillis / (1000 * 60) % 60;
        long hour = durationInMillis / (1000 * 60 * 60) % 24;

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        final SessionInfo SessionInfo =getModule(SessionInfo.class);
        if (this.SessionInfo == null) {
            this.SessionInfo = this.getModule(SessionInfo.class);
        }
        Vector2d position = this.SessionInfo.position;
        final String titleString = showTitle.getValue() ? Localization.get("SessionInfo") : "";

        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            float width = scale.x + 147;
            float height = scale.y + 28;
            final double textX = position.x + 5.3F;
            final double textY = position.y + scale.y / 2.0F - nunitoNormal.height() / 4.0F - 1F;
            SessionInfo.positionValue.setScale(new Vector2d(width, height));
            if(SessionInfo.rendertitle()){
                FontManager.getProductSansLight(20).drawString("SessionInfo", textX-3, textY-16,new Color(255,255,255,200).getRGB());
            }
            RenderUtil.roundedRectangle(position.x, position.y, width, height, getTheme().getRound(), getTheme().getBackgroundShade());
            RenderUtil.resetColor();
            GradientUtil.applyGradientHorizontal((float) textX, (float) textY,  nunitoNormal.width(titleString), 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                RenderUtil.setAlphaLimit(0);
                nunitoNormal.drawString(titleString, textX, textY, 0);
            });
            FontRenderer fontRenderer = (FontRenderer) FontManager.getTahoma(16);
            fontRenderer.drawStringWithShadow(Localization.get("Elap"),textX, textY + 11, Color.WHITE.getRGB());
            fontRenderer.drawStringWithShadow(getFormattedTime(),textX + width - fontRenderer.width(getFormattedTime()) - 10, textY + 11, Color.WHITE.getRGB());
            fontRenderer.drawStringWithShadow(Localization.get("Ban"),textX, textY + 21, Color.WHITE.getRGB());
            fontRenderer.drawStringWithShadow(String.valueOf((HytAutoPlay.banned)),textX + width - fontRenderer.width(String.valueOf((HytAutoPlay.banned))) - 10, textY + 21, Color.WHITE.getRGB());
            fontRenderer.drawStringWithShadow(Localization.get("Kill"),textX, textY + 31, Color.WHITE.getRGB());
            fontRenderer.drawStringWithShadow(String.valueOf((KillEffect.kills)),textX + width - fontRenderer.width(String.valueOf((KillEffect.kills))) - 10, textY + 31, Color.WHITE.getRGB());
        });

        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, position.y, scale.x + 147, scale.y + 28, getTheme().getRound(), Color.BLACK));
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, position.y, scale.x + 147, scale.y + 28, getTheme().getRound() + 1, getTheme().getDropShadow()));
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
