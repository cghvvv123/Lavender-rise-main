package com.alan.clients.module.impl.movement.noslow;

import com.alan.clients.module.impl.movement.NoSlow;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PostMotionEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.motion.SlowDownEvent;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.ItemUtil;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockContainer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.concurrent.LinkedBlockingQueue;


public class HuaYuTingNoSlow extends Mode<NoSlow> {
    private final BooleanValue food = new BooleanValue("Food", this, true);
    public final BooleanValue bow = new BooleanValue("Bow", this, true);

    public HuaYuTingNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }
    private boolean var0 = false;
    private final LinkedBlockingQueue<Packet<?>> var4 = new LinkedBlockingQueue<>();
    private boolean droppedPacketSent = false;

    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        if (!isNull()) {
            droppedPacketSent = false;
        }
    };

    @EventLink
    public final Listener<SlowDownEvent> onSlowDown = event -> {
        if (isNull()) return;
        if (InstanceAccess.mc.thePlayer.getHeldItem() == null) return;
        if ((InstanceAccess.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) && InstanceAccess.mc.thePlayer.isUsingItem()) {
            event.setCancelled(true);
        }
        if ((InstanceAccess.mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) && bow.getValue() && InstanceAccess.mc.thePlayer.isUsingItem()) {
            event.setCancelled(true);
        }
    };


    @EventLink()
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (isNull()) return;
        if (InstanceAccess.mc.thePlayer.getHeldItem() == null) return;

        if ((InstanceAccess.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) && InstanceAccess.mc.thePlayer.isUsingItem()) {
            InstanceAccess.mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(InstanceAccess.mc.thePlayer.inventory.currentItem % 8 + 1));
            InstanceAccess.mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("MadeByFire", new PacketBuffer(Unpooled.buffer())));
            InstanceAccess.mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(InstanceAccess.mc.thePlayer.inventory.currentItem));
        }
        if ((InstanceAccess.mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) && bow.getValue() && InstanceAccess.mc.thePlayer.isUsingItem()) {
            InstanceAccess.mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(InstanceAccess.mc.thePlayer.inventory.currentItem % 8 + 1));
            InstanceAccess.mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("MadeByFire", new PacketBuffer(Unpooled.buffer())));
            InstanceAccess.mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(InstanceAccess.mc.thePlayer.inventory.currentItem));
        }
    };

    @EventLink()
    private final Listener<PostMotionEvent> onPostMotion = event -> {
        if (isNull()) return;
        if (InstanceAccess.mc.thePlayer.getHeldItem() == null) return;
        if (InstanceAccess.mc.thePlayer.getHeldItem() != null && (InstanceAccess.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword || (InstanceAccess.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && bow.getValue())) && InstanceAccess.mc.thePlayer.isUsingItem()) {
            PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
            useItem.write(Type.VAR_INT, 1);
            PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
            PacketWrapper useItem2 = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
            useItem2.write(Type.VAR_INT, 0);
            PacketUtil.sendToServer(useItem2, Protocol1_8To1_9.class, true, true);
        }
    };
    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (isNull()) return;
        if (this.droppedPacketSent) {
            InstanceAccess.mc.gameSettings.keyBindUseItem.setPressed(false);
        }

        if (!InstanceAccess.mc.thePlayer.isUsingItem()) {
            this.droppedPacketSent = false;
        }
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (isNull()) return;
        Packet<?> packet = event.getPacket();

        if (packet instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement wrapper = (C08PacketPlayerBlockPlacement) packet;
            if (this.food.getValue() && wrapper.getStack() != null && !(InstanceAccess.mc.theWorld.getBlockState(wrapper.getPosition()).getBlock() instanceof BlockContainer) && wrapper.getStack().getItem() instanceof ItemFood && InstanceAccess.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && !this.droppedPacketSent && wrapper.getStack().getStackSize() > 1 && !ItemUtil.isEnchantedGoldenApple(InstanceAccess.mc.thePlayer.getHeldItem())) {
                event.setCancelled(true);
                com.alan.clients.util.packet.PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                com.alan.clients.util.packet.PacketUtil.sendNoEvent(new C08PacketPlayerBlockPlacement(wrapper.getStack()));
                this.droppedPacketSent = true;
            }
        }

        if (this.food.getValue() && this.droppedPacketSent && packet instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging wrapper = (C07PacketPlayerDigging) packet;
            if (wrapper.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                event.setCancelled(true);
            }
        }
    };

}
