package org.redis.example;

import redis.clients.jedis.Jedis;

public class RedisSimpleTest {

	public static void main(String[] args) {
		Jedis jedis = new Jedis("localhost");
	    jedis.connect();
	    jedis.set("foo", "bar");
	    String value = jedis.get("foo");
	    System.out.println(value);
	    
	    System.out.println(jedis.get("counter"));
	    jedis.incr("counter");
	    System.out.println(jedis.get("counter"));
	    String cacheKey = "cachekey";

	    jedis.set(cacheKey,"cached key");
	    jedis.expire(cacheKey, 2);
	    System.out.println(jedis.ttl(cacheKey));
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    System.out.println("ttl:"+jedis.ttl(cacheKey));
	    System.out.println("Cached Value:" + jedis.get(cacheKey));

	    try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    System.out.println("ttl:"+jedis.ttl(cacheKey));
	    System.out.println("Expired Key:" + jedis.get(cacheKey));

	    jedis.close();
	}
}
