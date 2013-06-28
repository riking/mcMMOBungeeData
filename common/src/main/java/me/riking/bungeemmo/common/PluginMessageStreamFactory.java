package me.riking.bungeemmo.common;

public interface PluginMessageStreamFactory {
    // Stream creation
    public PluginMessageStream createStream(String channel);

    // Packet sending
    public void handleInputMessage(byte[] message);
    public boolean hasOutputReady();
    public byte[] getNextOutput();
}
