package com.navercorp.pinpoint.collector.dao.hbase;

public interface TracesTablePartitioner {
    public String getTracesTablePartitionName();

    public boolean isTracesTablePartitionEnable();
}
