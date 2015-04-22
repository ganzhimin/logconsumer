package com.zju.logservice.receiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private List<ClientSession> lcs;
	
	public LogConsumer(String host, String port,String consumerNum) throws Exception{
		Map<String, Object> connParams = new HashMap<String, Object>();
		connParams.put(TransportConstants.HOST_PROP_NAME, host);
		connParams.put(TransportConstants.PORT_PROP_NAME, port);
		TransportConfiguration config = new TransportConfiguration(NettyConnectorFactory.class.getName(), connParams);

		ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(config);
		serverLocator.setReconnectAttempts(-1);
		serverLocator.setConsumerWindowSize(-1);
		serverLocator.setBlockOnAcknowledge(false);
		
		csf = serverLocator.createSessionFactory();
		lcs = new ArrayList<ClientSession>();
		int num = Integer.parseInt(consumerNum);
		for(int i=0;i<num;i++){
			ClientSession session = csf.createTransactedSession();
			lcs.add(session);
		}
		
		CacheUtil cu = new CacheUtil();
		analyser = new LogAnalyser(cu);

		Runtime.getRuntime().addShutdownHook(new ShutdownThread(csf,lcs));
	}
		
	public void start() throws Exception{
		for(ClientSession cs:lcs){
			cs.start();
			ClientConsumer consumer = cs.createConsumer("jms.queue.sourceQueue");
			consumer.setMessageHandler(new MyMessageHandler(analyser,cs));
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
