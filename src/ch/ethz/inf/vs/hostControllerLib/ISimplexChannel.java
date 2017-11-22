package ch.ethz.inf.vs.hostControllerLib;

import ch.ethz.inf.vs.hostControllerLib.bluetooth.ComException;


/**
 * This interface represents a communication channel that communicates via a certain type of messages.
 * @author Romy Profanter
 *
 * @param <T> The type of message this channel sends and receives
 */
public interface ISimplexChannel<T,E extends Exception> {

	/**
	 * Sends a message to the channel.
	 * @param message The message to be sent
	 * @throws ComException
	 */
	public void send(T message) throws E;
	
	/**
	 * Receives a message from the channel
	 * @return The received message
	 * @throws ComException
	 */
	public T receive() throws E; 
	
	/**
	 * Closes this channel
	 */
	public void close() throws E;
}
