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
import com.alan.clients.util.font.Font;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.render.StencilUtil;
import com.alan.clients.util.render.particle.Particle;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
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

public class LacenderTargetInfo extends Mode<TargetInfo> {
    private TargetInfo targetInfoModule;

    private final BooleanValue particles = new BooleanValue("Particles", this, true);
    private final Animation openingAnimation = new Animation(EASE_OUT_ELASTIC, 500);
    private final Animation Animation = new Animation(EASE_OUT_ELASTIC, 500);
    private final Animation healthAnimation = new Animation(EASE_OUT_SINE, 500);
    public LacenderTargetInfo(String name, TargetInfo parent) {
        super(name, parent);
    }
    private final int EDGE_OFFSET = 8;
    private final int PADDING = 7;
    private final int INDENT = 4;
    private int destinationY = 4;
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
            com.alan.clients.util.font.FontManager.getProductSansLight(20).drawString("TargetInfo",  (x), (float) (y - 11),new Color(255,255,255,200).getRGB());
        }
        if (mc.currentScreen instanceof GuiChat) {
            this.render(x, y, (EntityLivingBase)mc.thePlayer);
        }else {
            if (targetInfo.multi_targetHUD.getValue()) {
                if (KillAura.targets != null && aura.target != null) {
                    for (int i = 0; i < KillAura.targets.size(); ++i) {
                        final Entity target = KillAura.targets.get(i);
                        Vector4d position = ProjectionComponent.get(target);
                        if (targetInfo.followPlayer.getValue() && position == null) return;
                        destinationY = i <= 0 ? 0 : 40;
                        if (i <= 2) {
                            x = targetInfo.followPlayer.getValue() ? (float) position.z : x;
                            y = targetInfo.followPlayer.getValue() ? (float) (position.w - (position.w - position.y) / 2 - this.targetInfoModule.positionValue.scale.y / 2f) : y;
                            this.render(x, y, (EntityLivingBase) target);
                            GlStateManager.resetColor();
                            y += targetInfo.followPlayer.getValue() ? 0 : Animation.getValue();
                        }
                    }
                }
            } else if (target != null) {
                this.render(x, y, target);
                GlStateManager.resetColor();
            }
        }
    };
    public void render(final float x, final float y, final EntityLivingBase target2) {
        if(target2 == null) return;
        final KillAura aura =getModule(KillAura.class);
        AbstractClientPlayer target = (AbstractClientPlayer) target2;
        boolean out = (!this.targetInfoModule.inWorld || this.targetInfoModule.stopwatch.finished(1000));
        openingAnimation.setDuration(out ? 400 : 850);
        openingAnimation.setEasing(out ? EASE_IN_BACK : EASE_OUT_ELASTIC);
        openingAnimation.run(out ? 0 : 1);
        if (openingAnimation.getValue() <= 0) return;

        String name = target.getCommandSenderName();
        double health = Math.min(!this.targetInfoModule.inWorld ? 0 : MathUtil.round(target.getHealth(), 1), target.getMaxHealth());
        double healthWidth = ((double) 88);
        double Widthfix = ((double) 127);

        healthAnimation.run((health / target.getMaxHealth()) * healthWidth);
        healthAnimation.setEasing(EASE_OUT_QUINT);
        healthAnimation.setDuration(250);

        double healthRemainingWidth = healthAnimation.getValue();
        double hurtTime = (target.hurtTime == 0 ? 0 : target.hurtTime - mc.timer.renderPartialTicks) * 0.5;
        int faceScale = 28;
        double faceOffset = hurtTime / 2f;
        double height = 32 + EDGE_OFFSET * 2;
        double scale = openingAnimation.getValue();

        this.targetInfoModule.positionValue.setScale(new Vector2d(Widthfix, height));
        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            if(target == null) return;
            GlStateManager.pushMatrix();
            GlStateManager.translate((x + Widthfix / 2) * (1 - scale), (y + height / 2) * (1 - scale), 0);
            GlStateManager.scale(scale, scale, 0);

            RenderUtil.roundedRectangle(x, y, Widthfix, height-14, 3, getTheme().getBackgroundShade());
            // Render name
            Font fluxicon = com.alan.clients.util.font.FontManager.getfluxicon(19);

            GlStateManager.resetColor();
            GlStateManager.popMatrix();
            ParticleComponent.render();
            GlStateManager.pushMatrix();
            GlStateManager.translate((x + Widthfix / 2) * (1 - scale), (y + height / 2) * (1 - scale), 0);
            GlStateManager.scale(scale, scale, 0);
            // Targets face
            RenderUtil.color(ColorUtil.mixColors(Color.RED, Color.WHITE, hurtTime / 9));

            RenderUtil.dropShadow(3, x + EDGE_OFFSET + faceOffset-5, y + EDGE_OFFSET + faceOffset-5, faceScale - hurtTime, faceScale - hurtTime, 20, 4);

            renderTargetHead(target, x + EDGE_OFFSET + faceOffset-5, y + EDGE_OFFSET + faceOffset-5, faceScale - hurtTime);
            // Health background
            RenderUtil.roundedRectangle(x + EDGE_OFFSET + 32 + PADDING-13, y + EDGE_OFFSET + 32 - INDENT - 17, healthWidth, 11, 0, new Color(37, 36, 36));
            // Health
            RenderUtil.roundedRectangle(x + EDGE_OFFSET + 32 + PADDING-13, y + EDGE_OFFSET + 32 - INDENT - 17, healthRemainingWidth, 11, 0,new Color(62, 89, 236, 255));

            GlStateManager.resetColor();
            GlStateManager.popMatrix();
            if(aura.target==target){
                fluxicon.drawString("a"+" ",x + EDGE_OFFSET + 32 + PADDING -12, y + EDGE_OFFSET + INDENT -5,-1);
                FontManager.arial20.drawString(name, x + EDGE_OFFSET + 32 + PADDING -12+fluxicon.width("a"+" "), y + EDGE_OFFSET + INDENT -6,  -1);
            } else {
                FontManager.arial20.drawString(name, x + EDGE_OFFSET + 32 + PADDING - 12, y + EDGE_OFFSET + INDENT - 6, -1);
            }
            FontManager.arial17.drawString(health +"%", (float) (x +67), (float) (y + EDGE_OFFSET + 32 - INDENT - 14.9), -1);
        });
        NORMAL_BLUR_RUNNABLES.add(() -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate((x + Widthfix / 2) * (1 - scale), (y + height / 2) * (1 - scale), 0);
            GlStateManager.scale(scale, scale, 0);

            RenderUtil.roundedRectangle(x, y, Widthfix, height-14, 3, Color.BLACK);

            GlStateManager.popMatrix();
        });

        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
            GlStateManager.pushMatrix();
            GlStateManager.translate((x + Widthfix / 2) * (1 - scale), (y + height / 2) * (1 - scale), 0);
            GlStateManager.scale(scale, scale, 0);

            RenderUtil.roundedRectangle(x, y, Widthfix, height-14, 3, getTheme().getDropShadow());

            GlStateManager.popMatrix();
        });
    }
    private void renderTargetHead(final AbstractClientPlayer abstractClientPlayer, final double x, final double y, final double size) {
        StencilUtil.initStencil();
        StencilUtil.bindWriteStencilBuffer();
        RenderUtil.roundedRectangle(x, y, size, size, 4, this.getTheme().getBackgroundShade());
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
