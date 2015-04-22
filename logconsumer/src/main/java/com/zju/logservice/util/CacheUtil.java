package com.zju.logservice.util;

import java.net.URL;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class CacheUtil {

	private CacheManager cacheManager;
	private URL url = this.getClass().getClassLoader()
			.getResource("ehcache.xml");
	
	

	public CacheUtil() {
		System.setProperty("net.sf.ehcache.enableShutdownHook","true");
	}

	private Cache getCache() {
			//cacheManager =CacheManager.newInstance(url);
		cacheManager = CacheManager.create(url);
		return cacheManager.getCache("patternCache");
	}

	public void cacheData(String key, Object value) {
		Cache cache = getCache();
		Element element = new Element(key, value);
		cache.put(element);
		cache.flush();
		cacheManager.shutdown();
	}

	public void removeData(String key) {
		Cache cache = getCache();
		cache.remove(key);
		cacheManager.shutdown();
	}

	public Object getData(String key) {
		Cache cache = getCache();
		Element element=cache.get(key);
		Object obj = null;
		if(element!=null){
			obj = element.getObjectValue();
		}
		return obj;
	}

	public void shutdown() {
		cacheManager.shutdown();
	}

	public void persist() {
		Cache cache = getCache();
		cache.flush();
	}

	public static void main(String[] args) {
		//CacheUtil cu = new CacheUtil();
    	
    	//int size = 20;
    	//ExecutorService es = Executors.newFixedThreadPool(size);
    	
    	//for(int i=0;i<size;i++){
    		//es.execute(new MyThread(cu, "name", "laogan", "tid"+i));
    	//}
	}
}
