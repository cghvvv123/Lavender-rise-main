package com.alan.clients.domcer.packet.customskinloader;

import com.alan.clients.Client;
import com.alan.clients.domcer.CustomPacket;
import com.alan.clients.domcer.DomcerPacketManager;
import com.alan.clients.domcer.packet.uview.UViewPacket;
import com.alan.clients.domcer.utils.HttpUtils;
import com.alan.clients.module.impl.other.DomcerPatcher;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import org.apache.commons.compress.utils.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class CustonSkinLoaderPacket implements CustomPacket {

    @Override
    public String getChannel() {
        return "CustomSkinLoader";
    }

    @Override
    public void process(ByteBuf byteBuf) {

        ByteBuf buf = Unpooled.wrappedBuffer(UViewPacket.packetCloseGuiInstance());
        mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("UView", new PacketBuffer(buf)));
        System.out.println("Sent -> WangHang Fucker");



        ByteBuf buf1 = Unpooled.wrappedBuffer(DomcerPacketManager.clientID.getBytes());
        mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("CustomSkinLoader", new PacketBuffer(buf1)));


        System.out.println("Sent -> Fake Screen Shots");
        fakeFileAndScreenshot(DomcerPacketManager.clientID);
    }


    public void fakeFileAndScreenshot(String id) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            DomcerPacketManager.beforeScreenshot();
            try {
                DomcerPatcher domcerPatcher = Client.INSTANCE.getModuleManager().get(DomcerPatcher.class);

                byte[] bytes = getJPG(1);
                // Local: 读取本地图片
                // Runtime: 实时截图
                String url = "https://upload.server.domcer.com:25566/uploadJpg?key=fea2c199-0341-0fe8-c1f6-8ea2fd831b3b&type=" + id;
                HashMap<String, byte[]> map = new HashMap<String, byte[]>();
                map.put("file", bytes);
                map.put("check", IOUtils.toByteArray(Objects.requireNonNull(CustonSkinLoaderPacket.class.getResourceAsStream("/fileCheck.txt"))));
                    /*
                     读取md5文件
                     @see README.md
                    */
                HttpUtils.HttpResponse response = HttpUtils.postFormData(url, map, null, null);
                String result = response.getContent();
                ByteBuf buf = Unpooled.wrappedBuffer((id + ":" + ((JsonObject) new Gson().fromJson(result, JsonObject.class)).get("data").getAsString()).getBytes());

                mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("CustomSkinLoader", new PacketBuffer(buf)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            DomcerPacketManager.afterScreenshot();
        });

    }

    private IntBuffer pixelBuffer;
    private int[] pixelValues;

    public byte[] getJPG(int type) throws IOException {
        switch (type) {
            case 0: {
                BufferedImage bufferedimage = ImageIO.read(new File("C:/back1.jpg"));
                int width = bufferedimage.getWidth();
                int height = bufferedimage.getHeight();
                Random random = new Random();
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                int pixel = bufferedimage.getRGB(x, y);
                int newPixel = random.nextInt();
                bufferedimage.setRGB(x, y, newPixel);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                boolean foundWriter = ImageIO.write((RenderedImage)bufferedimage, "jpg", baos);
                assert (foundWriter);
                return baos.toByteArray();
            }
            case 1: {
                Framebuffer buffer = Minecraft.getMinecraft().getFramebuffer();
                int width1 = Minecraft.getMinecraft().displayWidth;
                int height1 = Minecraft.getMinecraft().displayHeight;
                if (OpenGlHelper.isFramebufferEnabled()) {
                    int width = buffer.framebufferTextureWidth;
                    int n = buffer.framebufferTextureHeight;
                }
                int i = width1 * height1;
                if (pixelBuffer == null || pixelBuffer.capacity() < i) {
                    pixelBuffer = BufferUtils.createIntBuffer(i);
                    pixelValues = new int[i];
                }
                GL11.glPixelStorei(3333, 1);
                GL11.glPixelStorei(3317, 1);
                pixelBuffer.clear();
                if (OpenGlHelper.isFramebufferEnabled()) {
                    GlStateManager.bindTexture(buffer.framebufferTexture);
                    GL11.glGetTexImage(3553, 0, 32993, 33639, pixelBuffer);
                } else {
                    GL11.glReadPixels(0, 0, width1, height1, 32993, 33639, pixelBuffer);
                }
                pixelBuffer.get(pixelValues);
                TextureUtil.processPixelValues(pixelValues, width1, height1);
                BufferedImage bufferedimage1 = null;
                if (OpenGlHelper.isFramebufferEnabled()) {
                    int j;
                    bufferedimage1 = new BufferedImage(buffer.framebufferWidth, buffer.framebufferHeight, 1);
                    for (int k = j = buffer.framebufferTextureHeight - buffer.framebufferHeight; k < buffer.framebufferTextureHeight; ++k) {
                        for (int l = 0; l < buffer.framebufferWidth; ++l) {
                            bufferedimage1.setRGB(l, k - j, pixelValues[k * buffer.framebufferTextureWidth + l]);
                        }
                    }
                } else {
                    BufferedImage bufferedimage = new BufferedImage(width1, height1, 1);
                    bufferedimage.setRGB(0, 0, width1, height1, pixelValues, 0, width1);
                }
                BufferedImage finalBufferedimage = bufferedimage1;
                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                boolean foundWriter1 = ImageIO.write((RenderedImage)finalBufferedimage, "jpg", baos1);
                assert (foundWriter1);
                return baos1.toByteArray();
            }
        }
        throw new UnexpectedException("Unknown Type");
    }

}
