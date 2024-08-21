package com.github.shyiko.mysql.binlog;

import static org.testng.Assert.assertEquals;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.BitSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.github.shyiko.mysql.binlog.BinaryLogClientIntegrationTest.Callback;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventMetadata;

public class OptionalMetaDataIntegrationTest extends AbstractIntegrationTest {
    protected static final long DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(3);

    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    {
        logger.setLevel(Level.FINEST);
    }

    public void checkMysqlVersion() throws Exception {
        if (!mysqlVersion.atLeast(8, 0)) {
            throw new SkipException("For Optional Meta Data Test MYSQL VERSION SHOULD BE MORE THAN 8.0");
        }
    }

    @BeforeMethod
    public void beforeEachTest() throws Exception {
        checkMysqlVersion();
        master.execute(new Callback<Statement>() {
            @Override
            public void execute(Statement statement) throws SQLException {
                statement.execute("drop table if exists optionalMetaDataIntegrationTest");
                statement.execute(
                    "create table optionalMetaDataIntegrationTest (col1 int unsigned , col2 int, col3 varchar(30), col4 int unsigned)");
            }
        });
        eventListener.waitForAtLeast(EventType.QUERY, 2, DEFAULT_TIMEOUT);
        eventListener.reset();
    }

    @Test
    public void testSignedness() throws Exception {
        CapturingEventListener capturingEventListener = new CapturingEventListener();
        client.registerEventListener(capturingEventListener);
        // ensure "capturingEventListener -> eventListener" order
        client.unregisterEventListener(eventListener);
        client.registerEventListener(eventListener);
        try {
            master.execute(new Callback<Statement>() {
                @Override
                public void execute(Statement statement) throws SQLException {
                    statement.execute("insert into optionalMetaDataIntegrationTest values (1,2,3,4)");
                }
            });
            eventListener.waitFor(TableMapEventData.class, 1, DEFAULT_TIMEOUT);
            TableMapEventMetadata eventMetadata = capturingEventListener.getEvents(TableMapEventData.class).get(
                0).getEventMetadata();
            BitSet expectBitSet = new BitSet();
            expectBitSet.set(0);
            expectBitSet.set(3);
            assertEquals(eventMetadata.getSignedness(), expectBitSet);

        } finally {
            client.unregisterEventListener(capturingEventListener);
        }
    }

}
