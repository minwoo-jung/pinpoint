package com.profiler.common.util;

import org.apache.thrift.TBase;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.common.dto.thrift.JVMInfoThriftDTO;
import com.profiler.common.dto.thrift.RequestDataListThriftDTO;
import com.profiler.common.dto.thrift.RequestThriftDTO;
import com.profiler.common.dto.thrift.Span;

public class DefaultTBaseLocator implements TBaseLocator {

	private static final short JVM_INFO_THRIFT_DTO = 10;
	private static final short REQUEST_DATA_LIST_THRIFT_DTO = 20;
	private static final short REQUEST_THRIFT_DTO = 30;
	private static final short SPAN = 40;
	private static final short AGENT_INFO = 50;

	@Override
	public TBase<?, ?> tBaseLookup(short type) {
		switch (type) {
		case JVM_INFO_THRIFT_DTO:
			return new JVMInfoThriftDTO();
		case REQUEST_DATA_LIST_THRIFT_DTO:
			return new RequestDataListThriftDTO();
		case REQUEST_THRIFT_DTO:
			return new RequestThriftDTO();
		case SPAN:
			return new Span();
		case AGENT_INFO:
			return new AgentInfo();
		}
		throw new IllegalArgumentException("Unsupported type:" + type);
	}

	@Override
	public short typeLookup(TBase<?, ?> tbase) {
		if (tbase instanceof JVMInfoThriftDTO) {
			return JVM_INFO_THRIFT_DTO;
		}
		if (tbase instanceof RequestDataListThriftDTO) {
			return REQUEST_DATA_LIST_THRIFT_DTO;
		}
		if (tbase instanceof RequestThriftDTO) {
			return REQUEST_THRIFT_DTO;
		}
		if (tbase instanceof Span) {
			return SPAN;
		}
		if (tbase instanceof AgentInfo) {
			return AGENT_INFO;
		}
		throw new UnsupportedOperationException("Unsupported Type");
	}
}
