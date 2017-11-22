package ch.ethz.inf.vs.hostControllerLib;

import android.os.Handler;

public interface IHostCallback<S,T> {

	/**
	 * The method that will be called when a message is received from a controller
	 * @param message The received message
	 * @param player The identifier that represents the controller the message came from
	 */
	public void onReceive(T message, S identifier);
	
	public void onInvalidMessageReceived(S identifier);
	
	public void onControllerDisconnect(S identifier);
	
	public void onControllerReconnect(S identifier);
	
	/**
	 * The method that will be called when a new controller connected to the host
	 * @param identifier Identifies the newly connected controller
	 */
	public void onConnectionAccepted (S identifier);
	
	/**
	 * Returns a handler the called methods of this object shall be posted to
	 * @return A handler the called methods of this object shall be posted to
	 */
	public Handler getCallbackHandler();
}
