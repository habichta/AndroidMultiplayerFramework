package ch.ethz.inf.vs.hostControllerLib;

import java.util.List;

public interface IHost<S,T> {
	
	/**
	 * Starts accepting controllers.
	 */
	public boolean startAcceptingControllers(IHostCallback<S,T> callback, IIdentifierFactory<S> identifierFactory);
	
	/**
	 * Stops accepting controllers.
	 */
	public void stopAcceptingControllers();
	
	/**
	 * Sends a message to a controller
	 * @param message The message to be sent.
	 * @param identifier Identifiers the controller the message shall be sent to.
	 */
	public boolean sendTo(T message, S identifier);
	
	/**
	 * Returns a list of all controller identifiers
	 * @return A list of all controller identifiers
	 */
	public List<S> getControllerIdentifiers();
	
	/**
	 * Changes the current host's callback to a new callback
	 * @param callback The new callback for this host
	 */
	public void setNewCallback(IHostCallback<S,T> callback);
	
	/**
	 * Pauses this host's receiving function until it is resumed 
	 */
	public void pauseReceiving();
	
	/**
	 * Resumes this host's receiving function
	 */
	public void resumeReceiving();
	
	/**
	 * Closes this controller
	 */
	public void close();
	
	/**
	 * Disconnects and removes a controller
	 * @param identifier Identifies the controller to be removed
	 */
	public void removeController(S identifier);

}
