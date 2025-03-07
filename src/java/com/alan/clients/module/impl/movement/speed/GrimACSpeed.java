package com.alan.clients.module.impl.movement.speed;

import com.alan.clients.Client;
import com.alan.clients.module.impl.movement.Speed;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.util.chat.ChatUtil;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.Mode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;

public final class GrimACSpeed extends Mode<Speed> {
    public boolean s08 = false;
    private EntityChicken entityChicken;

    public GrimACSpeed(String name, Speed parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        if (entityChicken != null) {
            InstanceAccess.mc.theWorld.removeEntity(entityChicken);
            entityChicken = null;
        }
    }

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        if (isNull()) return;
        Packet<?> packet = event.getPacket();
        if (packet instanceof S08PacketPlayerPosLook) {
            s08 = true;
            ChatUtil.display( "S08 Flags");
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    s08 = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    };
    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (isNull()) return;
        if (isPlayerValid() && InstanceAccess.mc.thePlayer.hurtTime > 0) {
            return;
        }
        Scaffold Scaffold = Client.INSTANCE.getModuleManager().get(Scaffold.class);
        if (s08 || Scaffold.isEnabled()) return;
        if (InstanceAccess.mc.thePlayer.movementInput.moveForward == 0.0f && InstanceAccess.mc.thePlayer.movementInput.moveStrafe == 0.0f) {
            return;
        }
        int collisions = 0;
        AxisAlignedBB box = InstanceAccess.mc.thePlayer.boundingBox.expand(1.0, 1.0, 1.0);
        for (Entity entity : InstanceAccess.mc.theWorld.loadedEntityList) {
            AxisAlignedBB entityBox = entity.boundingBox;
            if (canCauseSpeed(entity) && box.intersectsWith(entityBox)) {
                collisions++;
            }
        }
        double yaw = Math.toRadians(InstanceAccess.mc.thePlayer.rotationYaw);
        double boost = 0.08 * collisions;
        InstanceAccess.mc.thePlayer.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
    };

    private boolean canCauseSpeed(Entity entity) {
        return entity != InstanceAccess.mc.thePlayer && entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand);
    }

    private boolean isPlayerValid() {
        return InstanceAccess.mc.thePlayer != null && InstanceAccess.mc.theWorld != null;
    }

}
