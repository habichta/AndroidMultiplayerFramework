package ch.ethz.inf.vs.hostControllerLib;


/**
 * This interface represents the starting point for a host-controller based application.
 * It provides all the communication functionality needed for such an application.
 * 
 * @author Romy Profanter
 *
 * @param <S> The type of objects used to represent the controllers in the host object
 * @param <T> The type of messages this communicator is supposed handle. 
 * @param <V> The type of objects that will represent discovered hosts initially
 */
public interface ICommunicator<S, T, V>{
	
	/**
	 * Starts asynchronous host discovery. Once a host is discovered, the {@literal callback}'s
	 * {@link ICommunicatorCallback#onHostDiscovered(Object) onHostDiscovered} method
	 * will be called.
	 * @param callback The callback which's 
	 * {@link ICommunicatorCallback#onHostDiscovered(Object) onHostDiscovered} method
	 * will be called once a host is discovered.
	 */
	public void startHostDiscovery(ICommunicatorCallback<V> callback);
	
	/**
	 * Stops host discovery.
	 */
	public void stopHostDiscovery();

	/**
	 * Makes a controller object for the specified host.
	 * @param host The host this controller shall eventually connect to
	 * @return A controller object for the specified host
	 */
	public IController<S,T> makeControllerForForeignHost(V host);

	/**
	 * Makes a host object.
	 * @return A new host object.
	 */
	public IHost<S,T> makeHost();

}

