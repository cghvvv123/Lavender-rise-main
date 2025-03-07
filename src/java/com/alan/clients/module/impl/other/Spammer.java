package com.alan.clients.module.impl.other;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.chat.ChatUtil;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.player.ServerUtil;
import com.alan.clients.value.impl.NumberValue;
import util.time.StopWatch;

import java.util.ArrayList;
import java.util.List;

@Rise
@ModuleInfo(name = "module.other.spammer.name", description = "module.other.spammer.description", category = Category.OTHER)
public final class Spammer extends Module {

    private final List<String> messages = new ArrayList<>(); // 存储多个消息
    private final NumberValue delay = new NumberValue("Delay", this, 3000, 0, 20000, 1);
    private final StopWatch stopWatch = new StopWatch();
    private int currentIndex = 0;

    public Spammer() {
        messages.add("我是程鋆达,我写的木糖醇两天卖了一万多块钱");
        messages.add("我木糖醇作者程鋆达现在保证不卖");
        messages.add("快去购买我的木糖醇,给我送钱,我要给泷宝上舰长刷礼物!");
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (ServerUtil.isOnServer("loyisa.cn")) {
            ChatUtil.display("Upon a request from Loyisa we have blacklisted Loyisa's Test Server from Spammer.");
            this.toggle();
            return;
        }

        if (this.stopWatch.finished(delay.getValue().longValue())) {
            InstanceAccess.mc.thePlayer.sendChatMessage(messages.get(currentIndex));
            currentIndex = (currentIndex + 1) % messages.size();
            this.stopWatch.reset();
        }
    };
}
