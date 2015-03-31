package com.zju.logservice.receiver;

import java.util.HashMap;
import java.util.Map;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;

import com.zju.logservice.util.CacheUtil;
import com.zju.logservice.writer.LogAnalyser;


/**
 * @author laogan
 *
 */
public class HornetConsumer{
	private ClientSessionFactory csf;
	
	
	public HornetConsumer(String host, String port,String consumerNum){
		Map<String, Object> connParams = new HashMap<String, Object>();
		connParams.put(TransportConstants.HOST_PROP_NAME, "10.10.105.107");
		connParams.put(TransportConstants.PORT_PROP_NAME, "5445");
		TransportConfiguration config = new TransportConfiguration(NettyConnectorFactory.class.getName(), connParams);

		ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(config);
		
		LogAnalyser analyser = new LogAnalyser(new CacheUtil());
		
		try {
			csf = serverLocator.createSessionFactory();
			
			int csmNum = Integer.parseInt(consumerNum);
			
			for(int i=0;i<csmNum;i++){
				ClientSession session = csf.createTransactedSession();
				
				session.start();
				
				ClientConsumer consumer = session.createConsumer("jms.queue.sourceQueue");
				
				consumer.setMessageHandler(new MyMessageHandler(analyser,session));
			}			
		}  catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ClientSessionFactory getClientSessionFactory(){
		return csf;
	}
	public static void main(String[] args){
		HornetConsumer hc = null;
		hc =new HornetConsumer(args[0],args[1],args[2]);
		Runtime.getRuntime().addShutdownHook(new ShutdownThread(hc.getClientSessionFactory()));
		System.out.println("consumer starts successfully!");
		while(true);
	}
}
