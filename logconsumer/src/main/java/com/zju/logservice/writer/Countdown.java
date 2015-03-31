package com.zju.logservice.writer;

public class Countdown {

	private long startPoint = System.currentTimeMillis();
	public long getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(long startPoint) {
		this.startPoint = startPoint;
	}

	private long milliSeconds;
	
	public Countdown(int seconds) {
		super();
		long t = System.currentTimeMillis();
		startPoint = t-t%3600000;
		milliSeconds = seconds*1000;
	}

	public boolean count() {
		long during = System.currentTimeMillis() - this.startPoint;
		if (during >= milliSeconds)
			return true;
		else
			return false;
	}
	
	public void resumeStartPoint(){
		long t = System.currentTimeMillis();
		this.startPoint = t-t%3600000;
		
	}
	
}
