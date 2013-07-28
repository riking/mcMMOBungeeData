package me.riking.bungeemmo.common.messaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface Message {
    /**
     * Get the PluginMessage channel to use for sending this message.
     *
     * @return channel
     */
    public String getSendingChannelName();

    /**
     * Write the Message into a byte[] for a PluginMessage. Be sure to close()
     * any DataOutputStreams or ObjectOutputStreams writing into
     * <code>out</code> before returning.
     *
     * @param out the ByteArrayOutputStream to write the data into
     * @throws IOException
     */
    public void write(ByteArrayOutputStream out) throws IOException;

    public String getSubchannel();
}
