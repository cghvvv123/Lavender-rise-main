package com.alan.clients.domcer.packet.uview;

import com.alan.clients.domcer.CustomPacket;
import com.alan.clients.domcer.utils.StreamUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.SerializationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
public class UViewPacket implements CustomPacket {
    private static final Logger LOGGER = Logger.getLogger("UView");
    @Override
    public String getChannel() {
        return "UView";
    }

    @Override
    public void process(ByteBuf byteBuf) {
    }

    public static byte[] packetCloseGuiInstance()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream(64);
        try
        {
            StreamUtils.writeString(out, "PacketCloseGui");
            return out.toByteArray();
        }
        catch (IOException ex)
        {
            throw new SerializationException(ex.getMessage(), ex);
        }
    }
}
