package com.alan.clients.command.impl;

import com.alan.clients.api.Rise;
import com.alan.clients.command.Command;
import com.alan.clients.module.impl.render.ClickGUI;
import com.alan.clients.util.chat.ChatUtil;
import com.alan.clients.value.impl.SubMode;

/**
 * @author Auth
 * @since 3/02/2022
 */
@Rise
public final class Say extends Command {

    public Say() {
        super("command.say.description", "say", "chat");
    }

    @Override
    public void execute(final String[] args) {

        this.getModule(ClickGUI.class).mode.setValue(new SubMode("Rise"));

        if (args.length <= 1) {
            error(String.format(".%s <message>", args[0]));
        } else {
            ChatUtil.send(String.join(" ", args).substring(3).trim());
            ChatUtil.display("command.say.sent");
        }
    }
}
