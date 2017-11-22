package ch.ethz.inf.vs.controllerLib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.Player;
import ch.ethz.inf.vs.hostControllerLib.IHost;
import ch.ethz.inf.vs.hostControllerLib.IHostCallback;
import ch.ethz.inf.vs.hostControllerLib.IIdentifierFactory;
import ch.ethz.inf.vs.hostControllerLib.bluetooth.ComException;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public abstract class AbstractHostActivity<S extends Serializable, T extends Serializable> extends Activity implements IHostCallback<S,ControllerMessage>{

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		host = GlobalState.getState().host;
		host.setNewCallback(this);
		host.resumeReceiving();

		playerList = (List<S>) GlobalState.getState().players;
		
		reconnections = new ArrayList<S>();
	}
	
	private List<S> playerList;
	
	private List<S> reconnections;
	
	public IHost<S,ControllerMessage> host;
	
	/**
	 * 
	 * @return the list of all players
	 */
	public List<S> getPlayerList(){
		return playerList;
	}
	
	
	//TODO: This function can stay emtpy. It should be called if a Player p disconnects. It's implemented in the HostActivity
	public abstract void onDisconnect(S p);
	
	
	/**
	 * This method gets called when a message is received.
	 * @param b message to send
	 * @param p identifier of the sender
	 */
	public abstract void onReceive(T b, S p);
	
	//TODO: This function can stay emtpy. It should be called if a Player p reconnects. It's implemented in the HostActivity
	public abstract void onReconnect(S p);
	
	/**
	 * send a message b to a player p
	 * @param b message to send
	 * @param p the identifier for the player the message should be sent to
	 */
	public void send(T b, S p){
		try{
			ControllerMessage message =  new ControllerMessage(ControllerMessage.MessageType.GAME_MESSAGE, b);
			host.sendTo(message, p);
		} catch (Exception e){
			e.printStackTrace();
			//Toast.makeText(this, "send failed", Toast.LENGTH_SHORT).show();
		}
	}
	
	

	Handler callbackHandler = new Handler();
	@Override
	public Handler getCallbackHandler() {
		return callbackHandler;
	}

	@Override
	public void onReceive(ControllerMessage message, S identifier) {
		switch (message.messageType){
		case C_BACK_TO_LOBBY:
			break;
		case C_DISCONNECTED:
			break;
		case C_POLL:
			break;
		case C_REGISTRATION_MESSAGE:
			if (reconnections.contains(identifier)){
				reconnections.remove(identifier);
				ControllerMessage returnMessage = new ControllerMessage(ControllerMessage.MessageType.C_REGISTRATION_OK, identifier);
				host.sendTo(returnMessage, identifier);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				returnMessage = new ControllerMessage(ControllerMessage.MessageType.START_GAME,null);
				host.sendTo(returnMessage, identifier);
				onReconnect(identifier);
			}
			break;
		case C_REGISTRATION_OK:
			break;
		case GAME_MESSAGE:
			try{
				onReceive((T)message.message, identifier);
			} catch (Exception e){ }
			break;
		case START_GAME:
			break;
		default:
			break;
		
		}
		
	}

	@Override
	public void onConnectionAccepted(S identifier) {
		//TODO: don't accept new connections?
		
	}
	
	public void goBackToLobby(){
		for (S ident : playerList){
			ControllerMessage message = new ControllerMessage(ControllerMessage.MessageType.C_BACK_TO_LOBBY,null);
			try{
				host.sendTo(message, ident);
			} catch (Exception e){}
		}
		host.pauseReceiving();
		GlobalState.getState().resumeLobby = true;
		super.onBackPressed();
	}


	@Override
	public void onControllerDisconnect(S identifier) {
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(!btAdapter.isEnabled()){
			host.close();
			GlobalState.getState().goToStart = true;
			finish();
		}
		onDisconnect(identifier);
		
	}
	
	@Override
	public void onControllerReconnect(S identifier) {
		if (!reconnections.contains(identifier)){
			reconnections.add(identifier);
		}
	}


	@Override
	public void onInvalidMessageReceived(S identifier) {
		if (playerList.contains(identifier)){
			playerList.remove(identifier);
			host.removeController(identifier);
			onDisconnect(identifier);
			//Toast.makeText(this, "invalid message received", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
	
}