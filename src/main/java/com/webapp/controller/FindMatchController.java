package com.webapp.controller;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.webapp.task.FindMatch;


@RestController
@RequestMapping(path = "/test")
public class FindMatchController {
	
	private final Logger log = LoggerFactory.getLogger(FindMatchController.class);
	
	private final HazelcastInstance hInstance;
	
	private final String warzoneMapName = "warzoneMap";
	
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
	 * Forward the Findmatch tast to the member who is responsible of the requested warzone
	 * @param warzoneName
	 * @param user
	 * @return placeholder matchId - null if errors happen
	 */
	@GetMapping(path = "find-match")
	public Integer startFindMatch(@RequestParam String warzoneName, @RequestParam String user) {
		log.info("In warzone {} receiving request for warzone {}", this.warzoneName , warzoneName);
		Integer matchId = null ;
		Map<String,Member> warzoneMap = hInstance.getMap(warzoneMapName);
		if(!warzoneMap.containsKey(warzoneName)) {
			log.error(String.format("Warzone {} not found", warzoneName));
		}else {
			Future<Integer> matchSearch = service.submitToMember(new FindMatch(this.warzoneName , warzoneName),warzoneMap.get(warzoneName));
			try {
				matchId = matchSearch.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			log.info("Match finding started in warzone: {} for user: {}", warzoneName,user);
		}
		
		return matchId;
	}
	


}
