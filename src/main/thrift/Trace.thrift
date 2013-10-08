namespace java com.nhn.pinpoint.thrift.dto

struct TAnnotation {
  1: i32 key,

  2: optional string stringValue
  3: optional bool boolValue;
  4: optional i32 intValue;
  5: optional i64 longValue;
  6: optional i16 shortValue
  7: optional double doubleValue;
  8: optional binary binaryValue;
  9: optional byte byteValue;
}

struct TAgentKey {
    1: string agentId;
    2: string applicationName;
    3: i64 agentStartTime;
}

struct TSpanEvent {
  // spanEvent의 agentKey는 매우 특별한 경우에만 생성되므로 필드사이즈를 줄이기 위해 별도 객체로 분리.
  1: optional TAgentKey agentKey;

  17: optional i16 parentServiceType
  18: optional string parentEndPoint


  4: optional string traceAgentId
  5: optional i64 traceAgentStartTime;
  6: optional i64 traceTransactionSequence;

  7: optional i32 spanId
  8: i16 sequence

  9: i32 startElapsed
  10: i32 endElapsed

  11: optional string rpc
  12: i16 serviceType
  13: optional string endPoint

  14: list<TAnnotation> annotations

  15: optional i32 depth = -1
  16: optional i32 nextSpanId = -1

  20: optional string destinationId
  // address주소가 1개일 경우
  21: optional list<string> destinationAddress;
  // address주소가 2개이상일 경우
  //15: optional list<string> destinationAddressList;
}

struct TSpan {

  1: string agentId
  2: string applicationName
  3: i64 agentStartTime

  4: string traceAgentId
  5: i64 traceAgentStartTime;
  6: i64 traceTransactionSequence;

  7: i32 spanId
  8: optional i32 parentSpanId = -1

  // span 이벤트의 시작시간.
  9: i64 startTime
  10: i32 elapsed

  11: optional string rpc

  12: i16 serviceType
  13: optional string endPoint
  14: optional string remoteAddr

  15: list<TAnnotation> annotations
  16: optional i16 flag = 0

  17: optional i32 err

  18: optional list<TSpanEvent> spanEventList

  19: optional string parentApplicationName
  20: optional i16 parentApplicationType
  21: optional string acceptorHost
}

struct TSpanChunk {
  1: string agentId
  2: string applicationName
  3: i64 agentStartTime

  4: i16 serviceType

  5: string traceAgentId
  6: i64 traceAgentStartTime;
  7: i64 traceTransactionSequence;

  8: i32 spanId

  9: optional string endPoint

  10: list<TSpanEvent> spanEventList
}

struct TSqlMetaData {

    1: string agentId
    2: i64 agentStartTime

    4: i32 hashCode
    5: string sql;
}


struct TApiMetaData {
  1: string agentId
  2: i64 agentStartTime

  4: i32 apiId,
  5: string apiInfo,
  6: optional i32 line,
}

struct TResult {
  1: bool success
  2: optional string message
}