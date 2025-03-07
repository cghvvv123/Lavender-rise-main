package com.alan.clients.module.impl.render.targetinfo;

import com.alan.clients.component.impl.render.ParticleComponent;
import com.alan.clients.component.impl.render.ProjectionComponent;
import com.alan.clients.fontRender.FontManager;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.module.impl.render.TargetInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.animations.Animation;
import com.alan.clients.util.animations.Easing;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.render.StencilUtil;
import com.alan.clients.util.render.particle.Particle;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.SubMode;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector4d;
import java.awt.*;

import static com.alan.clients.util.animations.Easing.*;

public class ModernTargetInfo extends Mode<TargetInfo> {

    private final BooleanValue particles = new BooleanValue("Particles", this, true);

    private final ModeValue backgroundMode = new ModeValue("Background Mode", this) {{
        add(new SubMode("Glass"));
        add(new SubMode("Tint"));
        add(new SubMode("Solid"));
        setDefault("Glass");
    }};

    private TargetInfo targetInfoModule;
    private final int EDGE_OFFSET = 8;
    private final int PADDING = 7;
    private final int INDENT = 4;
    private int destinationY = 4;
    private final Animation Animation = new Animation(EASE_OUT_ELASTIC, 500);
    private final Animation openingAnimation = new Animation(EASE_OUT_ELASTIC, 500);
    private final Animation healthAnimation = new Animation(EASE_OUT_SINE, 500);
    public ModernTargetInfo(String name, TargetInfo parent) {
        super(name, parent);
    }

    private EntityLivingBase target;

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        final TargetInfo targetInfo =getModule(TargetInfo.class);
        if (this.targetInfoModule == null) {
            this.targetInfoModule = this.getModule(TargetInfo.class);
        }
        if (TargetInfo.target == null) return;

        float x =   (float) this.targetInfoModule.position.x;
        float y = (float) this.targetInfoModule.position.y;
        final KillAura aura =getModule(KillAura.class);

        Animation.run(destinationY);
        Animation.setDuration(1150);
        Animation.setEasing(Easing.EASE_OUT_EXPO);
        if(TargetInfo.rendertitle()){
            com.alan.clients.util.font.FontManager.getProductSansLight(20).drawString("TargetInfo",  (x+1), (float) (y - 11),new Color(255,255,255,200).getRGB());
        }
        if (mc.currentScreen instanceof GuiChat) {
            this.render(x, y, (EntityLivingBase)mc.thePlayer);
        }else {
            if (targetInfo.multi_targetHUD.getValue()) {
                if (KillAura.targets != null && aura.target != null) {
                    int count = 0;
                    for (int i = 0; i < KillAura.targets.size(); ++i) {
                        final Entity target = KillAura.targets.get(i);
                        Vector4d position = ProjectionComponent.get(target);
                        if (targetInfo.followPlayer.getValue() && position == null) return;
                        destinationY = i <= 0 ? 0 : 60;
                        if (count <= 2) {
                            x = targetInfo.followPlayer.getValue() ? (float) position.z : x;
                            y = targetInfo.followPlayer.getValue() ? (float) (position.w - (position.w - position.y) / 2 - this.targetInfoModule.positionValue.scale.y / 2f) : y;
                            this.render(x, y, (EntityLivingBase) target);
                            y += targetInfo.followPlayer.getValue() ? 0 : Animation.getValue();
                            ++count;
                        }
                    }
                }
            } else if (target != null) {
                this.render(x, y, target);
            }
        }
    };
    public void render(final float x, final float y, final EntityLivingBase target) {
        if(target == null) return;
        boolean out = (!this.targetInfoModule.inWorld || this.targetInfoModule.stopwatch.finished(1000));
        openingAnimation.setDuration(out ? 400 : 850);
        openingAnimation.setEasing(out ? EASE_IN_BACK : EASE_OUT_ELASTIC);
        openingAnimation.run(out ? 0 : 1);

        if (openingAnimation.getValue() <= 0) return;

        String name = target.getCommandSenderName();

        double nameWidth = FontManager.arial22.getStringWidth(name);
        double health = Math.min(!this.targetInfoModule.inWorld ? 0 : MathUtil.round(target.getHealth(), 1), target.getMaxHealth());
        double healthTextWidth = FontManager.arial22.getStringWidth(String.valueOf(health));
        double healthBarWidth = Math.max(nameWidth + 35 - healthTextWidth, 65);

        healthAnimation.run((health / target.getMaxHealth()) * healthBarWidth);
        healthAnimation.setEasing(EASE_OUT_QUINT);
        healthAnimation.setDuration(250);
        double healthRemainingWidth = healthAnimation.getValue();

        double hurtTime = (target.hurtTime == 0 ? 0 :
                target.hurtTime - mc.timer.renderPartialTicks) * 0.5;
        int faceScale = 32;
        double faceOffset = hurtTime / 2f;
        double width = EDGE_OFFSET + faceScale + EDGE_OFFSET + healthBarWidth + INDENT + healthTextWidth + EDGE_OFFSET;
        double height = faceScale + EDGE_OFFSET * 2;
        this.targetInfoModule.positionValue.setScale(new Vector2d(width, height));

        double scale = openingAnimation.getValue();

        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate((x + width / 2) * (1 - scale), (y + height / 2) * (1 - scale), 0);
            GlStateManager.scale(scale, scale, 0);

            // Draw background
            Color background1 = new Color(0, 0, 0, 100);
            Color background2 = new Color(0, 0, 0, 100);
            Color accent1 = getTheme().getFirstColor();
            Color accent2 = getTheme().getSecondColor();

            if (this.backgroundMode.getValue().getName().equals("Tint")) {
                Color theme1 = this.getTheme().getAccentColor(new Vector2d(x, y)), theme2 = this.getTheme().getAccentColor(new Vector2d(x, y + height));
                background1 = new Color(theme1.getRed() / 5, theme1.getGreen() / 5, theme1.getBlue() / 5, 128);
                background2 = new Color(theme2.getRed() / 5, theme2.getGreen() / 5, theme2.getBlue() / 5, 128);
            } else if (this.backgroundMode.getValue().getName().equals("Solid")) {
                Color theme1 = this.getTheme().getFirstColor(), theme2 = this.getTheme().getSecondColor();
                background1 = new Color(theme1.getRed(), theme1.getGreen(), theme1.getBlue(), 128);
                background2 = new Color(theme2.getRed(), theme2.getGreen(), theme2.getBlue(), 128);
                accent1 = new Color(255, 255, 255);
                accent2 = new Color(164, 164, 164);
            }

            RenderUtil.drawRoundedGradientRect(x, y, width - 1, height, 14, background1, background2, true);

            // Render name
            FontManager.arial20.drawStringWithShadow(Localization.get("Name:"), x + EDGE_OFFSET + faceScale + PADDING, y + EDGE_OFFSET + INDENT + 3, Color.WHITE.getRGB());
            FontManager.arial22.drawStringWithShadow(name, x + EDGE_OFFSET + faceScale + PADDING +FontManager.arial20.getStringWidth(Localization.get("Name:")) + 3, y + EDGE_OFFSET + INDENT + 2.5, accent1.getRGB());

            GlStateManager.popMatrix();

            ParticleComponent.render();

            GlStateManager.pushMatrix();
            GlStateManager.translate((x + width / 2) * (1 - scale), (y + height / 2) * (1 - scale), 0);
            GlStateManager.scale(scale, scale, 0);

            // Targets face
            RenderUtil.color(ColorUtil.mixColors(Color.RED, Color.WHITE, hurtTime / 9));
            RenderUtil.dropShadow(3, x + EDGE_OFFSET + faceOffset, y + EDGE_OFFSET + faceOffset,
                    faceScale - hurtTime, faceScale - hurtTime, 20, this.getTheme().getRound() * 2);
            renderTargetHead((AbstractClientPlayer) target, x + EDGE_OFFSET + faceOffset, y + EDGE_OFFSET + faceOffset,
                    faceScale - hurtTime);

            // Health background
            RenderUtil.roundedRectangle(x + EDGE_OFFSET + faceScale + PADDING, y + EDGE_OFFSET + faceScale - INDENT - 7,
                    healthBarWidth, 6, 3, getTheme().getBackgroundShade());

            // Health
            RenderUtil.drawRoundedGradientRect(x + EDGE_OFFSET + faceScale + PADDING, y + EDGE_OFFSET + faceScale - INDENT - 7,
                    healthRemainingWidth, 6, 3, accent2, accent1, true);

            FontManager.arial22.drawStringWithShadow(String.valueOf(health),
                    x + EDGE_OFFSET + faceScale + PADDING + healthBarWidth + INDENT,
                    y + EDGE_OFFSET + faceScale - INDENT - 8, accent1.getRGB());
            GlStateManager.popMatrix();
        });

        NORMAL_PRE_RENDER_RUNNABLES.add(() -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate((x + width / 2) * (1 - scale), (y + height / 2) * (1 - scale), 0);
            GlStateManager.scale(scale, scale, 0);

            // Health background
            RenderUtil.roundedRectangle(x + EDGE_OFFSET + faceOffset, y + EDGE_OFFSET + faceOffset,
                    faceScale - hurtTime, faceScale - hurtTime, this.getTheme().getRound() * 2,
                    ColorUtil.withAlpha(Color.RED, (int) (hurtTime / 9 * 255)));

            // Health


            GlStateManager.popMatrix();
        });

        NORMAL_BLUR_RUNNABLES.add(() -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate((x + width / 2) * (1 - scale), (y + height / 2) * (1 - scale), 0);
            GlStateManager.scale(scale, scale, 0);
            RenderUtil.roundedRectangle(x, y, width - 1, height, 14, Color.BLACK);
            GlStateManager.popMatrix();
        });

        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate((x + width / 2) * (1 - scale), (y + height / 2) * (1 - scale), 0);
            GlStateManager.scale(scale, scale, 0);

//            final boolean glow = this.backgroundMode.getValue().getName().equals("Solid");
            final boolean glow = false;
            final Color outlineColor1 = glow ? this.getTheme().getFirstColor() : new Color(0, 0, 0, 128);
            final Color outlineColor2 = glow ? this.getTheme().getSecondColor() : new Color(0, 0, 0, 128);
            RenderUtil.drawRoundedGradientRect(x, y, width - 1, height, 14, outlineColor1, outlineColor2, true);

            GlStateManager.popMatrix();
        });
    };

    private void renderTargetHead(final AbstractClientPlayer abstractClientPlayer, final double x, final double y, final double size) {
        StencilUtil.initStencil();
        StencilUtil.bindWriteStencilBuffer();
        RenderUtil.roundedRectangle(x, y, size, size, this.getTheme().getRound() * 2, this.getTheme().getBackgroundShade());
        StencilUtil.bindReadStencilBuffer(1);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GlStateManager.enableTexture2D();

        final ResourceLocation resourceLocation = targetInfoModule.inWorld && abstractClientPlayer.getHealth() > 0
                ? abstractClientPlayer.getLocationSkin() : RenderSkeleton.getEntityTexture();

        mc.getTextureManager().bindTexture(resourceLocation);

        Gui.drawScaledCustomSizeModalRect(x, y, 4, 4, 4, 4, size, size, 32, 32);
        GlStateManager.disableBlend();
        StencilUtil.uninitStencilBuffer();
    }

    @EventLink()
    public final Listener<TickEvent> onTick = event -> {

        if (this.targetInfoModule == null) return;
        Entity target = this.targetInfoModule.target;

        if (target == null || openingAnimation.getValue() <= 0 || !this.particles.getValue()) return;
        final KillAura aura =getModule(KillAura.class);

        if (aura.target != null) {
            this.target = (EntityLivingBase) aura.target;

        }
        if (mc.currentScreen instanceof GuiChat) {
            this.target = (EntityLivingBase)mc.thePlayer;
        }
        double hurtTime = (((AbstractClientPlayer) target).hurtTime == 0 ? 0 :
                ((AbstractClientPlayer) target).hurtTime - mc.timer.renderPartialTicks) * 0.5;

        if (hurtTime > 0) {
            for (int i = 0; i < hurtTime * Math.random() / 2; i++) {
                ParticleComponent.add(new Particle(new Vector2f((float) (targetInfoModule.position.x + 20), (float) (targetInfoModule.position.y + 20)),
                        new Vector2f((float) (Math.random() - 0.5) * 1.7f, (float) (Math.random() - 0.5) * 1.7f)));
            }
        }
    };
}

