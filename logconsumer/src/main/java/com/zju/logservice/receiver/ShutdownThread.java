package com.zju.logservice.receiver;

import java.util.List;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;




public class ShutdownThread extends Thread {
	private ClientSessionFactory csf;
	private List<ClientSession> lcs;
	public ShutdownThread(ClientSessionFactory csf, List<ClientSession> lcs){
		this.csf = csf;
		this.lcs = lcs;
	}
	@Override
	public void run() {
		System.out.println("Shutdown thread invoke");
		
		System.out.println("Close consumer sessions");
		for(ClientSession cs:lcs){
			try {
				if(cs!=null)
					cs.close();
			} catch (HornetQException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Done");
		
		if(csf!=null){
			System.out.println("Close connection to hornetq");
			csf.close();
			System.out.println("Done");
		}
	}
	
}
