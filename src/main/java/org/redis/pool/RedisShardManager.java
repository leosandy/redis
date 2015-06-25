package org.redis.pool;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class RedisShardManager {

	private static ShardedJedisPool pool = null;
	
	static{
		JedisPoolConfig config = new JedisPoolConfig();
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		shards.add(new JedisShardInfo("localhost"));
		pool = new ShardedJedisPool(config, shards);
	}
	
	public ShardedJedis getConnection(){
		return pool.getResource();
	}
	
	public void releaseConnection(ShardedJedis jedis){
		pool.returnResourceObject(jedis);
	}
	
}
