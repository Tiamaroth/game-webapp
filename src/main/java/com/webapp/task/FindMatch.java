package com.webapp.task;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FindMatch implements Callable<Integer>, Serializable {
	
	
	private static final long serialVersionUID = 3744852697252702835L;
	private final Logger log = LoggerFactory.getLogger(FindMatch.class);
	
	private String warzoneCaller;
	private String warzoneReceiver;
	public FindMatch(String warzoneCaller, String warzoneReceiver) {
		this.warzoneCaller = warzoneCaller;
		this.warzoneReceiver = warzoneReceiver;
	}
	@Override
	public Integer call() throws Exception {
		log.info("Executing TASK FIND MATCH in Warzone {} FROM CALLER {} ", warzoneReceiver, warzoneCaller );
		return 1;
	}

}
