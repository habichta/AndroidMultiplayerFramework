package ch.ethz.inf.vs.hostControllerLib.bluetooth;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import ch.ethz.inf.vs.hostControllerLib.IIdentifierFactory;
import ch.ethz.inf.vs.hostControllerLib.ISimplexChannel;
import ch.ethz.inf.vs.hostControllerLib.IHost;
import ch.ethz.inf.vs.hostControllerLib.IHostCallback;

public class BluetoothHost<S, T extends Serializable> implements IHost<S,T> {


	private final BluetoothMultiplexChannel<S,T> hostChannel;
	private final BluetoothAdapter adapter;
	private final Object mutex;	
	private final ReentrantReadWriteLock readWriteLock;
	private final int receiveBufferSize;
	private final String applicationName;
	private final UUID applicationUuid;
	private final Map<S,LooperThread> controllerToReceiverThread;
	private final Map<S,BluetoothDevice> controllerToDevice;
	private final Map<BluetoothDevice,S> inactiveControllers;
	
	private IHostCallback<S,T> hostCallback;
	private Handler callbackHandler;
	private AcceptState acceptState;
	private HostState hostState;
	private HostState hostStateBeforePause;
	private BluetoothServerSocket serverSocket;
	private LooperThread controllerAcceptThread;
	

	private enum AcceptState{
		ACCEPTING, NOT_ACCEPTING
	};
	
	private enum HostState{
		ACTIVE, PAUSED, INACTIVE;
	}
	
	/**
	 * Creates a new bluetooth host for a given application
	 * @param callback The callback used to inform about certain events
	 * @param adapter The bluetooth adapter at which this host will get a service record
	 * @param applicationUuid The UUID of the application this host serves for 
	 * @param applicationName The name of the application this host serves for
	 */
	public BluetoothHost(BluetoothAdapter adapter, int receiveBufferSize, UUID applicationUuid, String applicationName){
		
		this.mutex = new Object();
		this.adapter = adapter;
		this.receiveBufferSize = receiveBufferSize;
		this.applicationUuid = applicationUuid;
		this.applicationName = applicationName;
		this.readWriteLock = new ReentrantReadWriteLock(true);
		this.hostState = HostState.INACTIVE;
		this.hostChannel = new BluetoothMultiplexChannel<S,T>();
		this.controllerToDevice = Collections.synchronizedMap(new HashMap<S,BluetoothDevice>());
		this.controllerToReceiverThread = Collections.synchronizedMap(new HashMap<S,LooperThread>());
		this.inactiveControllers = Collections.synchronizedMap(new HashMap<BluetoothDevice,S>());
	}
	
	@Override
	public boolean sendTo(final T message, final S player){

		try {
			hostChannel.sendTo(player, message);
		} catch (ComException e) {
			if (e.type.equals(ComException.ComExceptionType.SEND_FAILURE)){
				try {
					hostChannel.removeChannel(player);
				} catch (ComException e1) {
					throw new RuntimeException();
				}
			}
			else if(e.type.equals(ComException.ComExceptionType.INVALID_IDENTIFIER)){
				// do nothing
			}
			else{
				throw new RuntimeException();
			}
			return false;
		}
		
		return true;
	}

	@Override
	public boolean startAcceptingControllers(IHostCallback<S,T> callback, IIdentifierFactory<S> identifierFactory) {
		
		readWriteLock.writeLock().lock();
		
		boolean success = true;
		
		if (hostState.equals(HostState.INACTIVE)){
			this.hostCallback = callback;
			this.callbackHandler = callback.getCallbackHandler();
	
			controllerAcceptThread = new LooperThread();
			controllerAcceptThread.start();
			while(controllerAcceptThread.handler==null)
				Thread.yield();
			Handler threadHandler = controllerAcceptThread.handler;
			
			try {
				// Create a server socket with an application specific Service Record
				serverSocket = adapter.listenUsingRfcommWithServiceRecord(applicationName, applicationUuid);
			} catch (IOException e1) {
				success = false;
				threadHandler.getLooper().quit();
			}
			
			if (success){
				this.acceptState = AcceptState.ACCEPTING;
				this.hostState = HostState.ACTIVE;
				// Run the code for channel creation on a new thread
				threadHandler.post(codeForChannelCreation(identifierFactory));
			}
		}
		else
			success = false;
		
		readWriteLock.writeLock().unlock();
		return success;
	}

	@Override
	public void stopAcceptingControllers() {	
		readWriteLock.writeLock().lock();
			
		this.acceptState=AcceptState.NOT_ACCEPTING;
		
		readWriteLock.writeLock().unlock();
	}

	@Override
	public List<S> getControllerIdentifiers() {
		return new ArrayList<S>(controllerToReceiverThread.keySet());
	}
	
	/**
	 * Code to get connected sockets out of the {@link #serverSocket server socket}, create channels
	 * out of them and run {@link #codeToWaitForRegistrationAttempt(ISimplexChannel) code to wait for registration attempts}
	 * on every created channel
	 * @return A Runnable containing the code for channel creation
	 */
	private Runnable codeForChannelCreation(final IIdentifierFactory<S> identifierFactory){

		Runnable code = new Runnable(){
			
			@Override
			public void run() {
				
				Handler thisHandler = new Handler();
				
				boolean exception = false;
				BluetoothSocket socket=null;
				try {
					socket = serverSocket.accept();
				} catch (IOException e1) {
					exception = true;	
				}
				
				waitOnPauseRead();
					
				if (!hostState.equals(HostState.INACTIVE)){
					
					if (exception){
						// Bluetooth connection lost. Enqueue again as long as Host was not closed
						thisHandler.post(this);
						readWriteLock.readLock().unlock();
						return;
					}
					
					// The device that connected to this host
					BluetoothDevice device = socket.getRemoteDevice();
					
					boolean knownPlayer = inactiveControllers.containsKey(device);
					
					if (knownPlayer || acceptState.equals(AcceptState.ACCEPTING)){
					
						final BluetoothSimplexChannel<T> channel = new BluetoothSimplexChannel<T>(socket, receiveBufferSize);
						
						final S identifier;
						
						if (knownPlayer){
							// Get this device's old identifier
							identifier = inactiveControllers.get(device);
							// Set controller active again
							inactiveControllers.remove(identifier);
						}
						else{
							// Get a new identifier
							identifier = identifierFactory.getNewIdentifier();
							controllerToDevice.put(identifier, device);
						}
						
						LooperThread receiverThread = new LooperThread();
						receiverThread.start();
						while (receiverThread.handler==null)
							Thread.yield();
						Handler threadHandler = receiverThread.handler;
						
						controllerToReceiverThread.put(identifier, receiverThread);
					
						hostChannel.addChannel(identifier, channel);
						
						if (knownPlayer){
							callbackHandler.post(new Runnable(){
								@Override
								public void run() {
									hostCallback.onControllerReconnect(identifier);	
								}
							});
						}
						else{
							callbackHandler.post(new Runnable(){
		
								@Override
								public void run() {
									hostCallback.onConnectionAccepted(identifier);
								}	
							});	
						}
						
						// Start to receive from this player
						threadHandler.post(codeForChannelReceive(identifier));
						
					}
					else{
						// Kick the invalid connection out
						try {
							socket.close();
						} catch (IOException e) {
							throw new RuntimeException();
						}
					}
					
					// Keep accepting incoming connections
					thisHandler.post(this);
					
				}
				else
					thisHandler.getLooper().quit();
				
				readWriteLock.readLock().unlock();
				}
		};
		
		return code;
	}
	
	
	/**
	 * Code to receive messages from a channel
	 * @param channelIdentifier The identifier of the channel to receive from
	 * @return A runnable containing code to receive messages from a channel
	 */
	private Runnable codeForChannelReceive(final S channelIdentifier){
		
		Runnable code = new Runnable(){

			@Override
			public void run() {
				
				final Handler thisHandler = new Handler();
				ComException exception = null;
				T message=null;
				
				try {
					message = hostChannel.receiveFrom(channelIdentifier);
				} catch (final ComException e) {
					exception = e;
				}
				
				waitOnPauseRead();
				
				final T finalMessage = message;
				
				if (!hostState.equals(HostState.INACTIVE)){
					
					if (exception!=null){
						if (exception.type.equals(ComException.ComExceptionType.RECEIVE_FAILURE) || 
								exception.type.equals(ComException.ComExceptionType.INVALID_IDENTIFIER)){
							// Instead of continuing to receive, set the controller inactive (lets
							// this receiving thread stop)
							thisHandler.post(new Runnable(){
								@Override
								public void run() {
									setControllerInactive(channelIdentifier);
									thisHandler.getLooper().quit();
								}
							});
							readWriteLock.readLock().unlock();
							return;
						}
						else if (exception.type.equals(ComException.ComExceptionType.INVALID_MESSAGE)){
							
							callbackHandler.post(new Runnable(){
								@Override
								public void run() {
									hostCallback.onInvalidMessageReceived(channelIdentifier);
								}
							});
						}
						else{ // INTERNAL_FAILURE
							throw new RuntimeException();
						}
					}
					
					else{
						callbackHandler.post(new Runnable(){
							@Override
							public void run() {
								hostCallback.onReceive(finalMessage, channelIdentifier);
							}	
						});	
					}
					
					// Continue receiving
					thisHandler.post(this);
				}
				else
					thisHandler.getLooper().quit();
				
				// release the read lock
				readWriteLock.readLock().unlock();
			}

		};
		
		return code;
	}
	
	private void setControllerInactive(final S identifier) {
		waitOnPauseWrite();
		
		try {
			hostChannel.removeChannel(identifier);
		} catch (ComException e) {
			if (e.type.equals(ComException.ComExceptionType.INVALID_IDENTIFIER)){
				// do nothing
			}
			else{
				throw new RuntimeException();
			}
		}
		
		BluetoothDevice device = controllerToDevice.get(identifier);
		
		inactiveControllers.put(device,identifier);
		
		callbackHandler.post(new Runnable(){
			@Override
			public void run() {
				hostCallback.onControllerDisconnect(identifier);
			}
		});
		
		readWriteLock.writeLock().unlock();
	}

	@Override
	public void setNewCallback(IHostCallback<S,T> callback) {
		// write callback and callbackHandler
		readWriteLock.writeLock().lock();
		
		this.hostCallback = callback;
		this.callbackHandler = callback.getCallbackHandler();
		
		readWriteLock.writeLock().unlock();
	}

	@Override
	public void pauseReceiving() {
		// write receiveState
		readWriteLock.writeLock().lock();
		
		if (!hostState.equals(HostState.PAUSED)){
			this.hostStateBeforePause = this.hostState;
			this.hostState=HostState.PAUSED;
		}

		readWriteLock.writeLock().unlock();
	}

	@Override
	public void resumeReceiving() {
		// write host state
		readWriteLock.writeLock().lock();
		synchronized(mutex){
			if (hostState.equals(HostState.PAUSED)){
				this.hostState = this.hostStateBeforePause;
				// tell all threads which exited a blocking call in the meantime (and stopped
				// execution therefore) to continue 
				mutex.notifyAll();
			}
		}
		readWriteLock.writeLock().unlock();
	}

	@Override
	public void close() {

		readWriteLock.writeLock().lock();
		synchronized(mutex){
			hostState = HostState.INACTIVE;
			// wake up all receiver threads
			mutex.notifyAll();
		}
		readWriteLock.writeLock().unlock();
		
		try {
			serverSocket.close();
		} catch (IOException e1) {
			throw new RuntimeException();
		}
		
		try {
			hostChannel.close();
		} catch (ComException e) {
			throw new RuntimeException();
		}
		
		Collection<LooperThread> receiverThreads = controllerToReceiverThread.values();
		for(LooperThread thread : receiverThreads){
			while(thread.isAlive())
				try {
					thread.join();
				} catch (InterruptedException e) {
					// do nothing
				}
		}
		
		while(controllerAcceptThread.isAlive()){
			try {
				controllerAcceptThread.join();
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		
	}

	@Override
	public void removeController(S identifier) {
		Thread receiverThread;
		
		waitOnPauseWrite();
		
		try {
			hostChannel.removeChannel(identifier);
		} catch (ComException e) {
			if (!e.type.equals(ComException.ComExceptionType.INVALID_IDENTIFIER)){
				throw new RuntimeException();
			}
		}
		
		receiverThread = controllerToReceiverThread.get(identifier);
		controllerToReceiverThread.remove(identifier);
		controllerToDevice.remove(identifier);
		inactiveControllers.remove(identifier);
		
		readWriteLock.writeLock().unlock();
		
		while(receiverThread.isAlive()){
			try {
				receiverThread.join();
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		
		
		
	}

	private void waitOnPauseRead(){
		boolean resume = false;
		do{	
			readWriteLock.readLock().lock();

			if (hostState.equals(HostState.PAUSED)){
				// get Lock on mutex to "register" by wait() for 
				// notification if host state is changed
				synchronized(mutex){
					// release the read lock
					readWriteLock.readLock().unlock();
					try {
						// wait for the notification that the host isn't in paused state anymore
						mutex.wait();
					} catch (InterruptedException e) {
						// do nothing, start waiting again if host is still paused
					}
				}
			}
			else 
				resume = true;	
			
		}while(!resume);
	}
	
	private void waitOnPauseWrite(){
		boolean resume = false;
		do{	
			readWriteLock.writeLock().lock();

			if (hostState.equals(HostState.PAUSED)){
				// get Lock on mutex to "register" by wait() for 
				// notification if host state is changed
				synchronized(mutex){
					// release the read lock
					readWriteLock.readLock().unlock();
					try {
						// wait for the notification that the host isn't in paused state anymore
						mutex.wait();
					} catch (InterruptedException e) {
						// do nothing, start waiting again if host is still paused
					}
				}
			}
			else 
				resume = true;	
			
		}while(!resume);
	}
}
