package com.alan.clients.module.impl.other;

import com.alan.clients.fontRender.FontManager;
import com.alan.clients.fontRender.RapeMasterFontManager;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.noti.NotificationManager;
import com.alan.clients.noti.NotificationType;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.DragValue;
import com.alan.clients.value.impl.NumberValue;
import com.alan.clients.value.impl.StringValue;
import javafx.embed.swing.JFXPanel;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;

@ModuleInfo(name = "SSEMusicPlayer", category = Category.OTHER, description = "By LaoShui")
public class SSEMusicPlayer extends Module {
    // 抄袭SM
    private final StringValue ip = new StringValue("IP", this, "127.0.0.1");
    private final StringValue port = new StringValue("Port", this, "23330");
    public final NumberValue PosY = new NumberValue("MusicPlayerLyricY", this, 120d, 0d, 200d, 1d);
    private final DragValue position = new DragValue("Position", this, new Vector2d(200, 200));
    private final BooleanValue gradient = new BooleanValue("Gradient", this, true);

    private final RapeMasterFontManager font = FontManager.arial18bold;
    private String status = "";
    private String lyric = "";
    private String expandLyric = "";
    private String musicName = "";
    private String lastmusicName = "";
    private String singer = "";
    private String albumName = "";
    private String progress = "";
    private String duration = "";
    private String playbackRate = "";
    private String picUrl = " ";
    private final File artPicFolder = new File(mc.mcDataDir, ".cache/artCache");
    private final HashMap<Long, ResourceLocation> artsLocations = new HashMap<>();

    private Thread sseThread;
    private boolean isthreadcreated = false;

    public static long getMusicId(String musicname) {
        try {
            // 使用SHA-1算法生成哈希值
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(musicname.getBytes(StandardCharsets.UTF_8));

            // 将哈希值转换为正整数（long类型）
            BigInteger hashBigInt = new BigInteger(1, hashBytes);

            return hashBigInt.longValue();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    public void loadimage() {
        if (artsLocations.containsKey(getMusicId(musicName+singer))) {
            return;
        }
        File path = new File(artPicFolder.getAbsolutePath() + File.separator + getMusicId(musicName+singer));
        if (!path.exists()){
            this.makeimage(128, artPicFolder.getAbsolutePath() + File.separator + getMusicId(musicName+singer));
        }
        ResourceLocation rl = new ResourceLocation("music/" + getMusicId(musicName+singer));
        IImageBuffer iib = new IImageBuffer() {

            public BufferedImage parseUserSkin(BufferedImage image) {
                return image;
            }

            @Override
            public void skinAvailable() {
                artsLocations.put(getMusicId(musicName+singer), rl);
            }
        };

        ThreadDownloadImageData textureArt = new ThreadDownloadImageData(path, null, null, iib);
        mc.getTextureManager().loadTexture(rl, textureArt);
    }

    public void makeimage(int wid, String path) {
        try {
            BufferedImage avatarImage;
            if (picUrl.startsWith("http")) {
                // 如果是以http开头，就从网络下载图片
                avatarImage = ImageIO.read(new URL(picUrl));
            } else if (picUrl.startsWith("data:image/")) {
                // 如果是base64编码的图片数据，进行解码
                String base64Image = picUrl.split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                avatarImage = ImageIO.read(bis);
                bis.close();
            } else {
                throw new IllegalArgumentException("Invalid picture URL format.");
            }

            BufferedImage formatAvatarImage = new BufferedImage(wid, wid, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D graphics = formatAvatarImage.createGraphics();
            {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                int border = 0;
                Rectangle2D.Double shape = new Rectangle2D.Double(border, border, wid - border * 2, wid - border * 2);
                graphics.setClip(shape);
                graphics.drawImage(avatarImage, border, border, wid - border * 2, wid - border * 2, null);
                graphics.dispose();
            }

            try (OutputStream os = Files.newOutputStream(Paths.get(path))) {
                ImageIO.write(formatAvatarImage, "png", os);
            } catch (Exception ignored) {
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public ResourceLocation getArt(long id) {
        return artsLocations.get(id);
    }

    @Override
    public void onEnable() {
        status = "";
        lyric = "";
        expandLyric = "";
        musicName = "";
        lastmusicName = "";
        progress = "";
        duration = "";
        playbackRate = "";
    }

    @Override
    public void onDisable() {
        lyric = "";
        expandLyric = "";
        musicName = "";
        lastmusicName = "";
        progress = "";
        duration = "";
        playbackRate = "";

        if (sseThread != null && sseThread.isAlive()) {
            sseThread.interrupt(); // Interrupt the thread when disabling the module
            isthreadcreated = false;
        }
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (!isthreadcreated) {
            sseThread = new Thread(this::run);

            sseThread.start(); // Start the thread to listen to the SSE stream
            isthreadcreated = true;
        }

        ScaledResolution sr = new ScaledResolution(InstanceAccess.mc);
        RapeMasterFontManager lyricFont = FontManager.arial22bold;
        int addonYlyr = PosY.getValue().intValue();

        int borderCol = new Color(238, 171, 227).getRGB();
        int col = new Color(0xffE8DEFF).getRGB();

        lyricFont.drawCenterOutlinedString(lyric, sr.getScaledWidth() / 2f, sr.getScaledHeight() - 140 - 80 + addonYlyr, borderCol, col);
        if (Objects.equals(status, "playing") && Objects.equals(lyric, "") && Objects.equals(expandLyric, "")) {
            lyricFont.drawCenterOutlinedString("识别为间奏", sr.getScaledWidth() / 2f, sr.getScaledHeight() - 140 - 80 + addonYlyr, borderCol, col);
        }
        lyricFont.drawCenterOutlinedString(expandLyric, sr.getScaledWidth() / 2f, sr.getScaledHeight() - 120 + 0.5f - 80 + addonYlyr, new Color(0x595959).getRGB(), col);

        if (!Objects.equals(lastmusicName, musicName)) {
            lastmusicName = musicName;
            NotificationManager.post(NotificationType.INFO, "MusicPlayer", "Now Playing: " + musicName + " " + singer, 15);
            loadimage();
        }

        String blockRateText;
        try {
            int progressInt = Integer.parseInt(progress);
            int durationInt = Integer.parseInt(duration);
            blockRateText = "正在播放：" + musicName + "   " + convertSecondsToHMS(progressInt) + "/" + convertSecondsToHMS(durationInt);
        } catch (NumberFormatException e) {
            blockRateText = "等待解析";
        }

        float widthblockRate = font.getStringWidth(blockRateText);
        float widthplaybackRate = font.getStringWidth("当前播放速率:" + playbackRate + "x") + 40;
        float widthsinger = font.getStringWidth("歌手:" + singer) + 40;
        float widthalbumName = font.getStringWidth("专辑:" + albumName) + 40;
        float width = Math.max(widthblockRate, Math.max(widthplaybackRate, Math.max(widthsinger, widthalbumName)));

        this.position.setScale(new Vector2d(width + 50, 68));

        float progressValue;
        float durationValue;
        try {
            progressValue = Float.parseFloat(progress);
            durationValue = Float.parseFloat(duration);
        } catch (NumberFormatException e) {
            progressValue = 0;
            durationValue = 1; // 设置为1，避免除以0
        }

        float progressWidthTarget = Math.min(1.0f, progressValue / durationValue) * width;

        // 绘制进度条和背景矩形
        String finalBlockRateText = blockRateText;
        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            if (mc.currentScreen instanceof GuiChat) {
                com.alan.clients.util.font.FontManager.getProductSansLight(20).drawString("MusicPlayerHud", position.position.x + 1, position.position.y - 46,new Color(255,255,255,200).getRGB());
            }

            RenderUtil.roundedRectangle(position.position.x, position.position.y - 35, width + 50, 108, 4, getTheme().getBackgroundShade());

            // 绘制背景矩形
            RenderUtil.roundedRectangle(position.position.x + 24, position.position.y + 60, width + 5, 4, 2, getTheme().getBackgroundShade());

            // 绘制进度条
            if (gradient.getValue()) {
                RenderUtil.drawRoundedGradientRect(position.position.x + 24, position.position.y + 60, progressWidthTarget, 4, 2, getTheme().getFirstColor(), getTheme().getSecondColor(), false);
            } else {
                RenderUtil.roundedRectangle(position.position.x + 24, position.position.y + 60, progressWidthTarget, 4, 2, new Color(255,255,255,200));
            }

            // 绘制文本信息
            font.drawStringWithShadow(finalBlockRateText, position.position.x + 25, position.position.y + 49f, new Color(255, 255, 255, 255).getRGB());
            font.drawStringWithShadow("当前播放速率:" + playbackRate + "x", position.position.x + 80, position.position.y - 25, new Color(255, 255, 255, 255).getRGB());
            font.drawStringWithShadow("歌手:" + singer, position.position.x + 80, position.position.y - 15, new Color(255, 255, 255, 255).getRGB());
            font.drawStringWithShadow("歌名:" + musicName, position.position.x + 80, position.position.y - 5, new Color(255, 255, 255, 255).getRGB());
            if (!Objects.equals(albumName, "")) {
                font.drawStringWithShadow("专辑:" + albumName, position.position.x + 80, position.position.y + 5, new Color(255, 255, 255, 255).getRGB());
            }

            try {
                GL11.glPushMatrix();
                ResourceLocation image = getArt(getMusicId(musicName+singer));
                mc.getTextureManager().bindTexture(image);
                RenderUtil.drawImage2(getArt(getMusicId(musicName+singer)), (float) position.position.x + 5, (float) (position.position.y - 28), 70, 70, 1f);
                GL11.glPopMatrix();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        NORMAL_BLUR_RUNNABLES.add(() -> {
            RenderUtil.roundedRectangle(position.position.x, position.position.y - 35, width + 50, 108, 4, Color.BLACK);
        });

        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
            RenderUtil.roundedRectangle(position.position.x, position.position.y - 35, width + 50, 108, 5, getTheme().getDropShadow());

            // 绘制进度条阴影效果
            RenderUtil.drawRoundedGradientRect(position.position.x + 24, position.position.y + 60, progressWidthTarget, 4, 3, getTheme().getFirstColor(), getTheme().getSecondColor(), false);
        });
    };

    private void run() {
        if (!artPicFolder.exists())
            artPicFolder.mkdirs();
        SwingUtilities.invokeLater(JFXPanel::new);
        String sseUrl = "http://" + ip.getValue() + ":" + port.getValue() + "/subscribe-player-status?filter=lyricLineAllText,name,progress,duration,playbackRate,picUrl,singer,albumName";

        try {
            URL url = new URL(sseUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setDoOutput(true);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                String currentEvent = "";

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue; // 跳过空行
                    }

                    if (line.startsWith("event:")) {
                        currentEvent = line.split(":", 2)[1].trim();
                    } else if (line.startsWith("data:")) {
                        String data = line.split(":", 2)[1].trim().replaceAll("^\"|\"$", "");

                        // 根据事件名来处理数据
                        switch (currentEvent) {
                            case "status":
                                status = data;
                                break;
                            case "lyricLineAllText":
                                String[] parts = data.split("\\\\n");
                                lyric = parts[0];
                                expandLyric = parts.length > 1 ? parts[1] : "";
                                break;

                            case "name":
                                musicName = data;
                                break;

                            case "progress":
                                double tempDouble = Double.parseDouble(data); // 先转换为double
                                int intValue = (int) tempDouble; // 直接取整数部分，忽略小数
                                progress = String.valueOf(intValue);
                                break;

                            case "duration":
                                double tempDouble2 = Double.parseDouble(data); // 先转换为double
                                int intValue2 = (int) tempDouble2; // 直接取整数部分，忽略小数
                                duration = String.valueOf(intValue2);
                                break;

                            case "playbackRate":
                                playbackRate = data;
                                break;

                            case "picUrl":
                                picUrl = data;
                                break;

                            case "singer":
                                singer = data;
                                break;

                            case "albumName":
                                albumName = data;
                                break;
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String convertSecondsToHMS(int seconds) {
        int hours = seconds / (60 * 60);
        seconds %= (60 * 60);
        int minutes = seconds / 60;
        seconds %= 60;

        // 没有小时的情况，只保留分钟和秒
        if (hours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
    }

}
