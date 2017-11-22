package ch.ethz.inf.vs.ballgame;

import java.io.Serializable;

public class GameMessage implements Serializable{

	public enum MessageType{
		PAUSE_REQUEST,
		RESUME_REQUEST,
		LEAVE_REQUEST;
	}
	
	public GameMessage(MessageType type){
		messageType = type;
	}
	
	public MessageType messageType;
	
}
