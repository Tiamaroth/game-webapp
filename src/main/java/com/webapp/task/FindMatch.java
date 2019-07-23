package com.webapp.task;

import java.io.Serializable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.webapp.event.MatchfindEvent;
import com.webapp.matchfinding.MatchfindingStatus;

/**
 * 
 * Dummy matchfinding - store in hazelcast map the matchfind event
 * 
 */

public class FindMatch implements Runnable, HazelcastInstanceAware, Serializable {
	
	
	private static final long serialVersionUID = 3744852697252702835L;
	private final Logger log = LoggerFactory.getLogger(FindMatch.class);
	
	private MatchfindEvent matchFind;
	private HazelcastInstance hInstance;
	private final String matchfindMapName = "matchfindMap";
	
	public FindMatch(String warzoneCaller, String warzoneReceiver, String user) {
		matchFind = new MatchfindEvent(user, MatchfindingStatus.START, warzoneCaller, warzoneReceiver);
	}
	
	
	@Override
	public void run(){
		log.info("Executing command FIND MATCH in Warzone {} FROM CALLER {} user {}", matchFind.getWarzoneReceiver(), matchFind.getWarzoneCaller() , matchFind.getUser());
		Map<String,MatchfindEvent> matchfindInProgress = hInstance.getMap(matchfindMapName);
		matchfindInProgress.put(matchFind.getUser(), matchFind);
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
		matchFind.updateStatus(MatchfindingStatus.FOUND);
		matchFind.setMatchId(new Random().nextInt(5));
		matchfindInProgress.put(matchFind.getUser(), matchFind);
		log.info("MATCH FOUND in Warzone {} FROM CALLER {} user {} ", matchFind.getWarzoneReceiver(), matchFind.getWarzoneCaller() , matchFind.getUser());
		
	}

	@Override
	public void setHazelcastInstance(HazelcastInstance hInstance) {
		this.hInstance = hInstance;
	}

}
