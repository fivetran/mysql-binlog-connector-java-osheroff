package com.github.shyiko.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import org.testng.annotations.BeforeClass;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;

public abstract class AbstractIntegrationTest {
    protected MySQLConnection master;
    protected MySQLConnection slave;
    protected BinaryLogClient client;
    protected CountDownEventListener eventListener;
    protected MysqlVersion mysqlVersion;

    protected MysqlOnetimeServerOptions getOptions() {
        MysqlOnetimeServerOptions options = new MysqlOnetimeServerOptions();
        options.fullRowMetaData = true;
        return options;
    }

    @BeforeClass
    public void setUp() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        mysqlVersion = MysqlOnetimeServer.getVersion();
        MysqlOnetimeServer masterServer = new MysqlOnetimeServer(getOptions());
        MysqlOnetimeServer slaveServer = new MysqlOnetimeServer(getOptions());

        masterServer.boot();
        slaveServer.boot();
        slaveServer.setupSlave(masterServer.getPort());

        master = new MySQLConnection("127.0.0.1", masterServer.getPort(), "root", "");
        slave = new MySQLConnection("127.0.0.1", slaveServer.getPort(), "root", "");

        client = new BinaryLogClient(slave.hostname, slave.port, slave.username, slave.password);
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY,
            EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG);
        client.setEventDeserializer(eventDeserializer);
        client.setServerId(client.getServerId() - 1); // avoid clashes between BinaryLogClient instances
        client.setKeepAlive(false);
        client.registerEventListener(new TraceEventListener());
        client.registerEventListener(eventListener = new CountDownEventListener());
        client.registerLifecycleListener(new TraceLifecycleListener());
        client.connect(BinaryLogClientIntegrationTest.DEFAULT_TIMEOUT);
        master.execute(new BinaryLogClientIntegrationTest.Callback<Statement>() {
            @Override
            public void execute(Statement statement) throws SQLException {
                statement.execute("drop database if exists mbcj_test");
                statement.execute("create database mbcj_test");
                statement.execute("use mbcj_test");
            }
        });
        eventListener.waitFor(EventType.QUERY, 2, BinaryLogClientIntegrationTest.DEFAULT_TIMEOUT);

        if ( mysqlVersion.atLeast(8, 0) ) {
            setupMysql8Login(master);
            eventListener.waitFor(EventType.QUERY, 2, BinaryLogClientIntegrationTest.DEFAULT_TIMEOUT);
        }
    }

    protected void setupMysql8Login(MySQLConnection server) throws Exception {
        server.execute("create user 'mysql8' IDENTIFIED WITH caching_sha2_password BY 'testpass'");
        server.execute("grant replication slave, replication client on *.* to 'mysql8'");
    }
}
