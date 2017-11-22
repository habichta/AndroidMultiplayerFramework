package ch.ethz.inf.vs.controllerLib;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.ballgame.R;
import ch.ethz.inf.vs.ballgame.R.id;
import ch.ethz.inf.vs.ballgame.R.layout;
import ch.ethz.inf.vs.ballgame.util.SystemUiHider;
import ch.ethz.inf.vs.hostControllerLib.ICommunicator;
import ch.ethz.inf.vs.hostControllerLib.IController;
import ch.ethz.inf.vs.hostControllerLib.IControllerCallback;
import ch.ethz.inf.vs.hostControllerLib.IHost;
import ch.ethz.inf.vs.hostControllerLib.IHostCallback;
import ch.ethz.inf.vs.hostControllerLib.IIdentifierFactory;
import ch.ethz.inf.vs.hostControllerLib.IMultiplexChannel;
import ch.ethz.inf.vs.hostControllerLib.ISimplexChannel;
import ch.ethz.inf.vs.hostControllerLib.bluetooth.ComException;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


public abstract class AbstractLobbyActivity<S extends Serializable, T extends Serializable> extends Activity implements IHostCallback<S,ControllerMessage>{

	
	public static final int REQUEST_DISCOVERABLE = 624;
	public static final int REQUEST_RESET_DISCOVERABLE = 738;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		playerList = new ArrayList<S>();
		communicator = GlobalState.getState().communicator;
		isHost = GlobalState.getState().isHost;
	
		//set visible for discovery
		Intent discoverable = new Intent (BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivityForResult(discoverable,REQUEST_DISCOVERABLE);
		
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		registerReceiver(bReceiver, filter);
	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (GlobalState.getState().resumeLobby){
			host.setNewCallback(this);
			host.resumeReceiving();
			host.startAcceptingControllers(this, new IIdentifierFactory<S>(){
				@Override
				public S getNewIdentifier() {
					return createIdentifier();
				} });
			GlobalState.getState().resumeLobby = false;
		} else if (GlobalState.getState().goToStart){
			GlobalState.getState().goToStart = false;
			finish();
		}
	}
	
	private ICommunicator<S,ControllerMessage,BluetoothDevice> communicator;
	private IController<S,ControllerMessage> controller;
	private IHost<S,ControllerMessage> host;
	private boolean isHost;
	private Intent hostActivity;
	
	public List<S> playerList; 
	
	boolean visible = false;
	
	private final BroadcastReceiver bReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)){
				int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
				if (BluetoothAdapter.SCAN_MODE_CONNECTABLE == scanMode){
					visibilityChanged(false);
				} else if (BluetoothAdapter.SCAN_MODE_NONE == scanMode){
					Toast.makeText(AbstractLobbyActivity.this, "Cannot accept new connections", Toast.LENGTH_SHORT).show();
					visibilityChanged(false);
					if(host != null)
						host.close();
				}
			}
			
		}
		
	};
	
	/**
	 * gets called when the bluetooth visibility changes
	 * @param visible
	 */
	public abstract void onVisibilityChanged(boolean visible);
	
	private void visibilityChanged(boolean visible){
		if (this.visible == visible)
			return;
		
		this.visible = visible;
		onVisibilityChanged(visible);
	}
	
	/**
	 * Make a request to make the device discoverable
	 */
	public void setVisible(){
		//set visible for discovery
		Intent discoverable = new Intent (BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivityForResult(discoverable,AbstractLobbyActivity.REQUEST_RESET_DISCOVERABLE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_DISCOVERABLE){
			if (resultCode == RESULT_CANCELED){
				Toast.makeText(this, "Not visible for other Devices", Toast.LENGTH_SHORT).show();
				//start bluetooth
				BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
				if (btAdapter == null) {
					//Device does not support Bluetooth
					Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show();
					onBackPressed();
					return;
				}
				if (!btAdapter.isEnabled()){
					Toast.makeText(this, "Bluetooth must be turned on", Toast.LENGTH_SHORT).show();
					onBackPressed();
					return;
				}
			} else {
				visibilityChanged(true);
			}
			host = communicator.makeHost();
			host.startAcceptingControllers(this, new IIdentifierFactory<S>(){

				@Override
				public S getNewIdentifier() {
					return createIdentifier();
				} });
		} else if (requestCode == REQUEST_RESET_DISCOVERABLE) {
			if (resultCode == 300){
				visibilityChanged(true);
				host.startAcceptingControllers(this, new IIdentifierFactory<S>(){
					@Override
					public S getNewIdentifier() {
						return createIdentifier();
					} });
			}
		}else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	
	/** Host Callbacks  **/
	
	@Override
	public void onReceive(ControllerMessage message, S identifier) {
		if(message.messageType.equals(ControllerMessage.MessageType.C_REGISTRATION_MESSAGE)){
		    
			if (playerList.contains(identifier)){
				//player is already registered
				int index = playerList.indexOf(identifier);
				ControllerMessage registrationRetMessage = new ControllerMessage(ControllerMessage.MessageType.C_REGISTRATION_OK,playerList.get(index));
				try{
					host.sendTo(registrationRetMessage, identifier);
					Log.d("HOST_REGISTER_MESSAGE", "Reg Return Message sent");
				}catch (Exception e){
					//TODO: Correct Exception <?>
					Log.d("HOST_REGISTER_MESSAGE", "Reg Return Message failed");
					e.printStackTrace();	
				}
			} else {
			
			    //Initialize the identifier
				S initIdentifier = initializeIdentifier(identifier,message);
				if (initIdentifier == null){
					Toast.makeText(this, "failed to add player", Toast.LENGTH_SHORT).show();
					return;
				}
				ControllerMessage registrationRetMessage = new ControllerMessage(ControllerMessage.MessageType.C_REGISTRATION_OK,initIdentifier);
				playerList.add(initIdentifier);
				try{
				host.sendTo(registrationRetMessage, initIdentifier);
				Log.d("HOST_REGISTER_MESSAGE", "Reg Return Message sent");
				
				//Add to List after correct registration
				addIdentifierToList(initIdentifier);
				
				}catch (Exception e){
					//TODO: Correct Exception <?>
					Log.d("HOST_REGISTER_MESSAGE", "Reg Return Message failed");
					e.printStackTrace();
				}
			}
		
		
		}else if(message.messageType.equals(ControllerMessage.MessageType.C_DISCONNECTED)){
			playerList.remove(identifier);
			removeIdentifierFromList(identifier);
			host.removeController(identifier);
		} else if(message.messageType.equals(ControllerMessage.MessageType.START_GAME)){
			if(startRequest()){
				try{
					for (S ident : getPlayers()){
						host.sendTo(message, ident);
					}
				} catch (Exception e){
					e.printStackTrace();
					//Toast.makeText(this, "send failed", Toast.LENGTH_SHORT).show();
				}
				onHostStart();
			}

		} else if(message.messageType.equals(ControllerMessage.MessageType.GAME_MESSAGE)){
			try{
				onReceive(identifier, (T)message.message);
			} catch (Exception e){ }
			
		}
	}
	
	/**
	 * gets invoked if a request to start the Game arrived
	 * @return true if the game should start or false if it should not start
	 */
	public abstract boolean startRequest();
	
	/**
	 * set the activity for the actual game.
	 * This method should be invoked in onCreate()
	 * @param hostActivity
	 */
	public void setHostActivity(Class<? extends AbstractHostActivity> hostActivity){
		this.hostActivity = new Intent(this, hostActivity);
	}

	@Override
	public void onConnectionAccepted(S identifier) {
		// do nothing?
	}
	
	
	
	
	/**
	 * Creates an empty object of the desired (concrete) identifier.
	 * @return non-generic identifier type
	 */
	public abstract S createIdentifier();
	
	/**
	 * Initializes desired fields of the identifier object by parsing the serializable message
	 * within the ControllerMessage
	 * @param <S> identifier
	 * @param ControllerMessage message
	 * @return initialized identifier
	 */
	public abstract S initializeIdentifier(S identifier,ControllerMessage message);
	
	
	/**
	 * Add identifier to a list to conclude the registration process
	 * @param <S> identifier
	 */
	public abstract void addIdentifierToList(S identifier);
	
	
	public abstract void removeIdentifierFromList(S identifier);
	
	
	
	public void onHostStart(){
		//Start HostActivity on Server
		host.stopAcceptingControllers();
		host.pauseReceiving();
		GlobalState.getState().host = host;
		GlobalState.getState().players = playerList;
		if (hostActivity != null){
			startActivity(hostActivity);
		} else {
			Toast.makeText(this, "ERROR: hostActivity not set", Toast.LENGTH_LONG).show();
		}
	}
	
	
	public boolean isHost(){
		return isHost;
		
	}
	
	public List<S> getPlayers(){
		return playerList;
	}
	

	
	Handler callbackHandler = new Handler();
	public Handler getCallbackHandler(){
		return callbackHandler;
	}
	
	/**
	 * send a message m to player p
	 * @param p identifier of player
	 * @param m Message to send
	 */
	public void sendTo(S p, T m){
		try{
			ControllerMessage message = new ControllerMessage(ControllerMessage.MessageType.GAME_MESSAGE, m);
			host.sendTo(message, p);
		} catch (Exception e){
			e.printStackTrace();
			//Toast.makeText(this, "send failed", Toast.LENGTH_SHORT).show();
		}
	}
	
	public abstract void onReceive(S p, T m);
	
	@Override
	protected void onDestroy() {
		ControllerMessage message = new ControllerMessage(ControllerMessage.MessageType.C_DISCONNECTED,null);
		for (S ident : playerList){
			try{
				host.sendTo(message, ident);
			} catch (Exception e){
				//try again?
				try{
					Thread.sleep(100);
					host.sendTo(message, ident);
				} catch (Exception e2){}
			}
		}
		host.stopAcceptingControllers();
		host.close();
		try{
			unregisterReceiver(bReceiver);
		} catch (IllegalArgumentException e){}
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		onDestroy();
		super.onBackPressed();
	}

	// TODO Romy: Exceptions are not propagated anymore so this method isn't part of 
	// the Callback anymore. I removed the @Override. Please remove this method.
	public void onReceiveException(ComException exception, S identifier) {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "receive exception", Toast.LENGTH_SHORT).show();
		if(playerList.contains(identifier)){
			playerList.remove(identifier);
			removeIdentifierFromList(identifier);
			host.removeController(identifier);
		}
	}

	@Override
	public void onControllerDisconnect(S identifier) {
		if (playerList.contains(identifier)){
			playerList.remove(identifier);
			removeIdentifierFromList(identifier);
			host.removeController(identifier);
		}
	}

	@Override
	public void onControllerReconnect(S identifier) {
		// do nothing
		
	}

	@Override
	public void onInvalidMessageReceived(S identifier) {
		onControllerDisconnect(identifier);
		
	}
	
}
