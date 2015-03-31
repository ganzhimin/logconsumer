package com.zju.logservice.receiver;


import org.apache.log4j.Logger;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.MessageHandler;
import com.zju.logservice.writer.LogAnalyser;

public class MyMessageHandler implements MessageHandler {
	private static final Logger logger = Logger.getLogger(MyMessageHandler.class);
	private LogAnalyser logAnalyser;
	private ClientSession session;
	
	public MyMessageHandler(LogAnalyser logAnalyser,ClientSession session){
		this.logAnalyser = logAnalyser;
		this.session = session;
	}
	
	@Override
	public void onMessage(ClientMessage message) {
		// TODO Auto-generated method stub
		int count = message.getIntProperty("number");
		String log = message.getStringProperty("log");
		logAnalyser.processMsg(log, count);
		try {
			message.acknowledge();
			session.commit();
		} catch (HornetQException e) {
			// TODO Auto-generated catch block
			logger.error("HornetQException,this session will be closed");
			try {
				session.close();
			} catch (HornetQException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//e.printStackTrace();
		}
	}

}
