package ch.ethz.inf.vs.hostControllerLib;

import java.util.List;

import ch.ethz.inf.vs.hostControllerLib.bluetooth.ComException;

/**
 * 
 * @author Romy Profanter
 *
 * @param <S> The type of identifier by which the specific subchannels can be identified
 * @param <T> The type of message this channel sends and receives
 */
public interface IMultiplexChannel<S, T, E extends Exception>{
	
	/**
	 * Broadcasts a message
	 * @param message The message to be sent
	 * @throws ComException
	 */
	public void broadcast(T message) throws E;
	
	/**
	 * Close this channel
	 * @throws E 
	 */
	public void close() throws E;


	/**
	 * Adds a channel with an identifier to this multiplexer. 
	 * The identifier is used to address the channel afterwards.
	 * @param channelIdentifier The object to identify the added channel later on.
	 * To be used e.g. in calls to {@link #sendTo(Object, Object) sendTo} and {@link #receiveFrom(Object) receiveFrom}
	 * @param channel The channel to be added to this multiplexer
	 */
	public void addChannel(S channelIdentifier, ISimplexChannel<T,E> channel);

	/**
	 * Sends a message to a specific channel identified by a channel identifier.
	 * @param channelIdentifier The identifier for the channel the message shall be sent to
	 * @param message The message to be sent
	 * @throws ComException
	 */
	public void sendTo(S channelIdentifier, T message) throws E;
	
	/**
	 * Receives a message from a specific channel identified by a channel identifier.
	 * @param channelIdentifier The identifier for the channel the message shall be received from
	 * @return The received message.
	 * @throws ComException
	 */
	public T receiveFrom(S channelIdentifier) throws E;
	
	/**
	 * Closes the channel identified by this channel identifier.
	 * @param channelIdentifier The identifier for the channel to be closed
	 * @throws E 
	 */
	public void removeChannel(S channelIdentifier) throws E;
	
	/**
	 * Returns a list of all channel identifiers for this multiplexer
	 * @return A list of all channel identifiers for this multiplexer
	 */
	public List<S> getChannelIdentifiers();
}
