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
		cacheManager =CacheManager.newInstance(url);
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
		if(element!=null){
			return element.getObjectValue();
		}
		return null;
	}

	public void shutdown() {
		cacheManager.shutdown();
	}

	public void persist() {
		Cache cache = getCache();
		cache.flush();
	}

	public static void main(String[] args) {
		CacheUtil cacheUtil = new CacheUtil();

		cacheUtil.cacheData("test1", "test2");
		String patterns = (String) cacheUtil.getData("test");
		// for(String pattern:patterns){
		System.out.println(patterns);
		// }

	}
}
