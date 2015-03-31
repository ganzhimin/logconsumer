package com.zju.logservice.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.*;


public class ConnectES {
	
	private final static String ESCONNECT_PROPERTIES = "esconnect.properties";
	
	private String clusterName="CFLog";
	private String clientModel="TRANSPORT";
	private String host1;
	private String host2;
	private String host3;
	private int port=9301;
	Settings settings;
	
	private Client client =null;
	private static ConnectES instance = null;
	
	private ConnectES() {
		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream(ESCONNECT_PROPERTIES);
		Properties props = new Properties();
		try {
			props.load(in);
			clusterName= props.getProperty("clustername");
			clientModel= props.getProperty("clientModel");
			host1= props.getProperty("host1");
			host2= props.getProperty("host2");
			/*host3= props.getProperty("host3");*/
			port= Integer.parseInt(props.getProperty("port"));
			
			if(client==null){
				if(clientModel.equals("NODE")){
					Node node = nodeBuilder().clusterName(clusterName).client(true).node();
					client = node.client();
				}
				else if(clientModel.equals("TRANSPORT")){
					settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName).build(); 
					client= new TransportClient(settings)
						.addTransportAddress(new InetSocketTransportAddress(host1,port))
						.addTransportAddress(new InetSocketTransportAddress(host2,port));
						/*.addTransportAddress(new InetSocketTransportAddress(host3,port));*/
				}
				else {
					System.out.println("clientModel is not correct.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ConnectES getInstance(){
		if(instance == null)
			instance = new ConnectES();
		return instance;
	}
	
	public Client getClient(){
		return this.client;
	}
}
