package com.alan.clients.module.impl.player;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.TimerUtil;
import com.alan.clients.util.animations.Animation;
import com.alan.clients.util.animations.Easing;
import com.alan.clients.util.chat.ChatUtil;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.value.impl.NumberValue;
import lombok.Getter;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

import java.awt.*;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.alan.clients.module.impl.render.Interface.mixColors2;

@Rise
@ModuleInfo(name = "TimerBalance", description = "DWGXspeed", category = Category.MOVEMENT)
public class TimerBalance extends Module {
    private final NumberValue speed = new NumberValue("Speed", this, 2.0F, 0.01F, 10.0F, 0.1F);
    private final Queue<C0FPacketConfirmTransaction> transactions = new LinkedBlockingQueue<>();
    private final Animation scaleAnim = new Animation(Easing.EASE_IN_EXPO, 50L);
    private final TimerUtil balanceTimer = new TimerUtil();
    @Getter
    private double balance = 0;

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        PacketUtil.sendC0F(0, (short) 0, true, true);

        if (mc.thePlayer.ticksExisted % 20 == 0) {
            ChatUtil.display("Balance: " + this.balance);
        }

        if (this.balance < 0) {
            this.toggle();
        }
    };


    @Override
    public void onEnable() {
        balanceTimer.reset();
        transactions.clear();
    }


    @EventLink
    private final Listener<PacketSendEvent> onPacketReceive = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C03PacketPlayer) {
            C03PacketPlayer wrapper = (C03PacketPlayer) packet;
            if (!wrapper.getRotating() && !isMoving()) {
                event.setCancelled(true);
            }

            if (!event.isCancelled() && this.balance > 0) {
                this.balance -= 50;
                this.balanceTimer.reset();
                return;
            }

            this.balance += balanceTimer.getElapsedTime();
            this.balanceTimer.reset();
        }

        if (packet instanceof C0FPacketConfirmTransaction)  {
            C0FPacketConfirmTransaction wrapper = (C0FPacketConfirmTransaction) packet;
            transactions.add(wrapper);
            event.setCancelled(true);
        }
    };

    // @EventLink
    // private final Listener<Render2DEvent> onRender = event -> {
//        if (System.currentTimeMillis() - lastTransaction >= 50 / mc.timer.timerSpeed) {
//            if (!transactions.isEmpty())
//                transactions.poll().processPacket(mc.getNetHandler());
//            lastTransaction = System.currentTimeMillis();
//        }
//
//        if (System.currentTimeMillis() - lastCustomPacketSent >= 50 / mc.timer.timerSpeed) {
//            lastCustomPacketSent = System.currentTimeMillis();
//        }
//    };
    @Override
    public void onDisable() {
        if (!transactions.isEmpty()) {
            PacketUtil.sendNoEvent(transactions.poll());
        }

        this.balance = 0;
        this.balanceTimer.reset();
    }

    private boolean isMoving(){
        return  mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0 || mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown();
    }
//    @EventLink()
//    public final Listener<Render2DEvent> onRender2D = event -> {
//        if (!ing) return;
//
//        Minecraft mc = Minecraft.getMinecraft();
//        ScaledResolution scaled = new ScaledResolution(mc);
//        int startX = scaled.getScaledWidth() / 2;
//        int startY = (scaled.getScaledHeight() / 2);
//        float width = 115;
//        float height = 30f;
//        float x = (startX - 58);
//        float y = startY - 52;
//
//        durationInMillis = System.currentTimeMillis() - Client.START_TIME;
//        long newSecond = durationInMillis / 1000;
//        if (newSecond > second) {
//            second = newSecond;
//        }
//
//        double progress = ((double) (durationInMillis % 1000) / 1000);
//
//        Color accent1 = getTheme().getFirstColor();
//        Color accent2 = getTheme().getSecondColor();
//
//        GL11.glScaled(scaleAnim.getValue(), scaleAnim.getValue(), scaleAnim.getValue());
//
//        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
//            RenderUtil.drawRoundedGradientRect(x + 10, y + 18, 95, 5, 2, new Color(0, 0, 0, 60), new Color(0, 0, 0, 60), true);
//            RenderUtil.roundedRectangle(x, y, width, height, getTheme().getRound(), getTheme().getBackgroundShade());
//
//            RenderUtil.drawRoundedGradientRect(x + 10, y + 18, progress * 95, 5, 2, accent2, accent1, true);
//
//            RenderUtil.resetColor();
//            GradientUtil.applyGradientHorizontal(x + 8, y + 4, nunitoNormal.width("TimerBalance"), 20, 1, getClientColors()[0], getClientColors()[1], () -> {
//                RenderUtil.setAlphaLimit(0);
//                nunitoNormal.drawString("TimerBalance", x + 8, y + 4, 0);
//            });
//        });
//
//        NORMAL_BLUR_RUNNABLES.add(() -> {
//            RenderUtil.roundedRectangle(x, y, width, height, getTheme().getRound(), Color.BLACK);
//        });
//
//        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
//            RenderUtil.roundedRectangle(x, y, width, height, getTheme().getRound(), getTheme().getDropShadow());
//        });
//    };


    private Color getClientColor () {
        Color theme1 = this.getTheme().getFirstColor();
        return new Color(theme1.getRGB());

    }
    private Color getAlternateClientColor () {
        Color theme2 = this.getTheme().getSecondColor();
        return new Color(theme2.getRGB());
    }
    public Color[] getClientColors () {
        Color firstColor = mixColors2(getClientColor(), getAlternateClientColor());
        Color secondColor = mixColors2(getAlternateClientColor(), getClientColor());
        return new Color[]{firstColor, secondColor};
    }
}
