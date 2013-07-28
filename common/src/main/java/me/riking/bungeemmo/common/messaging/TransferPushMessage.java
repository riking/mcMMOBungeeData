package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

import me.riking.bungeemmo.common.data.TransitPlayerProfile;

public class TransferPushMessage extends AbstractServerServerMessage {
    public final TransitPlayerProfile profile;
    public final boolean success;

    /**
     * Recipient constructor
     */
    public TransferPushMessage(TransitPlayerProfile profile, boolean success) {
        super(null);
        this.profile = profile;
        this.success = success;
    }

    /**
     * Sender constructor
     */
    public TransferPushMessage(String destinationServer, TransitPlayerProfile profile, boolean success) {
        super(destinationServer);
        this.profile = profile;
        this.success = success;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        out.writeBoolean(success);
        out.writeObject(profile);
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.PUSH_TRANSFER_SUBCHANNEL;
    }

}
