package ch.ethz.inf.vs.controllerLib;

import java.io.Serializable;

public class ControllerMessage implements Serializable {

	/* TODO by Romy: If these are the messages you actually intend to send, please implement IMessage<Player>
	 * here. This coupling between actual messages and actual players (based on three methods to implement)
	 * is the only constraint the communication interface imposes on you.
	 */
	
	private static final long serialVersionUID = -7055355990885658225L;

	public enum MessageType {
		C_REGISTRATION_MESSAGE,
		C_REGISTRATION_OK,
		C_DISCONNECTED,
		C_POLL,
		C_BACK_TO_LOBBY,
		GAME_MESSAGE,
		START_GAME
	}
	
	public MessageType messageType;
	public Serializable message;
	public String messageString;
	
	public ControllerMessage (MessageType type, Serializable message){
		messageType = type;
		this.message = message;
	}
	
	public ControllerMessage (MessageType type, String message){
		messageType = type;
		messageString = message;
	}
	
}
