/*
 * Copyright 2016 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.test.pinpoint.testweb.controller;

import com.nhncorp.nbase_t.jdbc.NbaseDataSource;
import com.nhncorp.nbase_t.jdbc.NbaseThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Controller
public class NbasetController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static DataSource dsForQuery;
    private static DataSource dsForQueryAll;
    private static DataSource dsForQueryAllCkeyList;

    static {
        try {
            dsForQuery = NbaseDataSource.create("jdbc:nbase//10.98.133.158:6219/zlatan/query?zone-list=0&timeout=1&txTimeout=10&port=6220");
            dsForQueryAll = NbaseDataSource.create("jdbc:nbase//10.98.133.158:6219/zlatan/query-all?zone-list=0&timeout=1&txTimeout=10&port=6220");
            dsForQueryAllCkeyList = NbaseDataSource.create("jdbc:nbase//10.98.133.158:6219/zlatan/query-all-ckey-list?zone-list=0&timeout=1&txTimeout=10&port=6220");
        } catch(Exception e) {
        }
    }

    @RequestMapping(value = "/nbaset/crud")
    @ResponseBody
    public String crud() {
        String result = "OK";
        try {
            doQuery("ckey1", 1);
            doQueryAll(3);
            doQueryAllCkeyList("ckey1;ckey2", 3);
        } catch (SQLException e) {
            result = e.getMessage();
        } finally {
//            NbaseDataSource.fini();
        }

        return result;
    }

    private static void doQuery(String ckey, int queryTimeoutSec) throws SQLException {
        NbaseThreadLocal.setCkey(ckey);
        Connection conn = dsForQuery.getConnection();
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT a,b FROM foo WHERE a=?");
            try {
                pstmt.setInt(1, 100);
                pstmt.setQueryTimeout(queryTimeoutSec);
                ResultSet rs = pstmt.executeQuery();
                try {
                    while (rs.next()) {
                        System.out.println(rs.getString("b"));
                    }
                } finally {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                    }
                }
            } finally {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    private static void doQueryAll(int queryTimeoutSec) throws SQLException {
        Connection conn = dsForQueryAll.getConnection();
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT a,b FROM foo WHERE a=? LIMIT 100");
            try {
                pstmt.setInt(1, 100);
                pstmt.setQueryTimeout(queryTimeoutSec);
                ResultSet rs = pstmt.executeQuery();
                try {
                    while (rs.next()) {
                        System.out.println(rs.getString("b"));
                    }
                } finally {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                    }
                }
            } finally {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    private static void doQueryAllCkeyList(String ckeyList, int queryTimeoutSec) throws SQLException {
        NbaseThreadLocal.setCkey(ckeyList);
        Connection conn = dsForQueryAllCkeyList.getConnection();
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT a,b FROM foo WHERE a=? LIMIT 100");
            try {
                pstmt.setInt(1, 100);
                pstmt.setQueryTimeout(queryTimeoutSec);
                ResultSet rs = pstmt.executeQuery();
                try {
                    while (rs.next()) {
                        System.out.println(rs.getString("b"));
                    }
                } finally {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                    }
                }
            } finally {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    @RequestMapping(value = "/nbaset/jdbcTemplate")
    @ResponseBody
    public String jdbcTemplate() {
        JdbcTemplate template = new JdbcTemplate(dsForQuery);
        NbaseThreadLocal.setCkey("ckey1;ckey2");
        String sql = "SELECT a,b FROM foo WHERE a=100 LIMIT 100";
        List<Map<String, Object>> result = template.queryForList(sql);

        return "OK";
    }

    @RequestMapping(value = "/nbaset/driver")
    @ResponseBody
    public String driver() {
        try {
            Class.forName("com.nhncorp.nbase_t.jdbc.NbaseDriver");
            NbaseThreadLocal.setCkey("ckey1;ckey2");
            Connection conn = DriverManager.getConnection("jdbc:nbase//10.98.133.158:6219/zlatan/query-all-ckey-list?zone-list=0&timeout=1&txTimeout=10&port=6220");
            try {
                PreparedStatement pstmt = conn.prepareStatement("SELECT a,b FROM foo WHERE a=? LIMIT 100");
                try {
                    pstmt.setInt(1, 100);
                    ResultSet rs = pstmt.executeQuery();
                    try {
                        while (rs.next()) {
                            System.out.println(rs.getString("b"));
                        }
                    } finally {
                        try {
                            rs.close();
                        } catch (SQLException e) {
                        }
                    }
                } finally {
                    try {
                        pstmt.close();
                    } catch (SQLException e) {
                    }
                }
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        } catch(Exception e) {
        }

        return "OK";
    }
}