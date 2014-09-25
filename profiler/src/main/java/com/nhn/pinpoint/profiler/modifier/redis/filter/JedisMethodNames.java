package com.nhn.pinpoint.profiler.modifier.redis.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Jedis method names
 * 
 * @author jaehong.kim
 *
 */
public class JedisMethodNames {

    private static Set<String> names = null;
    
    
    public static Set<String> get() {
        if(names != null) {
            return names;
        }
        
        final String[] methodNames = { 
                "get",
                "type",
                "append",
                "keys",
                "set",
                "exists",
                "sort",
                "rename",
                "hvals",
                "scan",
                "hexists",
                "hmget",
                "hincrBy",
                "del",
                "randomKey",
                "renamenx",
                "expire",
                "expireAt",
                "move",
                "ttl",
                "mget",
                "getSet",
                "setnx",
                "mset",
                "setex",
                "hlen",
                "hkeys",
                "hdel",
                "zrangeWithScores",
                "zrevrangeWithScores",
                "zrangeByScoreWithScores",
                "zrevrangeByScore",
                "zrevrangeByScoreWithScores",
                "zremrangeByRank",
                "zremrangeByScore",
                "objectRefcount",
                "objectEncoding",
                "objectIdletime",
                "incrBy",
                "decr",
                "incr",
                "decrBy",
                "msetnx",
                "hset",
                "substr",
                "hget",
                "hsetnx",
                "hmset",
                "hgetAll",
                "rpush",
                "lpush",
                "llen",
                "lrange",
                "ltrim",
                "lindex",
                "lset",
                "lrem",
                "lpop",
                "rpop",
                "rpoplpush",
                "sadd",
                "smembers",
                "srem",
                "spop",
                "smove",
                "scard",
                "sismember",
                "sinter",
                "sinterstore",
                "sunion",
                "sunionstore",
                "sdiff",
                "sdiffstore",
                "srandmember",
                "zadd",
                "zrange",
                "zrem",
                "zincrby",
                "zrank",
                "zrevrank",
                "zrevrange",
                "zcard",
                "zscore",
                "watch",
                "blpop",
                "brpop",
                "zcount",
                "zrangeByScore",
                "zunionstore",
                "zinterstore",
                "strlen",
                "lpushx",
                "persist",
                "rpushx",
                "echo",
                "linsert",
                "brpoplpush",
                "setbit",
                "getbit",
                "setrange",
                "getrange",
                "configGet",
                "configSet",
                "eval",
                "subscribe",
                "publish",
                "psubscribe",
                "evalsha",
                "scriptExists",
                "scriptLoad",
                "slowlogGet",
                "bitcount",
                "bitop",
                "dump",
                "restore",
                "pexpire",
                "pexpireAt",
                "pttl",
                "incrByFloat",
                "psetex",
                "clientKill",
                "clientSetname",
                "migrate",
                "hincrByFloat",
                "hscan",
                "sscan",
                "zscan",
                "shutdown",
                "debug",
                "save",
                "sync",
                "time",
                "select",
                "resetState",
                "configResetStat",
                "randomBinaryKey",
                "monitor",
                "unwatch",
                "slowlogReset",
                "slowlogLen",
                "ping",
                "quit",
                "flushDB",
                "dbSize",
                "flushAll",
                "auth",
                "bgsave",
                "bgrewriteaof",
                "lastsave",
                "slaveof",
                "slaveofNoOne",
                "getDB",
                "multi",
                "scriptFlush",
                "scriptKill",
                "clientGetname",
                "clientList",
                "slowlogGetBinary",
                "info"
            };
        
        names = new HashSet<String>(Arrays.asList(methodNames));
        return names;
    }
}