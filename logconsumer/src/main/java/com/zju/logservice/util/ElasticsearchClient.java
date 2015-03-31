package com.zju.logservice.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
/*
 * author:haochengsong
 * time:2014.8.8 16:00
 */
public class ElasticsearchClient {
	private static final Logger logger = Logger.getLogger(ElasticsearchClient.class);
	private static String JSONINDEX = "logs";
	private static String LOGINDEX = "logfiles";
	private static String DTYPE = "traditional";
	
	private static Client client;
	private static ElasticsearchClient instance = null;
	private static Object lock = new Object();
	private ElasticsearchClient(){
		client =ConnectES.getInstance().getClient();
		logger.info("init es client success!");
	}
	
	public static ElasticsearchClient getInstance(){
		if(instance == null){
			synchronized(lock){
				if(instance == null)
					instance = new ElasticsearchClient();
			}
		}
		return instance;
	}
	/**
     * 上传单条json数据
     * @param json : String
     */
	public void uploadJson(Map<String, Object> json){
		uploadJson( json , JSONINDEX , DTYPE);
	}
    /**
     * 上传单条json数据
     * @param indexName : String
     * @param type : String
     * @param json : String
     */
    public void uploadJson( Map<String, Object> json,String indexName, String type) {
         client.prepareIndex(indexName, type)
                .setSource(json).execute().actionGet();
    }

    /**
     * 上传单条log数据
     * @param type : String
     */
    public void uploadLogfile(HashMap<String, Object> log){
    	uploadLogfile(log,LOGINDEX, DTYPE);
    }
    
    /**
     * 上传单条logfile数据
     * @param type : String
     */
    public void uploadLogfile(HashMap<String, Object> log,String type){
    	uploadLogfile(log,LOGINDEX, type);
    }
    /**
     * 上传单条log数据
     * @param log : String
     * @param indexName : String
     * @param type : String
     */
    public void uploadLogfile(HashMap<String, Object> log,String indexName,String type){
    	client.prepareIndex(indexName, type)
    			.setSource(log).execute().actionGet();
    }
    
    /**
     * 设定指点field为date格式，
     * @param indexName : String
     * @param type : String
     * @param field : String
     * @throws IOException 
     */
    public void initLogfiles(String indexName, String type,String... fields) throws IOException {	
    	SetMapping mapping = new SetMapping(type);
        
    	mapping.addFieldStart(fields[0]);
		mapping.addFieldSetting("type", "date");
		mapping.addFieldSetting("format", DateUtils.DEFAULT_FORMAT);
		mapping.addFiledEnd();
		
		mapping.addFieldStart(fields[1]);
		mapping.addFieldSetting("type", "string");
		mapping.addFieldSetting("index", "not_analyzed");
		mapping.addFiledEnd();
		
		mapping.addFieldStart(fields[2]);
		mapping.addFieldSetting("type", "double");
		mapping.addFiledEnd();
		
		mapping.addFieldStart(fields[3]);
		mapping.addFieldSetting("type", "string");
		mapping.addFieldSetting("analyzer", "ik");
		mapping.addFiledEnd();
		
		mapping.setMappingFinished();
		mapping.putMapping(indexName);
    }
    
    private void initLogs(String indexName, String type, String... fields) throws IOException{	
    	XContentBuilder mapping = jsonBuilder()
	    		.startObject()
	            .startObject(type)
	            .startObject("properties")
	            .startObject(fields[0])
	            .field("type","date")
	            .field("format",DateUtils.DEFAULT_FORMAT)
	            .endObject()
	            .endObject().endObject().endObject();
		Client client= ConnectES.getInstance().getClient();
		final IndicesExistsResponse res = client.admin().indices()
                .prepareExists(indexName).execute().actionGet();
        if (!res.isExists()) {
        	CreateIndexRequestBuilder createIndexRequestBuilder= client
		                .admin().indices().prepareCreate(indexName).setSettings("{\"index\":{\"analysis\":{\"analyzer\":{\"default\":{\"type\":\"keyword\"}}}}}");
        	createIndexRequestBuilder.addMapping(type, mapping);
            createIndexRequestBuilder.execute().actionGet();
            logger.info("init logs success");
        }
        else {
        	logger.info("index logs exist");
		}
	}
    
    public static void main( String[] args ){
    	//String[] fields = {"timestamp","fileName"};
    	try {
    		/*init(JSONINDEX,DTYPE,"timestamp","fileName","reponseTime","eventType");
			init(LOGINDEX,DTYPE,"timestamp","fileName","responseTime","eventType");*/
    		long start=System.currentTimeMillis();
    		/*File file=new File("/home/yufangjiang/appLogs/cfWeb_info.log.2014-10-28");
    		BufferedReader reader=new BufferedReader(new FileReader(file));
    		String ct;
    		int i=0;
    		while((ct=reader.readLine())!=null){
    			i++;
    			if(i<=10000){
    				HashMap< String, Object> jsonHashMap=new HashMap<String, Object>();
            		jsonHashMap.put("message",ct);
                	jsonHashMap.put("fileName", "2014-07-19-9 10");
                	jsonHashMap.put("timestamp", "2014-09-10 15:38:07");
                	jsonHashMap.put("companyName", "test");
                	ElasticsearchClient.getInstance().uploadJson(jsonHashMap,"logtest",DTYPE);
    			}else{
    				break;
    			}
        			
    		}*/
    		for(int i=0;i<100;i++){
    			HashMap< String, Object> jsonHashMap=new HashMap<String, Object>();
        		jsonHashMap.put("message","12");
            	jsonHashMap.put("fileName", "2014-07-19-9 10");
            	jsonHashMap.put("timestamp", "2014-09-10 15:38:07");
            	jsonHashMap.put("companyName", "test");
            	ElasticsearchClient.getInstance().uploadJson(jsonHashMap,"logtest",DTYPE);
    		}
    		System.out.println(System.currentTimeMillis()-start);
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
}
