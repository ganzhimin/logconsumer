package com.zju.logservice.util;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.client.Client;

public class SetMapping {
	
	static XContentBuilder mapping =null;
	private static String TypeOfIndex;
	
	public SetMapping(String type) throws IOException{
		TypeOfIndex = type;
		mapping = jsonBuilder()
				.startObject()
            	.startObject(TypeOfIndex)
            		.startObject("properties");
	}
	public void addFieldStart(String field) throws IOException{
		mapping.startObject(field);
	}
	
	public void addFieldSetting(String key ,String value) throws IOException{
		mapping.field(key, value);
	}
	public void addFiledEnd() throws IOException{
		mapping.endObject();
	}
	public void setMappingFinished() throws IOException {
		mapping.endObject().endObject().endObject();
	}
	
	public void putMapping(String indexName){		
		Client client= ConnectES.getInstance().getClient();
		final IndicesExistsResponse res = client.admin().indices()
                .prepareExists(indexName).execute().actionGet();
        if (!res.isExists()) {
        	CreateIndexRequestBuilder createIndexRequestBuilder = client
                        .admin().indices().prepareCreate(indexName);
            createIndexRequestBuilder.addMapping(TypeOfIndex, mapping);
            createIndexRequestBuilder.execute().actionGet();
            System.out.println("put mapping succeed!");
        }
        else {
			System.out.println("mapping existed, fail to put!");
		}
	}
}
