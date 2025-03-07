package com.alan.clients.module.impl.render;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.newevent.impl.render.Render3DEvent;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.NumberValue;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;

@Rise
@ModuleInfo(name = "module.render.offscreenesp.name", description = "module.render.offscreenesp.description", category = Category.RENDER)
public final class OffscreenESP extends Module {
    private final NumberValue size = new NumberValue("Size",this, 10.0D, 5.0D, 25.0D, 1.0D);
    private final NumberValue radius = new NumberValue("Radius",this, 45.0D, 10.0D, 200.0D, 1.0D);
    private final BooleanValue fade = new BooleanValue("Fade",this,true);
    private int alpha;
    private boolean plus_or_minus;
    private final OffscreenESP.EntityListener entityListener = new OffscreenESP.EntityListener();
    public void onEnable() {
        this.alpha = 0;
        this.plus_or_minus = false;
        super.onEnable();
    }
    @EventLink()
    public final Listener<Render3DEvent> onRender3D = this.entityListener::render3d;
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if ((Boolean)this.fade.getValue()) {
            float speed = 0.0025F;
            if ((float)this.alpha <= 60.0F || (float)this.alpha >= 255.0F) {
                this.plus_or_minus = !this.plus_or_minus;
            }

            this.alpha = this.plus_or_minus ? (int)((float)this.alpha + speed) : (int)((float)this.alpha - speed);
            this.alpha = (int)clamp((double)this.alpha, 60.0D, 255.0D);
        } else {
            this.alpha = 255;
        }

        mc.theWorld.loadedEntityList.forEach((o) -> {
            if (o instanceof EntityPlayer) {
                EntityPlayer entity = (EntityPlayer)o;
                Vec3 pos = (Vec3)this.entityListener.getEntityLowerBounds().get(entity);
                if (pos != null && !this.isOnScreen(pos)) {
                    int x = Display.getWidth() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);
                    int y = Display.getHeight() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);
                    float yaw = this.getRotations(entity) - mc.thePlayer.rotationYaw;
                    GL11.glTranslatef((float)x, (float)y, 0.0F);
                    GL11.glRotatef(yaw, 0.0F, 0.0F, 1.0F);
                    GL11.glTranslatef((float)(-x), (float)(-y), 0.0F);
                    RenderUtil.drawTracerPointer((float)x, (float)y - ((Double)this.radius.getValue()).floatValue(), ((Double)this.size.getValue()).floatValue(), 2.0F, 1.0F, this.getColor(entity, this.alpha).getRGB());
                    GL11.glTranslatef((float)x, (float)y, 0.0F);
                    GL11.glRotatef(-yaw, 0.0F, 0.0F, 1.0F);
                    GL11.glTranslatef((float)(-x), (float)(-y), 0.0F);
                }
            }

        });
    };
    private boolean isOnScreen(Vec3 pos) {
        return pos.xCoord > -1.0D && pos.zCoord < 1.0D && pos.xCoord / (double)(mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale) >= 0.0D && pos.xCoord / (double)(mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale) <= (double) Display.getWidth() && pos.yCoord / (double)(mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale) >= 0.0D && pos.yCoord / (double)(mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale) <= (double)Display.getHeight();
    }

    private float getRotations(EntityLivingBase ent) {
        double x = ent.posX - mc.thePlayer.posX;
        double z = ent.posZ - mc.thePlayer.posZ;
        float yaw = (float)(-(Math.atan2(x, z) * 57.29577951308232D));
        return yaw;
    }

    private Color getColor(EntityLivingBase player, int alpha) {
        float f = mc.thePlayer.getDistanceToEntity(player);
        float f2 = 40.0F;
        float f3 = Math.max(0.0F, Math.min(f, 40.0F) / 40.0F);
        Color clr = new Color(Color.HSBtoRGB(f3 / 3.0F, 1.0F, 1.0F) | -16777216);
        return new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), alpha);
    }

    public static double clamp(double value, double minimum, double maximum) {
        return value > maximum ? maximum : Math.max(value, minimum);
    }

    public static class EntityListener {
        private final Map<Entity, Vec3> entityUpperBounds = Maps.newHashMap();
        private final Map<Entity, Vec3> entityLowerBounds = Maps.newHashMap();

        private void render3d(Render3DEvent event) {
            if (!this.entityUpperBounds.isEmpty()) {
                this.entityUpperBounds.clear();
            }

            if (!this.entityLowerBounds.isEmpty()) {
                this.entityLowerBounds.clear();
            }

            Iterator var2 = Module.mc.theWorld.loadedEntityList.iterator();

            while(var2.hasNext()) {
                Entity e = (Entity)var2.next();
                Vec3 bound = this.getEntityRenderPosition(e);
                bound.add(new Vec3(0.0D, (double)e.height + 0.2D, 0.0D));
                Vec3 upperBounds = RenderUtil.to2D(bound.xCoord, bound.yCoord, bound.zCoord);
                Vec3 lowerBounds = RenderUtil.to2D(bound.xCoord, bound.yCoord - 2.0D, bound.zCoord);
                if (upperBounds != null && lowerBounds != null) {
                    this.entityUpperBounds.put(e, upperBounds);
                    this.entityLowerBounds.put(e, lowerBounds);
                }
            }

        }

        private Vec3 getEntityRenderPosition(Entity entity) {
            double partial = (double)Module.mc.timer.renderPartialTicks;
            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial - mc.getRenderManager().viewerPosX;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial - mc.getRenderManager().viewerPosY;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial - mc.getRenderManager().viewerPosZ;
            return new Vec3(x, y, z);
        }

        public Map<Entity, Vec3> getEntityLowerBounds() {
            return this.entityLowerBounds;
        }
    }
}
