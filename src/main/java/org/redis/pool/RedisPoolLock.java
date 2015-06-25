package org.redis.pool;

import java.util.concurrent.TimeUnit;

import redis.clients.jedis.ShardedJedis;

public class RedisPoolLock {

	private RedisShardManager manager ;
	
	/**
	 * expire time seconds
	 */
	public static final int DEFAULT_EXPIRE = 10;
	
	/**
	 * time seconds
	 */
	public static final int DEFAULT_TIME_OUT = DEFAULT_EXPIRE;
	
	private int expire = DEFAULT_EXPIRE;
	
	private int timeout = DEFAULT_TIME_OUT;
	
	private String key;
	
	private boolean lock;
	
	public RedisPoolLock(RedisShardManager manager,String key){
		this.manager = manager;
		this.key = key;
	}

	public RedisPoolLock(RedisShardManager manager,String key,int expire){
		this(manager,key);
		this.expire = expire;
	}
	
	public RedisPoolLock(RedisShardManager manager,String key,int expire,int timeout){
		this(manager, key,expire);
		this.timeout = timeout;
	}
	
	
	public boolean acquireLock() throws InterruptedException{
		long timeout = TimeUnit.SECONDS.toMillis(this.timeout);
		
		while(!tryLock()){
			if(timeout < 0){
				return false;
			}
			timeout -= 100;
			synchronized (this) {
				this.wait(100);
			}
		}
		
		return true;
	}

	public boolean tryLock() {
		ShardedJedis jedis = this.manager.getConnection();
		try {
			long delay = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.timeout) + 1;
			if(jedis.setnx(key, String.valueOf(delay)) == 1){
				jedis.expire(key, expire);
				this.lock = true;
				return true;
			}
			
			String expireStr = jedis.get(key);
			//expire invoking getset
			if(expireStr != null && Long.parseLong(expireStr) < System.currentTimeMillis()){
				String currentV = jedis.getSet(key, String.valueOf(delay));
				System.out.println(String.format("getset key:%s,v:%s,currentThreadName :%s", key,currentV,Thread.currentThread().getName()));
				//race condition
				if(expireStr.equals(currentV)){
					jedis.expire(key, expire);
					this.lock = true;
					return true;
				}
			}
			
		} finally{
			this.manager.releaseConnection(jedis);
		}
		return false;
	}
	
	public void unlock(){
		if(lock){
			ShardedJedis jedisCommands = null;
			jedisCommands = this.manager.getConnection();
			if (jedisCommands == null){
	            throw new IllegalStateException("unlock failed, lock key is ["+ key +"], cause by :connection is null! ");
	        }
			jedisCommands.del(key);
			this.lock = false;
		}
	}
	
}
