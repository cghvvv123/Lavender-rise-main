package com.alan.clients.util.packet;

import com.alan.clients.Client;
import com.alan.clients.module.impl.exploit.Disabler;
import com.alan.clients.module.impl.exploit.disabler.GrimACDisabler;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.math.MathUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

@UtilityClass
public final class PacketUtil implements InstanceAccess {
    public void send(final Packet<?> packet) {
        mc.getNetHandler().addToSendQueue(packet);
    }

    public void sendNoEvent(final Packet<?> packet) {
        mc.getNetHandler().addToSendQueueUnregistered(packet);
    }

    public void queue(final Packet<?> packet) {
        if (isServerPacket(packet)) {
            mc.getNetHandler().addToReceiveQueue(packet);
        } else {
            mc.getNetHandler().addToSendQueue(packet);
        }
    }
    public static void sendC0F(int windowId, short uid, boolean accepted, boolean silent) {
        if (silent) {
            sendNoEvent(new C0FPacketConfirmTransaction(windowId, uid, accepted));
        } else {
            send(new C0FPacketConfirmTransaction(windowId, uid, accepted));
        }

    }
    public void sendRandomC0F(boolean silent) {
        if(silent) {
            sendNoEvent(new C0FPacketConfirmTransaction((int) MathUtil.getRandom(1145, 1919), (short) MathUtil.getRandom(1145, 1919), true));
        } else {
            send(new C0FPacketConfirmTransaction((int) MathUtil.getRandom(1145, 1919), (short) MathUtil.getRandom(1145, 1919), true));
        }
    }

    public static void sendPacketC0F() {
        GrimACDisabler dis = (GrimACDisabler) Client.INSTANCE.getModuleManager().get(Disabler.class).grimac.getMode();
        if (!dis.getGrimPost()) {
            send(new C0FPacketConfirmTransaction(MathUtil.getRandom2(102, 1000024123), (short) MathUtil.getRandom(102, 1000024123), true));
        }
    }
    public void queueNoEvent(final Packet<?> packet) {
        if (isServerPacket(packet)) {
            mc.getNetHandler().addToReceiveQueueUnregistered(packet);
        } else {
            mc.getNetHandler().addToSendQueueUnregistered(packet);
        }
    }

    public void receive(final Packet<?> packet) {
        mc.getNetHandler().addToReceiveQueue(packet);
    }

    public void receiveNoEvent(final Packet<?> packet) {
        mc.getNetHandler().addToReceiveQueueUnregistered(packet);
    }

    public boolean isServerPacket(final Packet<?> packet) {
        return packet.toString().toCharArray()[34] == 'S';
    }

    private boolean isClientPacket(final Packet<?> packet) {
        return packet.toString().toCharArray()[34] == 'C';
    }

    public static class TimedPacket {
        private final Packet<?> packet;
        private final long time;

        public TimedPacket(final Packet<?> packet, final long time) {
            this.packet = packet;
            this.time = time;
        }

        public Packet<?> getPacket() {
            return packet;
        }

        public long getTime() {
            return time;
        }
    }
}
