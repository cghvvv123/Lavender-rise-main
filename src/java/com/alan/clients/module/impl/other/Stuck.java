package com.alan.clients.module.impl.other;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.packet.PacketUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import javax.vecmath.Vector2f;


@ModuleInfo(name = "module.other.stuck.name", description = "NoC03", category = Category.PLAYER)
public class Stuck extends Module {
    private double x;
    private double y;
    private double z;
    private boolean onGround;
    private Vector2f rotation;
    @Override
    protected void onEnable() {
        this.onGround = InstanceAccess.mc.thePlayer.onGround;
        this.x = Stuck.mc.thePlayer.posX;
        this.y = Stuck.mc.thePlayer.posY;
        this.z = Stuck.mc.thePlayer.posZ;
        this.rotation = new Vector2f(Stuck.mc.thePlayer.rotationYaw, Stuck.mc.thePlayer.rotationPitch);
        final float f = Stuck.mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final float gcd = f * f * f * 1.2f;
        final Vector2f rotation = this.rotation;
        rotation.x -= this.rotation.x % gcd;
        final Vector2f rotation2 = this.rotation;
        rotation2.y -= this.rotation.y % gcd;
    }
    @EventLink()
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            final Vector2f current = new Vector2f(Stuck.mc.thePlayer.rotationYaw, Stuck.mc.thePlayer.rotationPitch);
            final float f = Stuck.mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            final float gcd = f * f * f * 1.2f;
            current.x -= current.x % gcd;
            current.y -= current.y % gcd;
            if (this.rotation.equals(current)) {
                return;
            }
            this.rotation = current;
            event.setCancelled(true);
            PacketUtil.sendNoEvent(new C03PacketPlayer.C05PacketPlayerLook(current.x, current.y, this.onGround));
            PacketUtil.sendNoEvent(new C08PacketPlayerBlockPlacement(InstanceAccess.mc.thePlayer.getHeldItem()));
        }
        if (event.getPacket() instanceof C03PacketPlayer) {
            event.setCancelled(true);
        }
    };

    @EventLink()
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            setEnabled(false);
        }
    };

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
         Stuck.mc.thePlayer.motionX = 0.0;
        Stuck.mc.thePlayer.motionY = 0.0;
        Stuck.mc.thePlayer.motionZ = 0.0;
        Stuck.mc.thePlayer.setPosition(this.x, this.y, this.z);
    };

    public void throwPearl(final Vector2f current) {
        if (!getModule(Stuck.class).isEnabled()) {
            return;
        }
        Stuck.mc.thePlayer.rotationYaw = current.x;
        Stuck.mc.thePlayer.rotationPitch = current.y;

        final float f = Stuck.mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final float gcd = f * f * f * 1.2f;

        current.x = (float) (Math.floor(current.x / gcd) * gcd);
        current.y = (float) (Math.floor(current.y / gcd) * gcd);

        if (!rotation.equals(current)) {
            PacketUtil.sendNoEvent(new C03PacketPlayer.C05PacketPlayerLook(current.x, current.y, onGround));
        }

        rotation = current;
        PacketUtil.sendNoEvent(new C08PacketPlayerBlockPlacement(Stuck.mc.thePlayer.getHeldItem()));
    }


}
