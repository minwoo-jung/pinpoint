namespace java com.navercorp.pinpoint.thrift.dto.command

struct TCmdGetAuthenticationToken {
    1: string userId
    2: string password
    3: optional TTokenType tokenType
}

struct TCmdGetAuthenticationTokenRes {
    1: TTokenResponseCode code
    2: optional string message
    3: optional binary token
}

enum TTokenType {
    ALL = 0,
    SPAN = 1,
    STAT = 2,

    UNKNOWN = -1;
}

enum TTokenResponseCode {
    OK = 200,

    BAD_REQUEST = 400,
    UNAUTHORIZED = 401,
    INTERNAL_SERVER_ERROR = 500;
}
