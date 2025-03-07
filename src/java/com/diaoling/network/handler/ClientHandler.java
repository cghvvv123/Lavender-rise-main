package com.diaoling.network.handler;

import com.alan.clients.Client;
import com.alan.clients.util.web.Browser;
import com.diaoling.network.packet.Packet;
import com.diaoling.network.packet.impl.info.UserInfoPacket;
import com.diaoling.utils.misc.enums.ClientType;
import com.diaoling.utils.misc.enums.Rank;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;

import java.net.SocketException;

/**
 * @author DiaoLing
 * @since 4/7/2024
 */
public class ClientHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        LogManager.getLogger().info("Received packet: " + packet.getClass().getSimpleName());

        packet.handler(ctx, this);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogManager.getLogger().info("Connected to server: " + ctx.channel().remoteAddress());
        if (Client.name.equals("Lkk")) {
            ctx.writeAndFlush(new UserInfoPacket(
                    ClientType.LAVENDER,
                    0,
                    EnumChatFormatting.LIGHT_PURPLE + "[" + Client.location + "人" + "] " + EnumChatFormatting.RESET + Client.name,
                    Rank.FEMBOY,
                    0,
                    114514
            ));
        }else if (Browser.get("https://gitcode.net/m0_74037382/emperor/-/raw/master/TesterList").contains(Client.name)){
            ctx.writeAndFlush(new UserInfoPacket(
                    ClientType.LAVENDER,
                    0,
                    EnumChatFormatting.LIGHT_PURPLE + "[" + Client.location + "人" + "] " + EnumChatFormatting.RESET + Client.name,
                    Rank.TESTER,
                    0,
                    114514 ));
        }
        else if (Browser.get("https://gitcode.net/m0_74037382/emperor/-/raw/master/DevList").contains(Client.name)){
            ctx.writeAndFlush(new UserInfoPacket(
            ClientType.LAVENDER,
                    0,
                    EnumChatFormatting.LIGHT_PURPLE + "[" + Client.location + "人" + "] " + EnumChatFormatting.RESET + Client.name,
                    Rank.DEV,
                    0,
                    114514 ));
        }else{
            ctx.writeAndFlush(new UserInfoPacket(
                    ClientType.LAVENDER,
                    0,
                    EnumChatFormatting.LIGHT_PURPLE + "[" + Client.location + "人" + "] " + EnumChatFormatting.RESET + Client.name,
                    Rank.USER,
                    0,
                    114514 ));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogManager.getLogger().warn("Disconnected from server.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SocketException) {
            LogManager.getLogger().error("Connection reset by peer or server shutdown.");
        } else {
            cause.printStackTrace();
        }
        ctx.close();
    }
}