package com.zju.logservice.receiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
				File f = new File(System.getProperty("user.home")+"/logservice/bugs.txt");
				FileOutputStream fs = null;
				try {
					fs = new FileOutputStream(f,true);
					fs.write("\n=================================================\n".getBytes());
					StackTraceElement[] ss = e.getStackTrace();
					String res = "";
					for(StackTraceElement s:ss){
						res+=s.getClassName()+": ["+s.getLineNumber()+"] - "+s.getMethodName()+"\n";
					}
					
					
					fs.write((e.getMessage()+"\n"+res).getBytes());
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}finally{
					try {
						fs.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
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
