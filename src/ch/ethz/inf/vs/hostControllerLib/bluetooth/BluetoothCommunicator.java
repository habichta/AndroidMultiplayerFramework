package ch.ethz.inf.vs.hostControllerLib.bluetooth;

import java.io.Serializable;
import java.util.UUID;

import ch.ethz.inf.vs.hostControllerLib.ICommunicator;
import ch.ethz.inf.vs.hostControllerLib.ICommunicatorCallback;
import ch.ethz.inf.vs.hostControllerLib.IController;
import ch.ethz.inf.vs.hostControllerLib.IHost;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Parcelable;

/**
 * Instances of this class serve as starting points for host-controller applications via Bluetooth.
 * @author Romy Profanter
 *
 */
public class BluetoothCommunicator<S, T extends Serializable> implements ICommunicator<S,T, BluetoothDevice>{


	private final BluetoothAdapter adapter;
	
	private final UUID applicationUuid;
	
	private final String applicationName;
	
	private final Context context;
	
	private final int receiveBufferSize;
	
	private BroadcastReceiver broadcastReceiver;
	
	private LooperThread broadcastReceiverThread;
	
	private Handler broadcastReceiverThreadHandler;
	
	private boolean discovering;
	
	private final Object mutex;
	

	/**
	 * Creates a new BluetoothCommunicator for a given application.
	 * @param applicationUuid The UUID that uniquely specifies the application 
	 * this communicator shall handle. 
	 * @param applicationName The name of the application this communicator
	 * shall handle.
	 * @param context The context in which this communicator is used
	 * @param receiveBufferSize The size of the returned host's and controllers'
	 * receive buffers
	 */
	public BluetoothCommunicator(UUID applicationUuid, String applicationName, Context context, int receiveBufferSize){
		this.adapter = BluetoothAdapter.getDefaultAdapter();
		this.applicationUuid = applicationUuid;
		this.applicationName = applicationName;
		this.context = context;
		this.receiveBufferSize = receiveBufferSize;
		this.discovering = false;
		this.mutex = new Object();
	}	

	@Override
	public final void startHostDiscovery(final ICommunicatorCallback<BluetoothDevice> callback) {
		
		broadcastReceiver = makeBroadcastReceiver(callback);
		
		broadcastReceiverThread = new LooperThread();
		broadcastReceiverThread.start();
		while(broadcastReceiverThread.handler==null)
			Thread.yield();
		
		broadcastReceiverThreadHandler = broadcastReceiverThread.handler;
		
		this.discovering = true;
		
		// Induces a barrier for broadcast callbacks posted before discovery finished
		broadcastReceiverThreadHandler.post(new Runnable(){
			@Override
			public void run() {
				synchronized(mutex){
					while (discovering)
						try {
							mutex.wait();
						} catch (InterruptedException e) {
							// do nothing
						}
				}
			}
		});
		
		// Register the BroadcastReceiver for ACTION_FOUND, ACTION_UUID and ACTION_DISCOVERY_FINISHED
		IntentFilter deviceFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(broadcastReceiver, deviceFilter);
		IntentFilter uuidFilter = new IntentFilter(BluetoothDevice.ACTION_UUID);
		context.registerReceiver(broadcastReceiver, uuidFilter); 
		IntentFilter discoveryFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		context.registerReceiver(broadcastReceiver, discoveryFilter);
				
		adapter.startDiscovery();
	}
	
	@Override
	public final void stopHostDiscovery(){
		adapter.cancelDiscovery();
		context.unregisterReceiver(broadcastReceiver);
		synchronized(mutex){
			discovering = false;
			mutex.notifyAll();
		}
		broadcastReceiverThreadHandler.getLooper().quit();
		while(broadcastReceiverThread.isAlive())
		try {
			broadcastReceiverThread.join();
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	@Override
	public final IHost<S,T> makeHost() {
		
		BluetoothHost<S,T> host = new BluetoothHost<S,T>(adapter, receiveBufferSize, applicationUuid, applicationName);
		return host;
	}

	@Override
	public final IController<S,T> makeControllerForForeignHost(BluetoothDevice host) {
		
		BluetoothController<S,T> controller = new BluetoothController<S,T>(host, receiveBufferSize, applicationUuid);
		return controller;
	}
	

	/**
	 * Creates a BroadcastReceiver for ACTION_FOUND and ACTION_UUID
	 * @param callback The callback to be called if a device with a 
	 * matching application UUID is found
	 * @return A BroadcastReceiver for ACTION_FOUND and ACTION_UUID
	 */
	private final BroadcastReceiver makeBroadcastReceiver(final ICommunicatorCallback<BluetoothDevice> callback){
	
		final BroadcastReceiver receiver = new BroadcastReceiver() {
			
		    public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        
		        // When discovery finds a device
		        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		            // Get the BluetoothDevice object from the Intent
		            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		 
		            broadcastReceiverThreadHandler.post(new Runnable(){
						@Override
						public void run() {
							device.fetchUuidsWithSdp();
						}
		            });
		            
		        }
		     
		        if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
		        	synchronized(mutex){
		        	discovering = false;
		        	mutex.notifyAll();
		        	}
		        }
		        
		        // When a specific device's service list was found
		        if (action.equals(BluetoothDevice.ACTION_UUID)){
		        	final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		        	// Get the UUID list
		        	Parcelable[] uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
		        	if (uuids!=null){
		        		int length = uuids.length;
		        		for(int i=0; i<length; i++){
		        			ParcelUuid parcelableUuid = (ParcelUuid)(uuids[i]);
		        			UUID uuid = null;
		        			if (parcelableUuid!=null)
		        				// Get a specific UUID
		        				uuid = parcelableUuid.getUuid();
		        			if (uuid!=null && applicationUuid.equals(uuid)){
		        				// Call back if the Service Record UUID is the UUID of this application
		        				Handler callbackHandler = callback.getCallbackHandler();
				        		callbackHandler.post(new Runnable(){
		
									@Override
									public void run() {
										callback.onHostDiscovered(device);
									}	
				        		});	
		        			}
		        		}
		        	}
		        }
		    }
		};
		
		return receiver;
	}

}

