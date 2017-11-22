package ch.ethz.inf.vs.ballgame;

import java.io.Serializable;

public class LobbyMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2332710819627364900L;
	
	
	public enum MessageType {
		CHANGE_COLOR,
		COLOR_CHANGE_REQUEST
	}
	
	public LobbyMessage(MessageType type){
		messageType = type;
	}
	
	public MessageType messageType;
	public int color;
	
}
