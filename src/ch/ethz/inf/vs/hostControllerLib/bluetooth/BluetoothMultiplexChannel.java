package ch.ethz.inf.vs.hostControllerLib.bluetooth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.inf.vs.hostControllerLib.ISimplexChannel;
import ch.ethz.inf.vs.hostControllerLib.IMultiplexChannel;

/**
 * This class describes a multiplexing bluetooth communication channel.
 * @author Romy Profanter
 *
 */
public class BluetoothMultiplexChannel<S, T extends Serializable> implements IMultiplexChannel<S,T,ComException>{

	/**
	 * Maps identifiers to channels.
	 */
	private final Map<S,ISimplexChannel<T,ComException>> identifiersToChannels;
	
	/**
	 * Creates a new multiplexing bluetooth communication channel
	 */
	public BluetoothMultiplexChannel() {
		// initialize the mapping of identifiers to channels
		identifiersToChannels = Collections.synchronizedMap(new HashMap<S,ISimplexChannel<T,ComException>>());
	}

	/**
	 * Broadcasts a message to all channels
	 */
	@Override
	public void broadcast(T message) throws ComException {
		Collection<ISimplexChannel<T,ComException>> channelList = identifiersToChannels.values();
		for (ISimplexChannel<T,ComException> channel: channelList){
			channel.send(message);
		}
	}

	
	@Override
	public void close() throws ComException{
		
		Collection<ISimplexChannel<T,ComException>> channelList = identifiersToChannels.values();
		for (ISimplexChannel<T,ComException> channel : channelList)
			channel.close();
		
	}
	
	@Override
	public void addChannel(S channelIdentifier, ISimplexChannel<T,ComException> channel) {

		identifiersToChannels.put(channelIdentifier, channel);
		
	}
	
	@Override
	public void sendTo(S channelIdentifier, T message) throws ComException {
		
		ISimplexChannel<T,ComException> clientChannel = identifiersToChannels.get(channelIdentifier);
		if (clientChannel==null)
			throw new ComException(ComException.ComExceptionType.INVALID_IDENTIFIER);
		clientChannel.send(message);
		
	}
	
	@Override
	public T receiveFrom(S channelIdentifier) throws ComException {
		
		ISimplexChannel<T,ComException> channel = identifiersToChannels.get(channelIdentifier);
		if (channel==null)
			throw new ComException(ComException.ComExceptionType.INVALID_IDENTIFIER);
		return channel.receive();
	}
	
	@Override
	public void removeChannel(S channelIdentifier) throws ComException {
		
		ISimplexChannel<T,ComException> channel = identifiersToChannels.get(channelIdentifier);
		if (channel==null)
			throw new ComException(ComException.ComExceptionType.INVALID_IDENTIFIER);
		
		channel.close();
		identifiersToChannels.remove(channelIdentifier);
		
	}
	
	@Override
	public List<S> getChannelIdentifiers() {
		
		Set<S> identifiers = identifiersToChannels.keySet();
		ArrayList<S> identifierList = new ArrayList<S>(identifiers);
		return identifierList;
	}
}
