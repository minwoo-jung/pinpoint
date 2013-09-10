package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class TraceIndexMapper implements RowMapper<List<TransactionId>> {
	@Override
	public List<TransactionId> mapRow(Result result, int rowNum) throws Exception {
		if (result == null) {
			return Collections.emptyList();
		}

		KeyValue[] raw = result.raw();

		List<TransactionId> traceIdList = new ArrayList<TransactionId>(raw.length);

		for (KeyValue kv : raw) {
            byte[] buffer = kv.getBuffer();
            int qualifierOffset = kv.getQualifierOffset();
            TransactionId traceId = new TransactionId(buffer, qualifierOffset);

            traceIdList.add(traceId);
		}

		return traceIdList;
	}
}
