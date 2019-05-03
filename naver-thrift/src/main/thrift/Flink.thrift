namespace java com.navercorp.pinpoint.thrift.dto.flink

struct TFSpanStat {
    1: string          organization
    2: string          applicationId
    3: string          agentId
    4: i64             spanTime
    5: i64             count
}

struct TFSpanStatBatch {
    1: list<TFSpanStat> tFSpanStatList
}