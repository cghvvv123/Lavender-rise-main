package com.alan.clients.domcer;

import com.alan.clients.domcer.packet.customskinloader.CustonSkinLoaderPacket;
import com.alan.clients.domcer.packet.uview.UViewPacket;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashSet;
import java.util.UUID;

public class DomcerPacketManager {
    public final HashSet<CustomPacket> packets = new HashSet<>();
    public static String clientID;
    public void init() {
        packets.add(new CustonSkinLoaderPacket());
        packets.add(new UViewPacket());

        File file = new File("C://CustomSkinAPIPlus-ClientID");
        if (file.isFile()) {
            try {
                clientID = org.apache.commons.io.FileUtils.readFileToString(file, "UTF-8");
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (StringUtils.isEmpty(clientID)) {
            clientID = UUID.randomUUID().toString();
            try {
                org.apache.commons.io.FileUtils.write(file, clientID, "UTF-8");
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void beforeScreenshot() {
        // TODO
        Minecraft.getMinecraft().gameSettings.hideGUI = true;
    }
    /**
     * @author ImFl0wow
     */
    public static void afterScreenshot() {
        // TODO
        Minecraft.getMinecraft().gameSettings.hideGUI = false;
    }
}
