package net.minecraft.client.renderer.entity.layers;

import com.alan.clients.util.render.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

public class LayerCape implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer playerRenderer;

    public LayerCape(RenderPlayer playerRendererIn) {
        this.playerRenderer = playerRendererIn;
    }

    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        if (entitylivingbaseIn.hasPlayerInfo() && !entitylivingbaseIn.isInvisible() && entitylivingbaseIn.getCommandSenderName().equals(Minecraft.getMinecraft().getSession().getUsername()) && entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE) ) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.playerRenderer.bindTexture(new ResourceLocation("lavender/cape/cape.png"));
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 0.0F, 0.125F);
            double d0 = entitylivingbaseIn.prevChasingPosX + (entitylivingbaseIn.chasingPosX - entitylivingbaseIn.prevChasingPosX) * (double)partialTicks - (entitylivingbaseIn.prevPosX + (entitylivingbaseIn.posX - entitylivingbaseIn.prevPosX) * (double)partialTicks);
            double d1 = entitylivingbaseIn.prevChasingPosY + (entitylivingbaseIn.chasingPosY - entitylivingbaseIn.prevChasingPosY) * (double)partialTicks - (entitylivingbaseIn.prevPosY + (entitylivingbaseIn.posY - entitylivingbaseIn.prevPosY) * (double)partialTicks);
            double d2 = entitylivingbaseIn.prevChasingPosZ + (entitylivingbaseIn.chasingPosZ - entitylivingbaseIn.prevChasingPosZ) * (double)partialTicks - (entitylivingbaseIn.prevPosZ + (entitylivingbaseIn.posZ - entitylivingbaseIn.prevPosZ) * (double)partialTicks);
            float f = entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks;
            double d3 = MathHelper.sin(f * 3.1415927F / 180.0F);
            double d4 = -MathHelper.cos(f * 3.1415927F / 180.0F);
            float f1 = (float)d1 * 10.0F;
            f1 = MathHelper.clamp_float(f1, -6.0F, 32.0F);
            float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
            float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
            if (f2 < 0.0F) {
                f2 = 0.0F;
            }

            if (f2 > 165.0F) {
                f2 = 165.0F;
            }

            if (f1 < -5.0F) {
                f1 = -5.0F;
            }

            float f4 = entitylivingbaseIn.prevCameraYaw + (entitylivingbaseIn.cameraYaw - entitylivingbaseIn.prevCameraYaw) * partialTicks;
            f1 += MathHelper.sin((entitylivingbaseIn.prevDistanceWalkedModified + (entitylivingbaseIn.distanceWalkedModified - entitylivingbaseIn.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4;
            if (entitylivingbaseIn.isSneaking()) {
                f1 += 25.0F;
                GL11.glTranslatef(0.0F, 0.142F, -0.0178F);
            }

            GL11.glRotatef(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            this.playerRenderer.getMainModel().renderCape(0.0625F);
            this.playerRenderer.bindTexture(new ResourceLocation("lavender/cape/overlay.png"));
            int color = ColorUtil.astolfoRainbow(50, 0.4F, 0.85f);

            float red = (float)(color >> 16 & 255) / 255.0F;
            float green = (float)(color >> 8 & 255) / 255.0F;
            float blue = (float)(color & 255) / 255.0F;
            GL11.glColor4f(red, green, blue, 0.3F);
            this.playerRenderer.getMainModel().renderCape(0.0625F);

            for(Iterator var31 = this.playerRenderer.getLayerRenderers().iterator(); var31.hasNext(); GlStateManager.resetColor()) {
                LayerRenderer layerRenderer = (LayerRenderer)var31.next();
                if (layerRenderer instanceof LayerArmorBase) {
                    ((LayerArmorBase)layerRenderer).renderGlintCape(entitylivingbaseIn, this.playerRenderer.mainModel, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale);
                }
            }

            GL11.glPopMatrix();
        }

    }

    public boolean shouldCombineTextures() {
        return false;
    }
}
