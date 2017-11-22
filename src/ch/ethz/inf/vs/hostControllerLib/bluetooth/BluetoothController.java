package ch.ethz.inf.vs.hostControllerLib.bluetooth;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import ch.ethz.inf.vs.hostControllerLib.ISimplexChannel;
import ch.ethz.inf.vs.hostControllerLib.IController;
import ch.ethz.inf.vs.hostControllerLib.IControllerCallback;

public class BluetoothController<S, T extends Serializable> implements IController<S,T>{

	private ISimplexChannel<T,ComException> channel;
	private IControllerCallback<S,T> controllerCallback;
	private Handler callbackHandler;
	private BluetoothDevice host;
	private UUID applicationUuid;
	private LooperThread receiveThread;
	private int receiveBufferSize;
	
	private Object mutex;
	private ReadWriteLock readWriteLock;
	private ControllerState controllerState;
	private ControllerState controllerStateBeforePause;
	
	/**
	 * Creates a new bluetooth controller for a given application
	 * @param host	The host this bluetooth controller is used to connect to
	 * @param receiveBufferSize The size of the controller's receive buffer 
	 * @param applicationUuid The UUID of the application this bluetooth controller
	 * belongs to. Used to identify the same application running on the host
	 */
	public BluetoothController(BluetoothDevice host, int receiveBufferSize, UUID applicationUuid) {
		this.host = host;
		this.receiveBufferSize = receiveBufferSize;
		this.applicationUuid = applicationUuid;
		this.mutex = new Object();
		this.readWriteLock = new ReentrantReadWriteLock(true);
		this.controllerState = ControllerState.DISCONNECTED;
	}
	
	private enum ControllerState{
		DISCONNECTED,CONNECTING,CONNECTED,PAUSED;
	}
	
	@Override
	public void connectWithHost(IControllerCallback<S,T> callback) {
		
		readWriteLock.writeLock().lock();
		
		if (controllerState.equals(ControllerState.DISCONNECTED))
		{
			controllerState = ControllerState.CONNECTING;

			this.controllerCallback=callback;
			this.callbackHandler = controllerCallback.getCallbackHandler();
			
			receiveThread = new LooperThread();
			receiveThread.start();
			while (receiveThread.handler==null)
				Thread.yield();
			receiveThread.handler.post(codeForConnect());
		}
		
		readWriteLock.writeLock().unlock();
	}

	@Override
	public boolean send(T message){
		
		try {
			channel.send(message);
		} catch (ComException e) {
			
			if (e.type.equals(ComException.ComExceptionType.SEND_FAILURE))
				try {
					channel.close();
				} catch (ComException e1) {
					throw new RuntimeException();
				}
			else{
				throw new RuntimeException();
			}
			return false;
		}
		
		return true;
	}
	
	private Runnable codeForConnect(){
		
		Runnable code = new Runnable(){
			
			@Override
			public void run() {
				
				Handler thisHandler = new Handler();
				
				boolean exception = false;
				
				// Try to connect to the device by searching there for the application's UUID in the SDP records
				BluetoothSocket socket=null;
				try {
					socket = host.createRfcommSocketToServiceRecord(applicationUuid);
					socket.connect();
				}catch (IOException e){
					exception = true;
				}
				
				boolean resume = false;
				do{
					readWriteLock.writeLock().lock();
					synchronized(mutex){
						if (controllerState.equals(ControllerState.PAUSED)){
							readWriteLock.writeLock().unlock();
							try {
								mutex.wait();
							} catch (InterruptedException e) {
								// do nothing
							}
						}
						else
							resume=true;
					}
				}while(!resume);
				
				if (exception){
					controllerState = ControllerState.DISCONNECTED;
					thisHandler.getLooper().quit();
				}
				else{
					channel = new BluetoothSimplexChannel<T>(socket, receiveBufferSize);
					
					controllerState = ControllerState.CONNECTED;
					
					callbackHandler.post(new Runnable(){
						@Override
						public void run() {
							controllerCallback.onConnectionEstablished();	
						}	
					});
					
					thisHandler.post(codeForReceive());
					
				}
				readWriteLock.writeLock().unlock();
			}
			};
		return code;
	}
	
	
	
	private Runnable codeForReceive(){
		
		Runnable code = new Runnable(){
	
			@Override
			public void run() {
				
				final Handler thisHandler = new Handler();
				ComException exception = null;
				T message=null;
				
				try {
					message = channel.receive();
				} catch (ComException e) {
					exception = e;
				}
				
				waitOnPause();
					
				final T finalMessage = message;
				
				if (!controllerState.equals(ControllerState.DISCONNECTED)){
					
					if(exception!=null){
						
						if (exception.type.equals(ComException.ComExceptionType.RECEIVE_FAILURE)){
							thisHandler.post(new Runnable(){
								@Override
								public void run() {
									disconnect(true);
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
									controllerCallback.onInvalidMessageReceived();
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
								controllerCallback.onReceive(finalMessage);
							}	
						});	
					}
					
					// Enqueue this Runnable again
					thisHandler.post(this);	
				}
				else
					thisHandler.getLooper().quit();
				
				// release the write lock
				readWriteLock.readLock().unlock();
			}
		};
		
		return code;
	}

	@Override
	public void setNewCallback(IControllerCallback<S,T> callback) {
		// write controllerCallback and callbackHandler
		readWriteLock.writeLock().lock();
		synchronized(mutex){
			this.controllerCallback = callback;
			this.callbackHandler = callback.getCallbackHandler();
		}
		readWriteLock.writeLock().unlock();
	}

	@Override
	public void pauseReceiving() {
		
		readWriteLock.writeLock().lock();
		synchronized(mutex){
			
			if(!controllerState.equals(ControllerState.PAUSED)){
				this.controllerStateBeforePause = this.controllerState;
				this.controllerState=ControllerState.PAUSED;
			}
		}
		readWriteLock.writeLock().unlock();
	}

	@Override
	public void resumeReceiving() {
		// write receiveState
		readWriteLock.writeLock().lock();
		synchronized(mutex){
			if(controllerState.equals(ControllerState.PAUSED)){
				this.controllerState=controllerStateBeforePause;
				mutex.notifyAll();
			}
		}
		readWriteLock.writeLock().unlock();
	}
	
	private void disconnect(boolean notifyCallback){
		readWriteLock.writeLock().lock();
		synchronized(mutex){
			if(!controllerState.equals(ControllerState.DISCONNECTED)){
				
				this.controllerState = ControllerState.DISCONNECTED;
				mutex.notifyAll();

				if (notifyCallback){
					callbackHandler.post(new Runnable(){
						@Override
						public void run() {
							controllerCallback.onDisconnect();
						}	
					});
				}
			}
		}
		readWriteLock.writeLock().unlock();
		
		try {
			channel.close();
		} catch (ComException e) {
			throw new RuntimeException();
		}
		
	}

	@Override
	public void close() {
		
		disconnect(false);
		
		// wait for receiving thread to be finished
		while(receiveThread.isAlive())
		try {
			receiveThread.join();
		} catch (InterruptedException e) {
			// do nothing
		}
	}
	
	private void waitOnPause(){
		boolean resume = false;
		
		do{
			readWriteLock.readLock().lock();

			if (controllerState.equals(ControllerState.PAUSED)){
				synchronized(mutex){
					// release the write lock
					readWriteLock.readLock().unlock();
					try {
						// wait for the notification that the controller is in receiving state again
						mutex.wait();
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
			else 
				resume = true;	
				
		}while(!resume);
	}

}
