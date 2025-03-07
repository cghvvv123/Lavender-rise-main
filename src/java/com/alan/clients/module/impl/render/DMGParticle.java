package com.alan.clients.module.impl.render;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.LivingUpdateEvent;
import com.alan.clients.newevent.impl.other.RespawnEvent;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.newevent.impl.render.Render3DEvent;
import com.alan.clients.util.Location;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.alan.clients.util.render.RenderUtil.disableGL2D;
import static com.alan.clients.util.render.RenderUtil.enableGL2D;

@ModuleInfo(name = "module.render.dmgparticle.name",description = "CNM",category = Category.RENDER)
public class DMGParticle extends Module {
    private HashMap<EntityLivingBase,Float> healthMap = new HashMap<>();
    private List<Particle> particles = new ArrayList<>();
    @EventLink()
    public final Listener<RespawnEvent> onRespawn = event -> {
        this.particles.clear();
        this.healthMap.clear();
    };
    @EventLink()
    public final Listener<TickEvent> onTick = event -> {
        List<Particle> particlesToRemove = new ArrayList<>();
        for (Particle particle : particles) {
            particle.ticks++;

            if (particle.ticks <= 10) {
                particle.location.setY(particle.location.getY() + particle.ticks * 0.005);
            }

            if (particle.ticks > 20) {
                particlesToRemove.add(particle);
            }
        }

        particles.removeAll(particlesToRemove);
    };
    @EventLink()
    public final Listener<LivingUpdateEvent> onLivingUpdate = e -> {
        final EntityLivingBase entity = (EntityLivingBase) e.getEntity();

        if (entity == this.mc.thePlayer)
            return;

        // detect

        if (!healthMap.containsKey(entity))
            healthMap.put(entity, entity.getHealth());

        final float before = healthMap.get(entity);
        final float after = entity.getHealth();

        if (before != after) {
            String text;

            if ((before - after) < 0) {
                text = EnumChatFormatting.GREEN + "" + roundToPlace((before - after) * -1, 1);
            } else {
                text = EnumChatFormatting.YELLOW + "" + roundToPlace((before - after), 1);
            }

            Location location = new Location(entity);

            location.setY(entity.getEntityBoundingBox().minY
                    + ((entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) / 2));

            location.setX((location.getX() - 0.5) + (new Random(System.currentTimeMillis()).nextInt(5) * 0.1));
            location.setZ((location.getZ() - 0.5) + (new Random(System.currentTimeMillis() + 1).nextInt(5) * 0.1));

            particles.add(new Particle(location, text));

            healthMap.remove(entity);
            healthMap.put(entity, entity.getHealth());
        }
    };
    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {
        for (Particle particle : this.particles) {
            final double x = particle.location.getX() - this.mc.getRenderManager().getRenderPosX();
            final double y = particle.location.getY() - this.mc.getRenderManager().getRenderPosY();
            final double z = particle.location.getZ() - this.mc.getRenderManager().getRenderPosZ();

            GlStateManager.pushMatrix();

            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0F, -1500000.0F);

            GlStateManager.translate((float) x, (float) y, (float) z);
            GlStateManager.rotate(-this.mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            float var10001 = this.mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
            GlStateManager.rotate(this.mc.getRenderManager().playerViewX, var10001, 0.0F, 0.0F);
            double scale = 0.03;
            GlStateManager.scale(-scale, -scale, scale);

            enableGL2D();
            disableGL2D();

            GL11.glDepthMask(false);
            mc.fontRendererObj.drawString(particle.text,
                    -(this.mc.fontRendererObj.width(particle.text) / 2),
                    -(this.mc.fontRendererObj.FONT_HEIGHT - 1), 0);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDepthMask(true);

            GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
            GlStateManager.disablePolygonOffset();

            GlStateManager.popMatrix();
        }
    };
    public static double roundToPlace(final double value, final int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
class Particle {
    public Particle(Location location, String text) {
        this.location = location;
        this.text = text;
        this.ticks = 0;
    }

    public int ticks;
    public Location location;
    public String text;
}
