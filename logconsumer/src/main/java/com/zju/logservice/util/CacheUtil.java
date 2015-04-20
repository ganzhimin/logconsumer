package com.zju.logservice.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
		try{
			//cacheManager =CacheManager.newInstance(url);
			cacheManager = CacheManager.create(url);
		}catch(Exception e){
			e.printStackTrace();
			File f = new File(System.getProperty("user.home")+"/logservice/bugs.txt");
			FileOutputStream fs = null;
			try {
				fs = new FileOutputStream(f,true);
				fs.write("\n=================================================\n".getBytes());
				StackTraceElement[] ss = e.getStackTrace();
				String res = "";
				for(StackTraceElement s:ss){
					res+=s.getClassName()+": ["+s.getLineNumber()+"] - "+s.getMethodName()+"\n";
				}
				
				
				fs.write((e.getMessage()+"\n"+res).getBytes());
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}finally{
				try {
					fs.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
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
