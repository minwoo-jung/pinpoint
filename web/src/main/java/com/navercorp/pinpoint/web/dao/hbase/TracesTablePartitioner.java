package com.navercorp.pinpoint.web.dao.hbase;

import java.util.Set;

public interface TracesTablePartitioner {
    public Set<String> getTracesTablePartitionName();

    public Set<String> getTracesTablePartitionName(long timeInMillis);
    
//    public Set<String> getTracesTablePartitionName(long startTimeInMillis, long endTimeInMillis);

    public boolean isTracesTablePartitionEnable();

    void createTracesTable(String tableName);

    void dropTracesTable(String tableName);

}
