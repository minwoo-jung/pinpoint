package com.profiler.modifier.db.util;

import com.profiler.common.ServiceType;

/**
 *
 */
public class JDBCUrlParser {
    public DatabaseInfo parse(String url) {
        String lowCaseURL = url.toLowerCase().trim();
        if (lowCaseURL.startsWith("jdbc:mysql")) {
            return parseMysql(url);
        }
        if (lowCaseURL.startsWith("jdbc:oracle")) {
            return parseOracle(url);
        }

        return new DatabaseInfo(ServiceType.UNKNOWN, url, url, "error", "error", "error");
//        else if (url.indexOf("jdbc:oracle") >= 0) {
//            maker.lower().after("jdbc:oracle:").after(':');
//            info.type = TYPE.ORACLE;
//            String description = maker.after('@').value().trim();
//
//            if (description.startsWith("(")) {
//                Matcher matcher = oracleRAC.matcher(description);
//
//                if (matcher.matches()) {
//                    info.host = matcher.group(1);
//                    info.port = matcher.group(2);
//                    info.databaseId = matcher.group(3);
//                } else {
//                    info.databaseId = "ParsingFailed[" + System.currentTimeMillis() + ']';
//                }
//            } else {
//                info.host = maker.before(':').value();
//                info.port = maker.next().after(':').before(':').value();
//                info.databaseId = maker.next().afterLast(':').value();
//            }
//        } else if (url.indexOf("jdbc:sqlserver") >= 0) {
//            maker.lower().after("jdbc:sqlserver:");
//            info.type = TYPE.MSSQL;
//            info.host = maker.after("//").before(';').value();
//            info.port = maker.currentTraceClear().after("port=").before(';').value();
//            info.databaseId = maker.currentTraceClear().after("databasename=").before(';').value();
//        } else if (url.indexOf("jdbc:jtds:sqlserver") >= 0) {
//            maker.lower().after("jdbc:jtds:sqlserver:");
//            info.type = TYPE.MSSQL;
//            info.host = maker.after("//").beforeLast('/').beforeLast(':').value();
//            info.port = maker.next().after(':').beforeLast('/').value();
//            info.databaseId = maker.next().afterLast('/').before(';').value();
//        } else if (url.indexOf("jdbc:cubrid") >= 0) {
//            maker.lower().after("jdbc:cubrid");
//            info.type = TYPE.CUBRID;
//            info.host = maker.after(':').before(':').value();
//            info.port = maker.next().after(':').before(':').value();
//            info.databaseId = maker.next().after(':').before(':').value();
//        }
//        if ("".equals(info.databaseId)) {
//            info.databaseId = info.host;
//        }

//        return info;
//        return null;
    }

    private DatabaseInfo parseOracle(String url) {
        return null;
    }


    private DatabaseInfo parseMysql(String url) {
        //            jdbc:mysql://10.98.133.22:3306/test_lucy_db
        StringMaker maker = new StringMaker(url);
        maker.after("jdbc:mysql:");
        String host = maker.after("//").before('/').before(':').value();
        String port = maker.next().after(':').before('/').value();
        String databaseId = maker.next().afterLast('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();
        return new DatabaseInfo(ServiceType.MYSQL, url, normalizedUrl, host, port, databaseId);
    }
}
