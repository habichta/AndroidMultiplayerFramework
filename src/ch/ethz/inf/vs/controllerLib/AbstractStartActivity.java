package ch.ethz.inf.vs.controllerLib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.Player;
import ch.ethz.inf.vs.ballgame.R;
import ch.ethz.inf.vs.ballgame.R.id;
import ch.ethz.inf.vs.ballgame.R.layout;
import ch.ethz.inf.vs.ballgame.util.SystemUiHider;
import ch.ethz.inf.vs.hostControllerLib.ICommunicator;
import ch.ethz.inf.vs.hostControllerLib.ICommunicatorCallback;
import ch.ethz.inf.vs.hostControllerLib.IController;
import ch.ethz.inf.vs.hostControllerLib.IControllerCallback;
import ch.ethz.inf.vs.hostControllerLib.ISimplexChannel;
import ch.ethz.inf.vs.hostControllerLib.bluetooth.BluetoothCommunicator;
import ch.ethz.inf.vs.hostControllerLib.bluetooth.ComException;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 
 * @author lukas
 *
 * @param <S> A class representing a player
 * @param <T> 
 */
public abstract class AbstractStartActivity<S, T extends Serializable> extends Activity implements  ICommunicatorCallback<BluetoothDevice>, IControllerCallback<S,ControllerMessage>{
	
	
	//Intent to get to the LobbyActivity. Has to be set in the concrete class.
	Intent lobbyIntent;
	Intent startGameIntent;
	Intent settingIntent;
	
	ICommunicator<S, ControllerMessage, BluetoothDevice> communicator;
	IController<S, ControllerMessage> controller;
	public boolean isConnected;
	
	private final int BUFFERSIZE = 8000;
	
	private UUID applicationUuid;
	
	
	public abstract UUID getUuid();
	
	public List<BluetoothDevice> deviceList; 
	
	public boolean isSearching = false;
	
	private S playerObject;
	
	private ProgressDialog hostSearchdialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		deviceList = new ArrayList<BluetoothDevice>();
		applicationUuid = getUuid();
		
		
		communicator = new BluetoothCommunicator<S,ControllerMessage>(applicationUuid, getString(R.string.app_name), this,BUFFERSIZE);
		GlobalState.getState().communicator = communicator;
		
		isConnected = false;
		
		hostSearchdialog = new ProgressDialog(this);
		hostSearchdialog.setMessage(getString(R.string.hostSearchDialog));
		hostSearchdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
	}
		
	public void settings(){
		startActivity(settingIntent);
	}
	
	public void createHost(){
		if(isSearching){
			onDiscovryModeChanged(false);
		}
		GlobalState.getState().isHost = true;
		goToActivity();
	}
	public void searchHosts(){
		//List Hosts. On click on a host, connect to it and pass host as parameter
		if(!isSearching){
			hostSearchdialog.show();
			communicator.startHostDiscovery(this);
			isSearching = true;
		}
		
	}
	
	public void stopHostSearch(){
		if(isSearching){
			hostSearchdialog.dismiss();
			communicator.stopHostDiscovery();
			isSearching = false;
		}
	}
	
	/**
	 * gets called to when the host discovry started or when it stopped
	 * @param searching true if mode was changed to searching
	 */
	public abstract void onDiscovryModeChanged(boolean searching);
	
	public void onHostDiscovered(BluetoothDevice host){
		if(!deviceList.contains(host)){
			hostSearchdialog.dismiss();
			deviceList.add(host);
			onHostDiscovered(host.getName());
		}
	}
	
	/**
	 * This method gets invoked when a Host gets Discovered.
	 * Add the new host to the List of hosts
	 * @param host
	 */
	public abstract void onHostDiscovered(String host);
	
	
	/**
	 * onListViewClick
	 * @param host
	 */
	private void connectedToHost(BluetoothDevice host){
		if (isSearching){
			onDiscovryModeChanged(false);
		}
		if (isConnected){
			onConnectionEstablished();
			return;
		}
		if (controller == null){
			controller = communicator.makeControllerForForeignHost(host);
		}
		GlobalState.getState().controller = controller;
		try {
			controller.connectWithHost(this);
		} catch (Exception e){
			//Toast.makeText(this, "EXCEPTION", Toast.LENGTH_LONG).show();
		}
		
	}
	
	boolean stateConnecting = false;
	public void connectToHost(final int listPosition){
		
		if(!stateConnecting){
			stateConnecting = true;
			Toast.makeText(getApplicationContext(), "connecting", Toast.LENGTH_SHORT).show();
			connectedToHost(deviceList.get(listPosition));
			stateConnecting = false;
		} else {
			//Toast.makeText(getApplicationContext(), "already connecting", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	/**
	 * this method has to be called on onCreate()
	 * @param lobbyActivity the Class of the concrete LobbyActivity
	 */
	public void setLobbyActivity(Class<? extends AbstractLobbyActivity> lobbyActivity, Class<? extends AbstractStartGameActivity> startGameActivity){
		lobbyIntent = new Intent(getApplicationContext(), lobbyActivity);
		startGameIntent = new Intent (getApplicationContext(),startGameActivity);
	}
	
	public void setSettingActivity(Class<? extends AbstractSettingActivity> settingActivity){
		settingIntent = new Intent (getApplicationContext(), settingActivity);
	}
	
	private final void goToActivity(){
		if(GlobalState.getState().isHost){
		//What is shown on Hosts
			
		startActivity(lobbyIntent);
		}else{
		//What is shown on the Controllers
			controller.pauseReceiving();
			startActivity(startGameIntent);
			isConnected = false;
			
		}
	}
	
	
	
	Handler discoveryHandler= new Handler();
	public Handler getCallbackHandler(){
		return discoveryHandler;
	}
	
	@SuppressWarnings("unchecked")
	public void onReceive(ControllerMessage message){
		if (message.messageType.equals(ControllerMessage.MessageType.C_REGISTRATION_OK)){
			Log.d("CONTROLLER_REGISTER_MESSAGE", "Reg OK Message received");
			playerObject =  (S) message.message;
			GlobalState.getState().self = playerObject;
			//Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show();
			GlobalState.getState().isHost = false;
			goToActivity();
		}
	}
	
	public void onConnectionEstablished(){
		isConnected = true;
		SharedPreferences pref = getSharedPreferences(getApplicationInfo().name + "_preferences", 0);
		String name = pref.getString(AbstractSettingActivity.PREF_NAME, "player 1");
		ControllerMessage message = new ControllerMessage(ControllerMessage.MessageType.C_REGISTRATION_MESSAGE, name);
		Log.d("CONTROLLER_REGISTER_MESSAGE", "Reg  Message sent");
		//Toast.makeText(this, "reg Message sent", Toast.LENGTH_SHORT).show();
		try{
			controller.send(message);
		} catch (Exception e){
			//close connection
			controller.close();
			isConnected = false;
			//Toast.makeText(this, "send failed", Toast.LENGTH_LONG).show();
		}
	}




	@Override
	public void onDisconnect() {
		Toast.makeText(this, "connecting failed", Toast.LENGTH_SHORT).show();
		isConnected = false;
		
	}



	@Override
	public void onInvalidMessageReceived() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}