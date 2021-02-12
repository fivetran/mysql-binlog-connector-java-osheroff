package com.github.shyiko.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.jmx.BinaryLogClientMXBean;
import com.github.shyiko.mysql.binlog.network.SSLMode;
import com.github.shyiko.mysql.binlog.network.SSLSocketFactory;

import java.io.IOException;

abstract public class BinaryLogClient implements BinaryLogClientMXBean {
    abstract public void setServerId(long serverId);
    abstract public void connect() throws IOException;
    abstract public void setSSLMode(SSLMode sslMode);
    abstract public void abort() throws IOException;
    abstract public void registerEventListener(EventListener eventListener);
    abstract public void registerLifecycleListener(LifecycleListener lifecycleListener);
    abstract public void setEventDeserializer(EventDeserializer eventDeserializer);
    abstract public void setKeepAliveConnectTimeout(long connectTimeout);
    abstract public long getConnectionId();
    abstract public void setSslSocketFactory(SSLSocketFactory sslSocketFactory);

    public interface LifecycleListener {

        /**
         * Called once client has successfully logged in but before started to receive binlog events.
         */
        void onConnect(BinaryLogClient client);

        /**
         * It's guarantied to be called before {@link #onDisconnect(BinaryLogClient)}) in case of
         * communication failure.
         */
        void onCommunicationFailure(BinaryLogClient client, Exception ex);

        /**
         * Called in case of failed event deserialization. Note this type of error does NOT cause client to
         * disconnect. If you wish to stop receiving events you'll need to fire client.disconnect() manually.
         */
        void onEventDeserializationFailure(BinaryLogClient client, Exception ex);

        /**
         * Called upon disconnect (regardless of the reason).
         */
        void onDisconnect(BinaryLogClient client);
    }

    public abstract static class AbstractLifecycleListener implements LifecycleListener {

        public void onConnect(BinaryLogClient client) { }

        public void onCommunicationFailure(BinaryLogClient client, Exception ex) { }

        public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) { }

        public void onDisconnect(BinaryLogClient client) { }

    }

    public interface EventListener {
        void onEvent(Event event);
    }
}
