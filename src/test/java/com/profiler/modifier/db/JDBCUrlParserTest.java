package com.profiler.modifier.db;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.profiler.common.ServiceType;

import java.net.URI;
import java.util.regex.Matcher;

/**
 *
 */
public class JDBCUrlParserTest {

    private Logger logger = LoggerFactory.getLogger(JDBCUrlParserTest.class);
    private com.profiler.modifier.db.JDBCUrlParser jdbcUrlParser = new JDBCUrlParser();

    @Test
    public void testURIParse() throws Exception {

        URI uri = URI.create("jdbc:mysql:replication://10.98.133.22:3306/test_lucy_db");
        logger.debug(uri.toString());
        logger.debug(uri.getScheme());

        // URI로 파싱하는건 제한적임 한계가 있음.
        try {
            URI oracleRac = URI.create("jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=on)" +
                    "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.4) (PORT=1521))" +
                    "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.5) (PORT=1521))" +
                    "(CONNECT_DATA=(SERVICE_NAME=service)))");

            logger.debug(oracleRac.toString());
            logger.debug(oracleRac.getScheme());
            Assert.fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void mysqlParse1() {

        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql://ip_address:3306/database_name?useUnicode=yes&amp;characterEncoding=UTF-8");
        Assert.assertEquals(dbInfo.getType(), ServiceType.MYSQL);
        Assert.assertEquals(dbInfo.getHost().get(0), ("ip_address:3306"));
        Assert.assertEquals(dbInfo.getDatabaseId(), "database_name");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql://ip_address:3306/database_name");
    }

    @Test
    public void mysqlParse2() {

        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql://10.98.133.22:3306/test_lucy_db");
        Assert.assertEquals(dbInfo.getType(), ServiceType.MYSQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "10.98.133.22:3306");

        Assert.assertEquals(dbInfo.getDatabaseId(), "test_lucy_db");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql://10.98.133.22:3306/test_lucy_db");
        logger.info(dbInfo.toString());
        logger.info(dbInfo.getMultipleHost());
    }

    @Test
    public void mysqlParse3() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql://61.74.71.31/log?useUnicode=yes&amp;characterEncoding=UTF-8");
        Assert.assertEquals(dbInfo.getType(), ServiceType.MYSQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "61.74.71.31");
        Assert.assertEquals(dbInfo.getDatabaseId(), "log");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql://61.74.71.31/log");
        logger.info(dbInfo.toString());
    }

    @Test
    public void oracleParser1() {
        //    jdbc:oracle:thin:@hostname:port:SID
//      "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:oracle:thin:@hostname:port:SID");
        Assert.assertEquals(dbInfo.getType(), ServiceType.ORACLE);
        Assert.assertEquals(dbInfo.getHost().get(0), "hostname:port");
        Assert.assertEquals(dbInfo.getDatabaseId(), "SID");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:oracle:thin:@hostname:port:SID");
        logger.info(dbInfo.toString());
    }

    @Test
    public void oracleParser2() {
        //    jdbc:oracle:thin:@hostname:port:SID
//      "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE");
        Assert.assertEquals(dbInfo.getType(), ServiceType.ORACLE);
        Assert.assertEquals(dbInfo.getHost().get(0), "localhost:1521");
        Assert.assertEquals(dbInfo.getDatabaseId(), "XE");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE");
        logger.info(dbInfo.toString());
    }

    @Test
    public void oracleParserServiceName() {
        //    jdbc:oracle:thin:@hostname:port:SID
//      "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:oracle:thin:@hostname:port/serviceName");
        Assert.assertEquals(dbInfo.getType(), ServiceType.ORACLE);
        Assert.assertEquals(dbInfo.getHost().get(0), "hostname:port");
        Assert.assertEquals(dbInfo.getDatabaseId(), "serviceName");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:oracle:thin:@hostname:port/serviceName");
        logger.info(dbInfo.toString());
    }

    @Test
    public void oracleRacParser1() {
//    "jdbc:oracle:thin:@(Description1=(LOAD_BALANCE=on)" +
//    "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.4) (PORT=1521))" +
//            "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.5) (PORT=1521))" +
//            "(CONNECT_DATA=(SERVICE_NAME=service)))"
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=on)" +
                "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.4) (PORT=1521))" +
                "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.5) (PORT=1522))" +
                "(CONNECT_DATA=(SERVICE_NAME=service)))";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(rac);
        Assert.assertEquals(dbInfo.getType(), ServiceType.ORACLE);
        Assert.assertEquals(dbInfo.getHost().get(0), "1.2.3.4:1521");
        Assert.assertEquals(dbInfo.getHost().get(1), "1.2.3.5:1522");

        Assert.assertEquals(dbInfo.getDatabaseId(), "service");
        Assert.assertEquals(dbInfo.getUrl(), rac);
        logger.info(dbInfo.toString());
    }


}
