package org.redis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import redis.clients.jedis.Jedis;

public class RedisLock {

	private int timeout = 10 * 1000;
	private int expire = 10 * 1000;
	private final Jedis jedis;
	private final String lockKey;
	private boolean lock = false;
	public RedisLock(final Jedis jedis,final String key,int timeout,int expire){
		this(jedis,key);
		this.timeout = timeout;
		this.expire = expire;
	}
	
	public RedisLock(final Jedis jedis ,final String key){
		this.jedis = jedis;
		this.lockKey = key;
	}
	
	public synchronized boolean acquire() throws InterruptedException{
		int timeout = this.timeout;
		while (!tryLock()) {
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
		try {
				long expireTime = System.currentTimeMillis() + this.timeout + 1;
				//SETNX
				if(this.jedis.setnx(lockKey,String.valueOf(expireTime)) == 1){
					this.jedis.expire(lockKey, expire);
					display();
					System.out.println(String.format("key:%s,value:%s", lockKey,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date(Long.parseLong(this.jedis.get(lockKey))))));
					this.lock = true;
					return true;
				}
				
				String expireStr = this.jedis.get(lockKey);
				if(expireStr != null && Long.parseLong(expireStr) < System.currentTimeMillis()){
					String currentStr = this.jedis.getSet(lockKey, String.valueOf(expireTime));
					if(expireStr.equals(currentStr)){
						this.jedis.expire(lockKey, expire);
						display();
						System.out.println(String.format("getset type is key:%s,value:%s", lockKey,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date(Long.parseLong(this.jedis.get(lockKey))))));
						this.lock = true;
						return true;
					}
				}
				System.out.println(String.format("after type is key:%s,value:%s", lockKey,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date(Long.parseLong(this.jedis.get(lockKey))))));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			release();
		}
		return false;
	}
	
	public synchronized void release(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(lock){
			this.jedis.del(lockKey);
			this.lock = false;
		}
	}
	
	public void display(){
		Set<String> keys = this.jedis.keys("*");
		StringBuilder sb = new StringBuilder();
		for (String string : keys) {
			sb.append(string).append("=").append(this.jedis.get(string)).append(",");
		}
		sb = sb.deleteCharAt(sb.length() - 1);
		System.out.println(sb.toString());
	}
	
	public static void main(String[] args) {
		
		try {
			for(int i = 0; i < 100;i++){
				new Thread(new Runnable() {
					
					public void run() {
						try {
							Jedis jedis = new Jedis("localhost");
							System.out.println(String.format("jedis ping %s", jedis.ping()));
							final RedisLock lock = new RedisLock(jedis,"lock");
							if(lock.acquire()){
								System.out.println("lock is successful");
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
