package me.riking.bungeemmo.common.messaging;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.commons.lang.Validate;


public abstract class AbstractServerServerMessage implements Message {
    public final String destinationServer;

    public AbstractServerServerMessage(String dest) {
        destinationServer = dest;
    }

    @Override
    public String getSendingChannelName() {
        return PluginMessageUtil.BUNGEE_CHANNEL_NAME;
    }

    public String getDestinationServer() {
        return destinationServer;
    }

    /**
     * Write the BungeeCord headers.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void write(ByteArrayOutputStream byteOut) throws IOException {
        Validate.notNull(destinationServer, "Recipient attempted to re-send a recieved Server-Server message");

        DataOutputStream outF = new DataOutputStream(byteOut);
        outF.writeUTF("Forward");
        outF.writeUTF(destinationServer);
        outF.writeUTF(PluginMessageUtil.MCMMO_CHANNEL_NAME);

        ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
        ObjectOutputStream out2 = new ObjectOutputStream(byteOut2);
        out2.writeUTF(getSubchannel());
        writeData(out2);
        out2.close();
        byte[] bytes = byteOut2.toByteArray();
        outF.writeShort(bytes.length);
        outF.write(bytes);
        outF.close();
    }

    /**
     * Write the part of the message to be recieved by the destination server.
     *
     * @param out
     * @throws IOException
     */
    protected abstract void writeData(ObjectOutputStream out) throws IOException;
}
