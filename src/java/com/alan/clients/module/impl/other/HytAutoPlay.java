package com.alan.clients.module.impl.other;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.module.impl.combat.Velocity;
import com.alan.clients.module.impl.player.Manager;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.module.impl.player.Stealer;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.other.WorldChangeEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.noti.NotificationManager;
import com.alan.clients.noti.NotificationType;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S02PacketChat;

@ModuleInfo(name = "HytAutoPlay", category = Category.OTHER, description = "CNM")
    public class HytAutoPlay extends Module {
        public static int wins = 0;
        public static int banned = 0;
        private final BooleanValue autodis = new BooleanValue("AutoDisable", this, true);

        @EventLink
        public final Listener<WorldChangeEvent> onWorldChange = event -> disableModule();

        @EventLink
        public final Listener<PreUpdateEvent> onPreUpdate = event -> {
            Scaffold scaffold = getModule(Scaffold.class);
            if (scaffold.isEnabled()){
                if (scaffold.getBlockCount() == 0) {
                    NotificationManager.post(NotificationType.INFO, "Scaffold", "Don't have blocks");
                    getModule(Scaffold.class).setEnabled(false);
                }
            }
        };

        @EventLink()
        public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> handlePacket(event.getPacket());

        public void disableModule() {
            disableModule(KillAura.class);
            disableModule(Manager.class);
            disableModule(Stealer.class);
            disableModule(ChestAura.class);
        }

        private void disableModule(Class<? extends Module> moduleClass) {
            Module module = getModule(moduleClass);
            if (module.isEnabled()) {
                module.setEnabled(false);
            }
        }

        private void handlePacket(Packet<?> packet) {
            if (packet instanceof S01PacketJoinGame) {
                disableModule();
            } else if (packet instanceof S02PacketChat) {
                handleChatPacket((S02PacketChat) packet);
            }
        }

        private void handleChatPacket(S02PacketChat chatPacket) {
            String text = chatPacket.chatComponent.getUnformattedText();

            if (text.contains("开始倒计时: 1 秒")) {
                enableImportantModules();
            } else if (text.contains("你在地图") && text.contains("赢得了")) {
                handleWin();
            } else if (text.contains("[起床战争] Game 结束！感谢您的参与！") || text.contains("喜欢 一般 不喜欢")) {
                disableImportantModules();
            } else if (text.contains("玩家") && text.contains("在本局游戏中行为异常")) {
                handleBannedPlayer();
            }
        }

        private void enableImportantModules() {
            getModule(Velocity.class).setEnabled(true);
            getModule(Stealer.class).setEnabled(true);
            getModule(Manager.class).setEnabled(true);
            getModule(ChestAura.class).setEnabled(true);
        }

        private void handleWin() {
            wins++;
            if (autodis.getValue()) {
                disableImportantModules();
            }
        }

        private void disableImportantModules() {
            if (autodis.getValue()) {
                disableModule(KillAura.class);
                disableModule(Velocity.class);
                disableModule(Stealer.class);
                disableModule(Manager.class);
                disableModule(ChestAura.class);
            }
        }

        private void handleBannedPlayer() {
            banned++;
            NotificationManager.post(NotificationType.WARNING, "BanChecker", "A player was banned.", 3);
        }
    }
