package com.nhn.pinpoint.web.dao.hbase;

import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import com.nhn.pinpoint.web.dao.MapResponseDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.ResponseTime;
import org.apache.hadoop.hbase.client.Scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapResponseTimeDao implements MapResponseDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String tableName = HBaseTables.MAP_STATISTICS_SELF;

    private int scanCacheSize = 40;

    @Autowired
    private RowMapper<ResponseTime> responseTimeMapper;

    @Autowired
    private HbaseOperations2 hbaseOperations2;


    @Override
    public List<ResponseTime> selectResponseTime(Application application, Range range) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("selectResponseTime applicationName:{}, {}", application, range);
        }
        Scan scan = createScan(application, range);
        List<ResponseTime> responseTimeList = hbaseOperations2.find(tableName, scan, responseTimeMapper);
        if (logger.isDebugEnabled()) {
            logger.debug("row:{}", responseTimeList.size());
            for (ResponseTime responseTime : responseTimeList) {
                logger.trace("responseTime:{}", responseTime);
            }
        }

        return responseTimeList;
    }

    private Scan createScan(Application application, Range range) {
        long startTime = TimeSlot.getStatisticsRowSlot(range.getFrom()) - 1;
        // hbase의 scanner를 사용하여 검색시 endTime은 검색 대상에 포함되지 않기 때문에, +1을 해줘야 된다.
        // 단 key가 역으로 치환되어 있으므로 startTime에 -1을 해야함.
        long endTime = TimeSlot.getStatisticsRowSlot(range.getTo());

        if (logger.isDebugEnabled()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
            logger.debug("scan startTime:{} endTime:{}", simpleDateFormat.format(new Date(startTime)), simpleDateFormat.format(new Date(endTime)));
        }

        // timestamp가 reverse되었기 때문에 start, end를 바꿔서 조회.

        byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), endTime);
        byte[] endKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), startTime);

        final Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);
        scan.setStartRow(startKey);
        scan.setStopRow(endKey);
        scan.addFamily(HBaseTables.MAP_STATISTICS_SELF_CF_COUNTER);
        scan.setId("ApplicationSelfScan");

        return scan;
    }


}
