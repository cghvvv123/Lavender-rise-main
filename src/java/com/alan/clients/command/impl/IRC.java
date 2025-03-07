package com.alan.clients.command.impl;

import com.alan.clients.Client;
import com.alan.clients.api.Rise;
import com.alan.clients.command.Command;

/**
 * @author Auth
 * @since 3/02/2022
 */
@Rise
public final class IRC extends Command {

    public IRC() {
        super("command.irc.description", "irc", "~");
    }

    @Override
    public void execute(final String[] args) {

        if (args.length <= 1) {
            error(String.format(".%s <message>", args[0]));
        } else {
            Client.INSTANCE.getSocketManager().chat(String.join(" ", args).substring(3).trim());
        }
    }
}
