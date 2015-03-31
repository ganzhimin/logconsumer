package com.zju.logservice.receiver;

import org.hornetq.api.core.client.ClientSessionFactory;



public class ShutdownThread extends Thread {
	private ClientSessionFactory csf;
	public ShutdownThread(ClientSessionFactory csf){
		this.csf = csf;
	}
	@Override
	public void run() {
		if(csf!=null){
				System.out.println("close connection...");
				csf.close();
			}
	}
	
}
