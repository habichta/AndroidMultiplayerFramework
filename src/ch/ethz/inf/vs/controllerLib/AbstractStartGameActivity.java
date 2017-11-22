package ch.ethz.inf.vs.controllerLib;

import java.io.Serializable;

import ch.ethz.inf.vs.ballgame.PlayerActivity;
import ch.ethz.inf.vs.ballgame.R;
import ch.ethz.inf.vs.ballgame.R.id;
import ch.ethz.inf.vs.ballgame.R.layout;
import ch.ethz.inf.vs.ballgame.util.SystemUiHider;
import ch.ethz.inf.vs.hostControllerLib.ICommunicator;
import ch.ethz.inf.vs.hostControllerLib.IController;
import ch.ethz.inf.vs.hostControllerLib.IControllerCallback;
import ch.ethz.inf.vs.hostControllerLib.bluetooth.ComException;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


public abstract class AbstractStartGameActivity<S extends Serializable, T extends Serializable> extends Activity implements IControllerCallback<S, ControllerMessage>{
	
	private ICommunicator<S,ControllerMessage,BluetoothDevice> communicator;
	private IController<S,ControllerMessage> controller;
	private boolean isHost;
	private S identifier;
	
	private boolean backPressed = false;
	
	private Intent playerActivityIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		
		identifier = (S) GlobalState.getState().self;
		communicator = GlobalState.getState().communicator;
		controller = GlobalState.getState().controller;
		isHost = GlobalState.getState().isHost;
		
		
		controller.setNewCallback(this);
		controller.resumeReceiving();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (GlobalState.getState().resumeLobby){
			GlobalState.getState().resumeLobby = false;
			controller.setNewCallback(this);
			controller.resumeReceiving();
		} else if (GlobalState.getState().goToStart){
			finish();
			GlobalState.getState().goToStart = false;
		}
	}

	@Override
	public void finish() {
		ControllerMessage disconnectMessage= new ControllerMessage(ControllerMessage.MessageType.C_DISCONNECTED,null);
		try{
		controller.send(disconnectMessage);
		//Toast.makeText(getApplicationContext(), "Disconnected from Host", Toast.LENGTH_SHORT).show();
		}catch(Exception e){
			//TODO  more specific Exception?
			e.printStackTrace();	
		}
		controller.close();
		
		super.finish();
	}


	@Override
	public void onReceive(ControllerMessage message) {
		
		switch(message.messageType){
		case C_DISCONNECTED:
			onBackPressed();
			break;
		case C_REGISTRATION_MESSAGE:
			break;
		case C_REGISTRATION_OK:
			break;
		case GAME_MESSAGE:
			try{
				onReceive((T)message.message);
			} catch (Exception e){	}
			break;
		case START_GAME:
			controller.pauseReceiving();
			onGameStart();
			break;
		default:
			break;
		
		}
		
		
	}
	
	/**
	 * call this in your onCreate() to set the activity of the game stage
	 * @param playerActivity the class of the playerActivity
	 */
	public void setPlayerActivity(Class< ? extends AbstractPlayerActivity> playerActivity){
		playerActivityIntent = new Intent(this,playerActivity);
	}
	
	/**
	 * Call this to notify the host to start the game
	 */
	public void startGame(){
		//send start initiation message to host
		ControllerMessage startRequest = new ControllerMessage(ControllerMessage.MessageType.START_GAME,null);
		
		try{
		controller.send(startRequest);
		//Toast.makeText(getApplicationContext(), "Start request sent", Toast.LENGTH_SHORT).show();
		
		}catch(Exception e){
			e.printStackTrace();
			//Toast.makeText(getApplicationContext(), "Start request failed", Toast.LENGTH_SHORT).show();
			onBackPressed();
		}
		
	}
	
	/**
	 * Gets called when the game starts. 
	 * Call super.onGameStart() after your own implemetation of this method
	 */
	public void onGameStart(){
		startActivity(playerActivityIntent);
	}
	
	@Override
	public void onConnectionEstablished() {}

	
	Handler callBackHandler = new Handler();
	@Override
	public Handler getCallbackHandler() {
		return callBackHandler;
	}


	/**
	 * @return the identifier
	 */
	public S getIdentifier() {
		return identifier;
	}


	public void send(T m){
		try{
			ControllerMessage message = new ControllerMessage(ControllerMessage.MessageType.GAME_MESSAGE,m);
			controller.send(message);
		} catch (Exception e){
			e.printStackTrace();
			//Toast.makeText(this, "send failed", Toast.LENGTH_SHORT).show();
			onBackPressed();
		}
	}


	public abstract void onReceive(T m);

	@Override
	public void onBackPressed() {
		backPressed = true;
		super.onBackPressed();
	}
	
	// TODO Romy: Exceptions are not propagated anymore so this method isn't part of 
	// the Callback anymore. I removed the @Override. Please remove this method.
	public void onReceiveException(ComException exception) {
		if (!backPressed){
			backPressed = true;
			finish();
		}
		
	}

	@Override
	public void onDisconnect() {
		finish();
		
	}

	@Override
	public void onInvalidMessageReceived() {
		finish();
		//Toast.makeText(this, "invalid message received", Toast.LENGTH_SHORT).show();
		
	}
	
}
