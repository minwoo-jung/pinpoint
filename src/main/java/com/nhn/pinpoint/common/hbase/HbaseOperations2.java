package com.nhn.pinpoint.common.hbase;

import java.util.List;

import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.client.*;
import org.springframework.data.hadoop.hbase.HbaseOperations;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;

/**
 *
 */
public interface HbaseOperations2 extends HbaseOperations {
    /**
     * Gets an individual row from the given table. The content is mapped by the given action.
     *
     * @param tableName target table
     * @param rowName   row name
     * @param mapper    row mapper
     * @return object mapping the target row
     */
    <T> T get(String tableName, byte[] rowName, final RowMapper<T> mapper);

    /**
     * Gets an individual row from the given table. The content is mapped by the given action.
     *
     * @param tableName  target table
     * @param rowName    row name
     * @param familyName column family
     * @param mapper     row mapper
     * @return object mapping the target row
     */
    <T> T get(String tableName, byte[] rowName, byte[] familyName, final RowMapper<T> mapper);

    /**
     * Gets an individual row from the given table. The content is mapped by the given action.
     *
     * @param tableName  target table
     * @param rowName    row name
     * @param familyName family
     * @param qualifier  column qualifier
     * @param mapper     row mapper
     * @return object mapping the target row
     */
    <T> T get(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final RowMapper<T> mapper);

    <T> T get(String tableName, final Get get, final RowMapper<T> mapper);

    <T> List<T> get(String tableName, final List<Get> get, final RowMapper<T> mapper);


    void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final byte[] value);

    void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final Long timestamp, final byte[] value);

    <T> void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final T value, final ValueMapper<T> mapper);

    <T> void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final Long timestamp, final T value, final ValueMapper<T> mapper);

    void put(String tableName, final Put put);

    void put(String tableName, final List<Put> puts);

    void delete(String tableName, final Delete delete);

    void delete(String tableName, final List<Delete> deletes);

    <T> List<T> find(String tableName, final List<Scan> scans, final ResultsExtractor<T> action);

    <T> List<List<T>> find(String tableName, final List<Scan> scans, final RowMapper<T> action);

    <T> List<T> find(String tableName, final Scan scan, AbstractRowKeyDistributor rowKeyDistributor, final RowMapper<T> action);

    <T> List<T> find(String tableName, final Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action);

    <T> T find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action);

    Result increment(String tableName, final Increment increment);

    long incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount);

    long incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount, final boolean writeToWAL);

}
