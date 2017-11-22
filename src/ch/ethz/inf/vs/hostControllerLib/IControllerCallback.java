package ch.ethz.inf.vs.hostControllerLib;

import android.os.Handler;

public interface IControllerCallback<S,T> {

	/**
	 * The method that will be called if a message was received from the controller's host
	 * @param message The received message
	 */
	public void onReceive(T message);
	
	public void onInvalidMessageReceived();
	
	/**
	 * The method that will be called if a connection attempt of the controller was successful.
	 */
	public void onConnectionEstablished();
	
	public void onDisconnect();
	
	
	/**
	 * Returns a handler the called methods of this object shall be posted to
	 * @return A handler the called methods of this object shall be posted to
	 */
	public Handler getCallbackHandler();
}
