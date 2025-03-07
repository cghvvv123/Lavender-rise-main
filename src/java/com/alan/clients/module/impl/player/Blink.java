package com.alan.clients.module.impl.player;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.newevent.impl.render.Render3DEvent;
import com.alan.clients.util.MSTimer;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Rise
@ModuleInfo(name = "module.player.blink.name", description = "Blink Fake Player",  category = Category.PLAYER)
public class Blink extends Module {
    final ConcurrentLinkedQueue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private final BooleanValue pulse = new BooleanValue("Pulse",this, false);
    private final BooleanValue CancelS32 = new BooleanValue("CancelS32",this, false);
    private final BooleanValue CancelServerpacket = new BooleanValue("CancelServerpacket",this, true);

    private final BooleanValue CancelC0f = new BooleanValue("CancelC0f",this, false);
    private final BooleanValue CancelC0fResend = new BooleanValue("CancelC0fResend",this, false);
    private final BooleanValue CancelAllCpacket = new BooleanValue("CancelAllCpacket",this, true);
    private final NumberValue delayPulse = new NumberValue("Tick Delay",this, 20, 4, 100, 1);
    private final LinkedList<Packet<INetHandlerPlayClient>> inBus = new LinkedList<>();
    Boolean disableLogger = false;
    private EntityOtherPlayerMP blinkEntity;
    List<Vec3> path = new ArrayList<>();
    private final MSTimer pulseTimer = new MSTimer();
    private final LinkedList<double[]> positions = new LinkedList<>();
    private EntityOtherPlayerMP fakePlayer = null;
    @Override
    protected void onEnable() {
        EntityPlayerSP thePlayer = mc.thePlayer;

        if (thePlayer == null) {
            return;
        }

        synchronized (positions) {
            double[] pos1 = new double[]{thePlayer.posX, thePlayer.getEntityBoundingBox().minY + thePlayer.getEyeHeight() / 2, thePlayer.posZ};
            double[] pos2 = new double[]{thePlayer.posX, thePlayer.getEntityBoundingBox().minY, thePlayer.posZ};
            positions.add(pos1);
            positions.add(pos2);
        }

        pulseTimer.reset();
        path.clear();
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        if (mc.thePlayer == null) {
            return;
        }
        blink();

        EntityOtherPlayerMP faker = fakePlayer;

        if (faker != null) {
            if (mc.theWorld != null) {
                mc.theWorld.removeEntityFromWorld(faker.getEntityId());
            }
            fakePlayer = null;
        }
        packets.forEach(PacketUtil::sendNoEvent);
        packets.clear();
        super.onDisable();
        super.onDisable();
    }
    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (!this.isNull()) {
                if (mc.thePlayer.ticksExisted < 50) return;

                if (mc.thePlayer.lastTickPosX != mc.thePlayer.posX || mc.thePlayer.lastTickPosY != mc.thePlayer.posY || mc.thePlayer.lastTickPosZ != mc.thePlayer.posZ) {
                    path.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
                }

                if (pulse.getValue()) {
                    while (path.size() > delayPulse.getValue().intValue()) {
                        path.remove(0);
                    }
                }

                if (pulse.getValue() && blinkEntity != null) {
                    mc.theWorld.removeEntityFromWorld(blinkEntity.getEntityId());
                }
        }
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (!this.isNull()) {
            PacketUtil.sendC0F(0, (short) 0, true, true);
            if (mc.thePlayer == null || mc.thePlayer.isDead || mc.isSingleplayer() || mc.thePlayer.ticksExisted < 50) {
                packets.clear();
                return;
            }
            if (event.getPacket() instanceof C03PacketPlayer || CancelS32.getValue() && event.getPacket() instanceof C0FPacketConfirmTransaction) // Cancel all movement stuff
                event.setCancelled();
            if (event.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition || event.getPacket() instanceof C03PacketPlayer.C06PacketPlayerPosLook ||
                    event.getPacket() instanceof C0APacketAnimation ||
                    event.getPacket() instanceof C0BPacketEntityAction || event.getPacket() instanceof C02PacketUseEntity || CancelAllCpacket.getValue() || event.getPacket().getClass().getSimpleName().startsWith("C", 1)
            ) {
                event.setCancelled();
                packets.add(event.getPacket());
            }
            if (event.getPacket() instanceof C0FPacketConfirmTransaction && CancelC0f.getValue()) {
                event.setCancelled();
                if (CancelC0fResend.getValue()) {
                    packets.add(event.getPacket());
                }
            }
            if (event.getPacket().getClass().getSimpleName().startsWith("S", 1) && CancelServerpacket.getValue()) {
                if (event.getPacket() instanceof S12PacketEntityVelocity && (mc.theWorld.getEntityByID(((S12PacketEntityVelocity) event.getPacket()).getEntityID()) == mc.thePlayer)) {
                    return;
                }
                event.setCancelled();
                inBus.add((Packet<INetHandlerPlayClient>) event.getPacket());
            }
            }
        };
    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (!this.isNull()) {
            int color = Color.BLUE.getRGB();
            synchronized (positions) {
                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                mc.entityRenderer.disableLightmap();
                GL11.glLineWidth(2F);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                ColorUtil.glColor(color);
                double renderPosX = mc.getRenderManager().viewerPosX;
                double renderPosY = mc.getRenderManager().viewerPosY;
                double renderPosZ = mc.getRenderManager().viewerPosZ;
                for (double[] pos : positions) {
                    GL11.glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ);
                }
                GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
                GL11.glEnd();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glPopMatrix();
            }
        }
    };
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        EntityPlayerSP thePlayer = mc.thePlayer;
        if (thePlayer == null) {
            return;
        }

        synchronized (positions) {
            double[] pos = new double[]{thePlayer.posX, thePlayer.getEntityBoundingBox().minY, thePlayer.posZ};
            positions.add(pos);
        }

        if (pulse.getValue() && pulseTimer.hasTimePassed(delayPulse.getValue().intValue())) {
            blink();
            pulseTimer.reset();
        }
    };
    private void blink() {
        try {
            disableLogger = true;

            while (!packets.isEmpty()) {

                mc.getNetHandler().getNetworkManager().sendPacket((Packet) packets);
            }

            while (!inBus.isEmpty()) {
                Packet<INetHandlerPlayClient> packet = inBus.poll();
                if (packet != null) {
                    packet.processPacket(mc.getNetHandler());
                }
            }

            disableLogger = false;
        } catch (Exception e) {
            e.printStackTrace();
            disableLogger = false;
        }

        synchronized (positions) {
            positions.clear();
        }
    }
}
