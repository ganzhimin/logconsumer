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
public class LogConsumer{
	private ClientSessionFactory csf;
	private LogAnalyser analyser;
	private int consumerNum;
	
	public LogConsumer(String host, String port,String consumerNum) throws Exception{
		Map<String, Object> connParams = new HashMap<String, Object>();
		connParams.put(TransportConstants.HOST_PROP_NAME, "10.10.105.107");
		connParams.put(TransportConstants.PORT_PROP_NAME, "5445");
		TransportConfiguration config = new TransportConfiguration(NettyConnectorFactory.class.getName(), connParams);

		ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(config);
		serverLocator.setReconnectAttempts(-1);
		serverLocator.setConsumerWindowSize(-1);
		csf = serverLocator.createSessionFactory();
		
		
		CacheUtil cu = new CacheUtil();
		analyser = new LogAnalyser(cu);
		
		this.consumerNum = Integer.parseInt(consumerNum);
		
		Runtime.getRuntime().addShutdownHook(new ShutdownThread(csf,cu));
	}
		
	public void start() throws Exception{
		for(int i=0;i<this.consumerNum;i++){
			ClientSession session = csf.createTransactedSession();
			
			session.start();
			
			ClientConsumer consumer = session.createConsumer("jms.queue.sourceQueue");
			
			consumer.setMessageHandler(new MyMessageHandler(analyser,session));
		}			
	}
	
	public static void main(String[] args){
		try {
			new LogConsumer(args[0],args[1],args[2]).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("consumer starts failed!");
			System.exit(-1);
		}
		System.out.println("consumer starts successfully!");
		while(true);
	}
}
