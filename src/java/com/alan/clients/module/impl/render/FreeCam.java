package com.alan.clients.module.impl.render;


import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.CancellableEvent;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.input.MoveInputEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.newevent.impl.other.BlockAABBEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.util.vector.Vector3d;
import com.alan.clients.value.impl.NumberValue;
import lombok.Getter;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;

@Getter
@ModuleInfo(name = "module.render.freecam.name", description = "module.render.freecam.description", category = Category.RENDER)
public final class FreeCam extends Module {

    private final NumberValue speed = new NumberValue("Speed", this, 1, 0.1, 9.5, 0.1);
    private Vector3d position, delta;
    private Vector2f rotation;
    private boolean sprinting;

    @Override
    protected void onEnable() {
        position = new Vector3d(InstanceAccess.mc.thePlayer.posX, InstanceAccess.mc.thePlayer.posY, InstanceAccess.mc.thePlayer.posZ);
        delta = new Vector3d(InstanceAccess.mc.thePlayer.motionX, InstanceAccess.mc.thePlayer.motionY, InstanceAccess.mc.thePlayer.motionZ);
        rotation = new Vector2f(InstanceAccess.mc.thePlayer.rotationYaw, InstanceAccess.mc.thePlayer.rotationPitch);
        sprinting = InstanceAccess.mc.gameSettings.keyBindSprint.isKeyDown();
    }

    @Override
    protected void onDisable() {
        InstanceAccess.mc.thePlayer.setPosition(position.getX(), position.getY(), position.getZ());
        InstanceAccess.mc.thePlayer.rotationYaw = rotation.getX();
        InstanceAccess.mc.thePlayer.rotationPitch = rotation.getY();
        InstanceAccess.mc.thePlayer.motionX = delta.getX();
        InstanceAccess.mc.thePlayer.motionY = delta.getY();
        InstanceAccess.mc.thePlayer.motionZ = delta.getZ();
        InstanceAccess.mc.gameSettings.keyBindSprint.setPressed(sprinting);
    }

    @EventLink
    public final Listener<BlockAABBEvent> blockAABBEventListener = CancellableEvent::setCancelled;

    @EventLink
    public final Listener<PacketSendEvent> send = event -> {
        Packet<?> packet = event.getPacket();

        if (packet instanceof C0APacketAnimation || packet instanceof C03PacketPlayer ||
                packet instanceof C02PacketUseEntity || packet instanceof C0BPacketEntityAction ||
                packet instanceof C08PacketPlayerBlockPlacement) {
            event.setCancelled();
        }
    };

    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {

        final float speed = this.speed.getValue().floatValue();

        event.setSpeed(speed);
    };

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        final float speed = this.speed.getValue().floatValue();

        InstanceAccess.mc.thePlayer.motionY = 0.0D
                + (InstanceAccess.mc.gameSettings.keyBindJump.isKeyDown() ? speed : 0.0D)
                - (InstanceAccess.mc.gameSettings.keyBindSneak.isKeyDown() ? speed : 0.0D);
    };


    @EventLink()
    public final Listener<MoveInputEvent> onMovementInput = event -> {
        event.setSneak(false);
    };
}
