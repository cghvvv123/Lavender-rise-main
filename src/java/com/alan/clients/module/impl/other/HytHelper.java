package com.alan.clients.module.impl.other;


import com.alan.clients.Client;
import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.other.AttackEvent;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.noti.NotificationManager;
import com.alan.clients.noti.NotificationType;
import com.alan.clients.util.MSTimer;
import com.alan.clients.util.chat.ChatUtil;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.math.MemoryUtils;
import com.alan.clients.util.player.ServerUtil;
import com.alan.clients.util.player.WeaponDetection;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.StringValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Rise
@ModuleInfo(name = "module.other.hythelper.name", description = "Include com.alan.clients.hyt vexview and germ", category = Category.OTHER)
public class HytHelper extends Module {
    private final BooleanValue memoryfix = new BooleanValue("MemoryFix", this, true);
    private final BooleanValue autogg = new BooleanValue("AutoGG", this, true);
    private final BooleanValue tarckNoti = new BooleanValue("PlayerTrack Noti", this, true);
    private final StringValue messages = new StringValue("Message", this, "Get good get Lavender");
    public static List<Entity> flaggedEntity = new ArrayList<>();
    private final Random random = new Random();
    private final List<String> cydMessages = Arrays.asList("%%target%%愿意和我木糖醇作者程鋆达签订保证不卖条约,成为魔法少女吗?", "我是程鋆达,我写的木糖醇两天卖了一万多块钱,给%%target%%羡慕坏了吧!", "我木糖醇作者程鋆达现在要求%%target%%保证不卖!", "哈哈,%%target%%已经被我程鋆达的木糖醇害死!", "我打不打得过%%target%%,都知道.", "算了,我和%%target%%不多说了.", "无所谓,我程鋆达也可以用木糖醇出视频,暴打%%target%%.", "我程鋆达用木糖醇继续圈钱,反正IRC够那%%target%%受的了.", "呵呵,%%target%%能保证不卖吗?有钱不赚.", "我程鋆达还要圈%%target%%的钱呢.", "%%target%%快去购买我的木糖醇,给我送钱,我要给泷宝上舰长刷礼物!", "我张恩哲觉得那个%%target%%现在已经不如当年了!", "我当时看%%target%%库库发视频,真以为要付出优点急眼了.", "我咋可能闲的没事搞%%target%%,就投了一个视频当天还删了.", "无所谓啊,反正我发誓,下%%target%%视频我撕全价.", "%%target%%不会以为视频也是我下的吧?", "爱信不信吧,我删%%target%%视频去啦!", "我张恩哲觉得%%target%%老实几天就不老实了.", "等会%%target%%给我张恩哲和程鋆达狙击了,虽然我不怕.");
    private EntityLivingBase target = null;
    public static String name;
    public static int kill = 0;
    private final MSTimer timer = new MSTimer();
    public static boolean regen = false;
    public static boolean strength = false;
    public static boolean gapple = false;
    public static boolean godaxe = false;
    public static boolean kbball = false;

    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        flaggedEntity.clear();
        strength = false;
        regen = false;
        godaxe = false;
        gapple = false;
        kbball = false;
    };
    @EventLink()
    public final Listener<AttackEvent> onAttack = event -> {
        if (isNull()) return;
        Entity targetEntity = event.getTarget();
        if (targetEntity instanceof EntityPlayer) {
            target = (EntityLivingBase) targetEntity;
        }
    };
    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (isNull()) return;
        if (target != null && this.target.getHealth() <= 0.0f && !mc.theWorld.loadedEntityList.contains(this.target)) {
            if (isNull()) return;
            if (target.isDead) {
                kill++;
            }
            if (autogg.getValue()) {
                if (ServerUtil.isOnServer("loyisa.cn")) {
                    ChatUtil.display("[AutoGG]This mod unable to use in this server");
                } else {
                    String customMessage = this.messages.getValue().replace("%%player%%", mc.thePlayer.getCommandSenderName()).replace("%%target%%", target.getCommandSenderName()).replace("%%killcount%%", String.valueOf(kill)).replace("%%clientname%%", Client.NAME).replace("%%clientversion%%", Client.VERSION);
                    String message = getMessage(target.getCommandSenderName(), customMessage).replace("%%player%%", mc.thePlayer.getCommandSenderName()).replace("%%target%%", target.getCommandSenderName()).replace("%%killcount%%", String.valueOf(kill)).replace("%%clientname%%", Client.NAME).replace("%%clientversion%%", Client.VERSION);
                    message += " " + generateRandomString(5);
                    InstanceAccess.mc.thePlayer.sendChatMessage(message);
                }
            }
            target = null;
        }
    };


    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            stringBuilder.append(randomChar);
        }
        return stringBuilder.toString();
    }

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (isNull()) return;
        final Packet<?> packet = event.getPacket();
        if (packet instanceof C01PacketChatMessage) {
            C01PacketChatMessage chatPacket = (C01PacketChatMessage) packet;
            String message = chatPacket.getMessage();
            if (message.startsWith("/")) {
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (char c : message.toCharArray()) {
                if (c >= 33 && c <= 128) {
                    stringBuilder.append((char) (c + 65248));
                } else {
                    stringBuilder.append(c);
                }
            }
            chatPacket.message = stringBuilder.toString();
        }
    };

    @EventLink()
    public final Listener<TickEvent> onTick = event -> {
        if (isNull()) return;
        if (InstanceAccess.mc.theWorld == null || InstanceAccess.mc.theWorld.loadedEntityList.isEmpty()) {
            strength = false;
            regen = false;
            godaxe = false;
            gapple = false;
            kbball = false;
            return;
        }
        if (WeaponDetection.isInLobby()) {
            strength = false;
            regen = false;
            godaxe = false;
            gapple = false;
            kbball = false;
            return;
        }
        if (InstanceAccess.mc.thePlayer.ticksExisted % 6 == 0) {
            for (final Entity ent : InstanceAccess.mc.theWorld.loadedEntityList) {
                if (ent instanceof EntityPlayer && ent != InstanceAccess.mc.thePlayer) {
                    final EntityPlayer player = (EntityPlayer) ent;
                    if (WeaponDetection.isStrength(player) > 0 && !flaggedEntity.contains(player)) {
                        flaggedEntity.add(player);
                        if (tarckNoti.getValue()) {
                            NotificationManager.post(NotificationType.WARNING, "PlayerTrack", player.getCommandSenderName() + "拥有力量药水", 15);
                        }
                        name = player.getCommandSenderName();
                        strength = true;
                    }
                    if (WeaponDetection.isRegen(player) > 0 && !flaggedEntity.contains(player)) {
                        flaggedEntity.add(player);
                        if (tarckNoti.getValue()) {
                            NotificationManager.post(NotificationType.WARNING, "PlayerTrack", player.getCommandSenderName() + "拥有恢复药水", 15);
                        }
                        name = player.getCommandSenderName();
                        regen = true;
                    }
                    if (WeaponDetection.isHoldingGodAxe(player) && !flaggedEntity.contains(player)) {
                        flaggedEntity.add(player);
                        if (tarckNoti.getValue()) {
                            NotificationManager.post(NotificationType.WARNING, "PlayerTrack", player.getCommandSenderName() + "正在使用秒人斧", 15);
                        }
                        name = player.getCommandSenderName();
                        godaxe = true;
                    }
                    if (WeaponDetection.isKBBall(player.getHeldItem()) && !flaggedEntity.contains(player)) {
                        flaggedEntity.add(player);
                        if (tarckNoti.getValue()) {
                            NotificationManager.post(NotificationType.WARNING, "PlayerTrack", player.getCommandSenderName() + "正在使用击退球,请小心点", 15);
                        }
                        name = player.getCommandSenderName();
                        kbball = true;
                    }
                    if (WeaponDetection.hasEatenGoldenApple(player) <= 0 || flaggedEntity.contains(player)) {
                        continue;
                    }
                    name = player.getCommandSenderName();
                    gapple = true;
                    flaggedEntity.add(player);
                    if (tarckNoti.getValue()) {
                        NotificationManager.post(NotificationType.WARNING, "PlayerTrack", player.getCommandSenderName() + "拥有附魔金苹果", 15);
                    }
                }
            }
        }
        if (memoryfix.getValue()) {
            final long maxMem = Runtime.getRuntime().maxMemory();
            final long totalMem = Runtime.getRuntime().totalMemory();
            final long freeMem = Runtime.getRuntime().freeMemory();
            final long usedMem = totalMem - freeMem;
            if (this.timer.hasReached(120 * 1000.0) && 80 <= (float) (usedMem * 100L / maxMem)) {
                MemoryUtils.memoryCleanup();
                this.timer.reset();
            }
        }
    };
    private String getMessage(String targetName, String customMessage) {

        return processMessages(cydMessages);
    }

    private String processMessages(List<String> messages) {
        int index = (new Random()).nextInt(messages.size());
        return messages.get(index);
    }
}

