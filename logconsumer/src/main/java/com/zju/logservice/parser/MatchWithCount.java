package com.zju.logservice.parser;

import oi.thekraken.grok.api.Match;

public class MatchWithCount extends Match {
	private int count=0;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
