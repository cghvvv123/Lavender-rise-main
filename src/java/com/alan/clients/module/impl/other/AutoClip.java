package com.alan.clients.module.impl.other;

import com.alan.clients.component.impl.player.BlinkComponent;
import com.alan.clients.component.impl.render.NotificationComponent;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.other.BlockAABBEvent;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.util.vector.Vector3d;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.SubMode;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockGlass;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

@ModuleInfo(name = "module.other.autoclip.name", description = "Auto Fly In com.alan.clients.hyt skywars", category = Category.PLAYER)
public class AutoClip  extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Normal"))
            .add(new SubMode("Boost"))
            .setDefault("Normal");
    private Vector3d startPlayer;
    private boolean phasing;
    private BlockPos startPos;
    private int boostTick;

    @Override
    public void onEnable() {
        if (mode.getValue().getName().equalsIgnoreCase("Normal")) {
            startPlayer = new Vector3d(InstanceAccess.mc.thePlayer.posX, InstanceAccess.mc.thePlayer.posY, InstanceAccess.mc.thePlayer.posZ);
            startPos = new BlockPos(InstanceAccess.mc.thePlayer).down();
            phasing = true;
            BlinkComponent.setExempt(C08PacketPlayerBlockPlacement.class);
            BlinkComponent.blinking = true;
        }

        boostTick = 0;
    }

    @EventLink
    private final Listener<WorldChangeEvent> onWorldChange = event -> {
        phasing = false;
        boostTick = 0;
    };

    @Override
    public void onDisable() {
        if (startPos != null && !(InstanceAccess.mc.theWorld.getBlockState(startPos).getBlock() instanceof BlockAir)) {
            BlinkComponent.packets.forEach(packet -> {
                if (packet instanceof C03PacketPlayer) {
                    final C03PacketPlayer wrapped = (C03PacketPlayer) packet;

                    if (wrapped.moving) {
                        wrapped.x = startPlayer.getX();
                        wrapped.y = startPlayer.getY();
                        wrapped.z = startPlayer.getZ();
                    }
                }

                InstanceAccess.mc.getNetHandler().addToSendQueueUnregistered(packet);
            });
            BlinkComponent.packets.clear();

            InstanceAccess.mc.thePlayer.setPosition(startPlayer.getX(), startPlayer.getY(), startPlayer.getZ());
        }
        BlinkComponent.blinking = false;
        startPos = null;
    }

    @EventLink
    private final Listener<BlockAABBEvent> onBlockAABB = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("Normal") && phasing)
            event.setBoundingBox(null);
    };

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("Normal")) {
            if (InstanceAccess.mc.thePlayer.posY + 3.1 < startPos.getY()) {
                phasing = false;

                if (InstanceAccess.mc.theWorld.getBlockState(startPos).getBlock() instanceof BlockAir) {
                    BlinkComponent.blinking = false;
                    toggle();
                    NotificationComponent.post("Phase", "Operation successful!");
                }
            }
        } else {
            if (!phasing) startPos = new BlockPos(InstanceAccess.mc.thePlayer).up(2);

            if (InstanceAccess.mc.theWorld.getBlockState(startPos).getBlock() instanceof BlockGlass) {
                if (!phasing) {
                    phasing = true;
                    BlinkComponent.setExempt(C08PacketPlayerBlockPlacement.class);
                    BlinkComponent.blinking = true;
                    boostTick = 0;
                    InstanceAccess.mc.thePlayer.setPosition(InstanceAccess.mc.thePlayer.posX, InstanceAccess.mc.thePlayer.posY + 3, InstanceAccess.mc.thePlayer.posZ);
                }
                boostTick++;

                if (boostTick == 5) {
                    MoveUtil.strafe(5);
                }

                if (boostTick == 1000) {
                    BlinkComponent.packets.clear();
                    BlinkComponent.blinking = false;
                    InstanceAccess.mc.thePlayer.sendChatMessage("/hub");
                }
            } else {
                BlinkComponent.blinking = false;
            }
        }
    };

}
