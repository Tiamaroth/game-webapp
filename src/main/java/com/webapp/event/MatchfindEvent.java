package com.webapp.event;

import java.io.Serializable;

import com.webapp.matchfinding.MatchfindingStatus;

/**
 * 
 * This object is used to keep track of the status of matchfinding for a user
 * 
 */
public class MatchfindEvent implements Serializable{
	
	
	private static final long serialVersionUID = 1L;

	private String user;
	
	private String warzoneCaller;
	
	private String warzoneReceiver;
	
	private MatchfindingStatus status;
	
	private int matchId;
	
	public MatchfindEvent( String user,  MatchfindingStatus status,  String warzoneCaller ,  String warzoneReceiver) {
		this.user = user;
		this.status = status;
		this.warzoneCaller = warzoneCaller;
		this.warzoneReceiver = warzoneReceiver;
	}

	public String getUser() {
		return user;
	}

	public MatchfindingStatus getStatus() {
		return status;
	}

	public String getWarzoneCaller() {
		return warzoneCaller;
	}

	public String getWarzoneReceiver() {
		return warzoneReceiver;
	}

	public void updateStatus(MatchfindingStatus newStatus) {
		this.status = newStatus;
	}

	public int getMatchId() {
		return matchId;
	}

	public void setMatchId(int matchId) {
		this.matchId = matchId;
	}

	@Override
	public String toString() {
		return "MatchfindEvent [user=" + user + ", warzoneCaller=" + warzoneCaller + ", warzoneReceiver="
				+ warzoneReceiver + ", status=" + status + ", matchId=" + matchId + "]";
	}
	
	
	
}
