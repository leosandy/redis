package org.redis.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.redis.pool.RedisPoolLock;
import org.redis.pool.RedisShardManager;

import redis.clients.jedis.Jedis;

public class RedisLockTest {
	
/*	private RedisPoolLock lock;
	private RedisShardManager manager;*/
	/*public void before(){
		manager = new RedisShardManager();
		this.lock = new RedisPoolLock(manager, "pool");
	}
	
	public void testLock(){
		for(int i = 0; i < 100;i++){
			try {
				if (lock.acquireLock()) {
					System.out.println(String.format("locked time :%s", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
					Jedis jedis = manager.getConnection().getAllShards().iterator().next();
					display(jedis);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	*/
	
	public static void display(Jedis jedis){
		Set<String> keys = jedis.keys("*");
		StringBuilder sb = new StringBuilder();
		for (String string : keys) {
			sb.append(string).append("=").append(jedis.get(string)).append(",");
		}
		sb = sb.deleteCharAt(sb.length() - 1);
		System.out.println(sb.toString());
	}
	
	public static void main(String[] args) {

		final RedisShardManager manager = new RedisShardManager();
		final RedisPoolLock lock = new RedisPoolLock(manager, "lock");
		for(int i = 0 ; i < 100;i++){
			new Thread(new Runnable() {
				
				public void run() {
					try {
						if (lock.acquireLock()) {
							System.out.println(String.format("locked time :%s", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
							Jedis jedis = manager.getConnection().getAllShards().iterator().next();
							display(jedis);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			},"Thread-" + i).start();
		}
	
	}
	
}
