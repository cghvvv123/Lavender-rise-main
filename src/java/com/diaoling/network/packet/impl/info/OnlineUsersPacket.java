package com.diaoling.network.packet.impl.info;

import com.diaoling.network.buffer.PacketBuffer;
import com.diaoling.network.handler.ClientHandler;
import com.diaoling.network.info.record.OnlineUserInfo;
import com.diaoling.network.packet.Packet;
import com.diaoling.network.user.UserManager;
import com.diaoling.utils.misc.enums.ClientType;
import com.diaoling.utils.misc.enums.Rank;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * @author DiaoLing
 * @since 4/8/2024
 */

public class OnlineUsersPacket extends Packet {
    private List<OnlineUserInfo> onlineUsers;

    public OnlineUsersPacket() {
    }

    public OnlineUsersPacket(List<OnlineUserInfo> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeList(onlineUsers, (buffer, user) -> {
            buffer.writeEnum(user.getClient());
            buffer.writeString(user.getUsername());
            buffer.writeString(user.getInGameName());
            buffer.writeEnum(user.getRank());
        });
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.onlineUsers = buf.readList(buffer -> new OnlineUserInfo(
                buffer.readEnum(ClientType.class),
                buffer.readString(),
                buffer.readString(),
                buffer.readEnum(Rank.class)
        ));
    }

    @Override
    public void handler(ChannelHandlerContext ctx, ClientHandler handler) {
        UserManager.setOnlineUsers(getOnlineUsers());
    }

    public List<OnlineUserInfo> getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(List<OnlineUserInfo> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }
}