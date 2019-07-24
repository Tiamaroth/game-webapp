package com.webapp.controller;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.client.impl.protocol.codec.TopicAddMessageListenerCodec;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import com.hazelcast.topic.impl.TopicService;
import com.webapp.event.MatchfindEvent;
import com.webapp.matchfinding.MatchfindingStatus;
import com.webapp.task.FindMatch;


@RestController
@RequestMapping(path = "/test")
public class FindMatchController {
	
	private final Logger log = LoggerFactory.getLogger(FindMatchController.class);
	
	private final HazelcastInstance hInstance;
	
	private final String warzoneMapName = "warzoneMap";
	
	private final String matchfindMapName = "matchfindMap";
	
	private final String matchIdHeader = "Match ID";
	
	@Value("${game.warzone.name}")
	String warzoneName;
	
	IExecutorService service;
	
	@Autowired
	public FindMatchController(HazelcastInstance hInstance) {
		this.hInstance = hInstance; //Hazelcast init
		
	}
	
	//We associate cluster member with warzone
	@PostConstruct
	public void postConstruct() { 
		Map<String,Member> warzoneMap = hInstance.getMap(warzoneMapName);
		
		log.info("This member is responsible for warzone named {}", warzoneName);
		
		warzoneMap.put(warzoneName, hInstance.getCluster().getLocalMember());
		service = hInstance.getExecutorService(warzoneName);
	}
	
	
	/**
	 * Forward the Findmatch command to the member who is responsible of the requested warzone
	 * @param warzoneName
	 * @param user
	 * @return bad request if error - created if ok
	 */
	@PostMapping(path = "find-match")
	public ResponseEntity<String> startFindMatch(@RequestParam String warzoneName, @RequestParam String user) {
		log.info("In warzone {} receiving request for warzone {}", this.warzoneName , warzoneName);
		Map<String,Member> warzoneMap = hInstance.getMap(warzoneMapName);
		Map<String,MatchfindEvent> matchfindmap = hInstance.getMap(matchfindMapName);
		HttpStatus status = HttpStatus.CREATED;
		String returnMessage = "Matchfind started"; 
		if(!warzoneMap.containsKey(warzoneName)) {
			log.error("Warzone {} not found", warzoneName);
			status = HttpStatus.BAD_REQUEST;
			returnMessage = String.format("Warzone %s not found", warzoneName);
		}else if(matchfindmap.containsKey(user)) {
			log.error("User {} is already searching", user);
			status = HttpStatus.BAD_REQUEST;
			returnMessage = String.format("User %s is already searching", user);
		}else {
			service.executeOnMember(new FindMatch(this.warzoneName , warzoneName, user),warzoneMap.get(warzoneName));
			
			log.info("Match finding started in warzone: {} for user: {}", warzoneName,user);
		}
		
		return new ResponseEntity<>(returnMessage,status) ;
	}
	
	/**
	 * In this method we check if a match has been found for the user 
	 * @param user
	 * @return response entity with the match id
	 */
	@GetMapping(path = "check-find-status")
	public ResponseEntity<String> checkFindStatus(@RequestParam String user) {
		log.info("Checking matchfind status for user {}", user);
		Map<String,MatchfindEvent> matchfindmap = hInstance.getMap(matchfindMapName);
		
		HttpStatus status = HttpStatus.NOT_FOUND;
		String returnMessage = "match not found"; 
		Map<String,Member> warzoneMap = hInstance.getMap(warzoneMapName);
		if(!warzoneMap.containsKey(warzoneName)) {
			log.error("Warzone {} not found", warzoneName);
			status = HttpStatus.BAD_REQUEST;
			returnMessage = String.format("Warzone %s not found", warzoneName);
		}else if(!matchfindmap.containsKey(user)) {
			log.error("User {} is not searching for a match", user);
			status = HttpStatus.BAD_REQUEST;
			returnMessage = String.format("User %s is not searching for a match", user);
		}else {
		
			switch (matchfindmap.get(user).getStatus()) {
			case FOUND:
				status = HttpStatus.FOUND;
				returnMessage = String.valueOf(matchfindmap.get(user).getMatchId());
				log.info("MATCH FOUND WITH ID {}",returnMessage);
				matchfindmap.remove(user);
				break;
	
			default:
				break;
			}
		}
			
		return ResponseEntity.status(status).body(returnMessage);
		
	}

}
