package com.nhn.pinpoint.collector.mapper;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ApplicationNameMapper implements RowMapper<String> {
	@Override
	public String mapRow(Result result, int rowNum) throws Exception {
		KeyValue[] raw = result.raw();

		if (raw.length == 0) {
			return null;
		}

		String[] ret = new String[raw.length];
		int index = 0;

		for (KeyValue kv : raw) {
			ret[index++] = new String(kv.getQualifier(), "UTF-8");
		}

		return ret[0];
	}
}
