package me.riking.bungeemmo.bungee;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PluginMessageListener implements Listener {
    public static final byte[] MAGIC = new byte[] {71, -92, -102, 4, 62, 119, -47, 65, 8, 94, 111, 120, 102, 94, -3, 35};

    @EventHandler
    public void handlePluginMessage(PluginMessageEvent event) {
        if ("mcmmodata".equals(event.getTag())) {
            if (!(event.getSender() instanceof Server)) {
                // don't pollute our info you dirty clients
                event.setCancelled(true);
            } else {
                String data;
                try {
                    data = new String(event.getData(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // Should never happen
                }

            }
        }
    }
}
