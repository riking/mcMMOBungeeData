package me.riking.bungeemmo.bungee;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import me.riking.bungeemmo.common.messaging.PluginMessageUtil;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PluginMessageListener implements Listener {
    @EventHandler
    public void handlePluginMessage(PluginMessageEvent event) {
        if (PluginMessageUtil.CHANNEL_NAME.equals(event.getTag())) {
            if (!(event.getSender() instanceof Server)) {
                // don't pollute our info you dirty clients
                event.setCancelled(true);
            } else {

            }
        }
    }
}
