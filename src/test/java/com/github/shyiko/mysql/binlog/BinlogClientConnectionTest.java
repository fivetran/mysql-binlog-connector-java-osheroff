package com.github.shyiko.mysql.binlog;


import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.network.SSLMode;
import org.junit.Test;

import java.io.IOException;

public class BinlogClientConnectionTest {

    public static void main(String[] args) throws IOException {
        BinaryLogClient client = null; // TODO

        client.registerEventListener(new BinaryLogClient.EventListener() {
            @Override
            public void onEvent(Event event) {
                System.out.println(event);
            }
        });
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(
            EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY);
        client.setEventDeserializer(eventDeserializer);
        client.registerLifecycleListener(new BinaryLogClient.LifecycleListener() {
            @Override
            public void onConnect(BinaryLogClient client) {
                System.out.println("LifecycleListener#onConnect");
            }

            @Override
            public void onCommunicationFailure(BinaryLogClient client, Exception ex) {
                System.out.println("LifecycleListener#onCommunicationFailure");
            }

            @Override
            public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {
                System.out.println("LifecycleListener#onEventDeserializationFailure");
            }

            @Override
            public void onDisconnect(BinaryLogClient client) {
                System.out.println("LifecycleListener#onDisconnect");
            }
        });
        client.setSSLMode(SSLMode.DISABLED);

        client.connect();
    }

    @Test
    public void test() throws IOException {
        main(null);
    }
}
