package com.alan.clients.module.impl.combat;

import com.alan.clients.api.Rise;
import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.component.impl.player.rotationcomponent.MovementFix;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.other.Stuck;
import com.alan.clients.module.impl.player.Blink;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.Priorities;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.render.Render3DEvent;
import com.alan.clients.util.WorldUtil;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Rise
@ModuleInfo(name = "module.combat.autoball.name", description = "AutoBall By LaoShui", category = Category.COMBAT)
public final class AutoBall extends Module {

    public static String aim = null;
    private Vec3 aimed;

    private final BooleanValue headshot = new BooleanValue("Only Head", this, false);
    public final NumberValue range = new NumberValue("Range", this, 10, 3, 20, 0.1);
    public final NumberValue deviation = new NumberValue("Pre-Attack", this, 1.5, 0, 10, 0.1);
    public final NumberValue fovCheck = new NumberValue("FOV Check", this, 180, 1, 180, 1);
    public final NumberValue smoothness = new NumberValue("Rotation Smoothing", this, 60, 1, 100, 5);

    @EventLink(value = Priorities.LOW)
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (getModule(KillAura.class).target != null || getModule(Scaffold.class).isEnabled() || getModule(Blink.class).isEnabled() || getModule(Stuck.class).isEnabled()) return;

        // 收集所有有效实体并过滤为仅包括玩家
        final List<EntityLivingBase> targets = WorldUtil.getLivingEntities().stream()
                .filter(this::isValid) // 保留有效实体
                .filter(e -> e instanceof EntityPlayer) // 只包括玩家
                .sorted(Comparator.comparing(e -> mc.thePlayer.getDistanceToEntity(e))) // 按距离排序
                .collect(Collectors.toList());

        if (targets.isEmpty())
            return;

        // 取最接近的玩家
        EntityPlayer nearestPlayer = (EntityPlayer) targets.get(0);
        // 检查物品栏中是否有雪球或鸡蛋
        for (int i = 0; i < 9; i++) {
            ItemStack currentStack = mc.thePlayer.inventory.mainInventory[i];
            if (currentStack != null) {
                mc.thePlayer.inventory.currentItem = i;
                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
            }
        }
        // 定位和转头处理
        this.aimed = this.getFixedLocation(nearestPlayer, deviation.getValue().floatValue(), headshot.getValue());
        aim = String.valueOf(aimed);

        final float[] rotations = getRotationToLocation(this.aimed);

       /* mc.thePlayer.rotationYaw = (rotations[0]);
        mc.thePlayer.rotationPitch = (rotations[1]);

        */

        mc.thePlayer.rotationYawHead = rotations[0];
        RotationComponent.setRotations(new Vector2f(rotations[0], rotations[1]), 10,MovementFix.NORMAL);

    };

    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (this.aimed != null) {
            double posX = this.aimed.xCoord - mc.getRenderManager().renderPosX;
            double posY = this.aimed.yCoord - mc.getRenderManager().renderPosY;
            double posZ = this.aimed.zCoord - mc.getRenderManager().renderPosZ;
            drawBlockESP(posX - 0.5, posY - 1, posZ - 0.5, (new Color(255, 255, 255, 255)).getRGB(), (new Color(0x549DEC)).getRGB(), 0.4F, 0.1F);
        }
    };

    private Vec3 getFixedLocation(EntityLivingBase entity, float velocity, boolean head) {
        double x = entity.posX + (entity.posX - entity.lastTickPosX) * (double)velocity;
        double y = entity.posY + (entity.posY - entity.lastTickPosY) * (double)velocity * 0.3 + (head ? (double)entity.getEyeHeight() : 1.0);
        double z = entity.posZ + (entity.posZ - entity.lastTickPosZ) * (double)velocity;
        return new Vec3(x, y, z);
    }

    private boolean isValid(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            if (!entity.isDead && !(entity.getHealth() <= 0.0F)) {
                return !((double) mc.thePlayer.getDistanceToEntity(entity) > range.getValue().doubleValue()) && canEntityBeSeenFixed(entity);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static float[] getAngles(EntityLivingBase entity) {
        if (entity == null) return null;
        final EntityPlayerSP player = mc.thePlayer;

        final double diffX = entity.posX - player.posX,
                diffY = entity.posY + entity.getEyeHeight() * 0.9 - (player.posY + player.getEyeHeight()),
                diffZ = entity.posZ - player.posZ, dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ); // @on

        final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F,
                pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);

        return new float[]{player.rotationYaw + MathHelper.wrapAngleTo180_float(
                yaw - player.rotationYaw), player.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - player.rotationPitch)};
    }

    private boolean isInFOV(EntityLivingBase entity) {
        if (entity != null) {
            float[] rotationPosition = getAngles(entity);
            int pitchByPos = (int) rotationPosition[1], yawByPos = (int) rotationPosition[0], yaw = (int) mc.thePlayer.rotationYaw,
                    pitch = (int) mc.thePlayer.rotationPitch, differenceYaw = Math.abs(yaw - yawByPos), differencePitch = Math.abs(pitch - pitchByPos);

            return differenceYaw <= fovCheck.getValue().intValue() && differencePitch <= fovCheck.getValue().intValue();
        }

        return false;
    }

    public float[] getRotationToLocation(Vec3 loc) {
        double xDiff = loc.xCoord - mc.thePlayer.posX;
        double yDiff = loc.yCoord - (mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight());
        double zDiff = loc.zCoord - mc.thePlayer.posZ;
        double distance = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float[] rotations = new float[]{(float)(Math.atan2(zDiff, xDiff) * 180.0 / Math.PI) - 90.0F, (float)(-(Math.atan2(yDiff, distance) * 180.0 / Math.PI))};
        float frac = MathHelper.clamp_float(1.0f - (float) smoothness.getValue() / 100.0f, 0.1f, 0.5f);
        rotations[0] = mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(rotations[0] - mc.thePlayer.rotationYaw) * frac;
        rotations[1] = mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(rotations[1] - mc.thePlayer.rotationPitch) * frac;
        return rotations;
    }

    public boolean canEntityBeSeenFixed(Entity entityIn) {
        return mc.thePlayer.worldObj.rayTraceBlocks(
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(),
                mc.thePlayer.posZ), new Vec3(entityIn.posX, entityIn.posY + (double)entityIn.getEyeHeight(), entityIn.posZ)) == null ||
                mc.thePlayer.worldObj.rayTraceBlocks(new Vec3(mc.thePlayer.posX,
                mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ),
                        new Vec3(entityIn.posX, entityIn.posY, entityIn.posZ)) == null;
    }

    public static void drawBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawBlockESP(double x, double y, double z, int maincoolor, int borderColor, float alpha, float lineWidth) {
        float red = (float)(maincoolor >> 16 & 255) / 255.0F;
        float green = (float)(maincoolor >> 8 & 255) / 255.0F;
        float blue = (float)(maincoolor & 255) / 255.0F;
        float lineRed = (float)(borderColor >> 16 & 255) / 255.0F;
        float lineGreen = (float)(borderColor >> 8 & 255) / 255.0F;
        float lineBlue = (float)(borderColor & 255) / 255.0F;
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(lineRed, lineGreen, lineBlue, alpha);
        drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

}
