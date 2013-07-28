package me.riking.bungeemmo.bukkit.network;

import java.net.InetAddress;
import java.net.Socket;

public class BackupConnection {
    public BackupConnection(InetAddress proxy, int port) {
        new Socket(proxy, 33333);
    }
}
