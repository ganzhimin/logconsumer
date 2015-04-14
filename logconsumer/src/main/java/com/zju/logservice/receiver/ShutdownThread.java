package com.zju.logservice.receiver;

import org.hornetq.api.core.client.ClientSessionFactory;

import com.zju.logservice.util.CacheUtil;



public class ShutdownThread extends Thread {
	private ClientSessionFactory csf;
	private CacheUtil cu;
	public ShutdownThread(ClientSessionFactory csf, CacheUtil cu){
		this.csf = csf;
		this.cu = cu;
	}
	@Override
	public void run() {
		System.out.println("Shutdown thread invoke");
		if(csf!=null){
			System.out.println("Close connection to hornetq");
			csf.close();
			System.out.println("Done");
		}
		if(cu!=null){
			System.out.println("Release pattern cache memory");
			cu.shutdown();
			System.out.println("Done");
		}
	}
	
}
