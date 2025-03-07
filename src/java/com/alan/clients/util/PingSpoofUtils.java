package com.alan.clients.util;

import com.viaversion.viaversion.bukkit.listeners.UpdateListener;
import net.minecraft.network.Packet;

import java.util.HashMap;

import static com.alan.clients.util.interfaces.InstanceAccess.mc;

public class PingSpoofUtils  {
    private static final HashMap<Packet, Long> packetQueue = new HashMap<>();
    private static boolean isPingSpoof = false;
    private static boolean isPingSpoofSendPacket = true;
    private static int maxDelay = 0;
    private static int minDelay = 0;


    public static void setDelay(int max, int min) {
        maxDelay = max;
        minDelay = min;
    }

    public static void setPingSpoof(boolean isStart) {
        isPingSpoof = isStart;
    }

    public static void onUpdate(UpdateListener event) {
        if (!isPingSpoof) packetQueue.clear();
        if (isPingSpoof && isPingSpoofSendPacket) {
            synchronized (packetQueue) {
                packetQueue.entrySet().removeIf(entry -> entry.getValue() >= System.currentTimeMillis());

                packetQueue.forEach((packet, time) -> {
                    mc.getNetHandler().addToSendQueue(packet);
                    packetQueue.remove(packet, time);
                });
            }
        }
    }

}
