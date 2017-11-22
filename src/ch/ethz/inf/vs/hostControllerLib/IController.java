package ch.ethz.inf.vs.hostControllerLib;

public interface IController<S, T> {
	
	/**
	 * Attempts to register this controller at its host
	 * @param callback The callback to be called on message receive
	 */
	public void connectWithHost(IControllerCallback<S,T> callback);
	
	/**
	 * Sends a message to the controller's host
	 * @param message The message to be sent.
	 */
	public boolean send(T message);
	
	/**
	 * Changes the current controller's callback to a new callback
	 * @param callback The new callback for this controller
	 */
	public void setNewCallback(IControllerCallback<S,T> callback);
	
	/**
	 * Pauses this controller's receiving function until it is resumed 
	 */
	public void pauseReceiving();
	
	/**
	 * Resumes this controller's receiving function
	 */
	public void resumeReceiving();
	
	/**
	 * Closes this controller
	 */
	public void close();
	
}
