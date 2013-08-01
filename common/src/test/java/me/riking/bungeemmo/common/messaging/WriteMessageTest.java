package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang.Validate;
import org.junit.BeforeClass;
import org.junit.Test;

public class WriteMessageTest {
    @BeforeClass
    public static void setupGlobal() {
        PluginMessageUtil.serverName = "Foo";
    }

    @Test
    public void testWriteStartupMessage() throws IOException {
        long version = new java.util.Random().nextLong();
        String prettyVersion = "vldgywexbmpmuuuvxpby";
    }

    @Test
    public void testWriteWelcomeMessage() throws IOException {
        String expectedName = "ru_nvcsw";
        ArrayList<String> expectedServerList = new ArrayList<String>();
        expectedServerList.add("aamvdabq");
        expectedServerList.add("jrimdfnv");
        expectedServerList.add("sipfnnpk");

        WelcomeMessage send = new WelcomeMessage(expectedName, expectedServerList);
        WelcomeMessage get = (WelcomeMessage) PluginMessageUtil.readIncomingMessage(PluginMessageUtil.writeMessage(send));
        Validate.isTrue(expectedName.equals(get.serverName));
        Validate.isTrue(expectedServerList.equals(get.otherServers));
    }
}
