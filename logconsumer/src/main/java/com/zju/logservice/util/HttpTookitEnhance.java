package com.zju.logservice.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class HttpTookitEnhance {
	//private static HttpClient client = new DefaultHttpClient();

	/**
	 * 
	 * @param url
	 * @param queryString
	 * @param charset
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String doGet(String url, String queryString, boolean pretty) throws Exception {
		HttpClient client = new DefaultHttpClient();
		StringBuffer response = new StringBuffer();
		HttpGet method = null;
		HttpResponse httpResponse;
		BufferedReader reader = null;
		try{
				
			if (queryString != null && !queryString.equals("")) {
				url = url + "?" + queryString;
			}
			System.out.println(url);
			 method=new HttpGet(url);
			 httpResponse = client.execute(method);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity httpEntity = httpResponse.getEntity();
				reader = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "utf-8"));
				String line;
				while ((line = reader.readLine()) != null) {
					if (pretty) {
						response.append(line).append(System.getProperty("line.separator"));
					} else {
						response.append(line);
					}
				}
				
			}
			
		}finally{
			if (reader != null) {
				reader.close();
			}
			if (method != null) {
				method.abort();
			}
			client.getConnectionManager().shutdown();
		}
		
		return response.toString();
	}
/*
	public static InputStream doGetImage(String url) throws Exception{
		InputStream in = null;
		HttpGet method = new HttpGet(url);
		HttpResponse httpResponse = client.execute(method);
		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			HttpEntity httpEntity = httpResponse.getEntity();
			in = httpEntity.getContent();
		}
		return in;
	}
	*/
	public static void main(String[] args) throws Exception {
		String queryString="appid="+"ef33f87f-25bb-4334-9807-4bfdd1c15db8";
		String patterns=HttpTookitEnhance.doGet("http://10.10.102.101:1234/cfWeb/rest/app/patterns"
				,queryString,true);
		System.out.print(patterns);
	}

	/**
	 * 
	 * @param url
	 * @param params
	 * @param charset
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String doPost(String url, Map<String, String> params, boolean pretty) throws ClientProtocolException,
			IOException {
		StringBuffer response = new StringBuffer();
		HttpClient client = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
		// 设置Http Post数据
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				requestParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		HttpEntity entity = new UrlEncodedFormEntity(requestParams, "utf-8");
		httpPost.setEntity(entity);
		HttpResponse httpResponse = client.execute(httpPost);
		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			HttpEntity httpEntity = httpResponse.getEntity();
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "utf-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				if (pretty) {
					response.append(line).append(System.getProperty("line.separator"));
				} else {
					response.append(line);
				}
			}
			reader.close();
		}
		
		return response.toString();
	}

	public static int doGet(String url) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(url);
		HttpResponse httpResponse = client.execute(method);
		
		return httpResponse.getStatusLine().getStatusCode();
	}
}
