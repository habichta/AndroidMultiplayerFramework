package ch.ethz.inf.vs.controllerLib;

import java.io.Serializable;

import ch.ethz.inf.vs.hostControllerLib.IController;
import ch.ethz.inf.vs.hostControllerLib.IControllerCallback;
import ch.ethz.inf.vs.hostControllerLib.bluetooth.ComException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public abstract  class AbstractPlayerActivity<S extends Serializable, T extends Serializable> extends Activity implements IControllerCallback<S, ControllerMessage>{

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		controller = GlobalState.getState().controller;
		controller.setNewCallback(this);
		controller.resumeReceiving();
		
		identifier = (S) GlobalState.getState().self;
	}
	
	IController<S,ControllerMessage> controller;
	
	private S identifier;
	
	public S getIdentifier(){
		return identifier;
	}
	
	
	public void onDisconnect(){
		goBackToStart();
	}
	
	
	/**
	 * gets called when a message b is received
	 * @param b the message
	 */
	public abstract void onReceive(T b);
	
	/**
	 * send a message b to the server
	 * @param b the message
	 */
	public void send(T b){
		try{
			ControllerMessage message = new ControllerMessage(ControllerMessage.MessageType.GAME_MESSAGE, b);
			controller.send(message);
		} catch (Exception e){
			//Toast.makeText(this, "send failed", Toast.LENGTH_SHORT).show();
		}
	}
	

	@Override
	public void onReceive(ControllerMessage message) {
		switch (message.messageType){
		case C_BACK_TO_LOBBY:
			goBackToLobby();
			break;
		case C_DISCONNECTED:
			break;
		case C_POLL:
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
			break;
		default:
			break;
		
		}
		
	}

	@Override
	public void onConnectionEstablished() {
		//should already be connected		
	}

	Handler callbackHandler = new Handler();
	@Override
	public Handler getCallbackHandler() {
		return callbackHandler;
	}

	private void goBackToLobby(){
		controller.pauseReceiving();
		GlobalState.getState().resumeLobby = true;
		finish();
	}

	// TODO Romy: Exceptions are not propagated anymore so this method isn't part of 
	// the Callback anymore. I removed the @Override. Please remove this method.
	public void onReceiveException(ComException exception) {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "receive exception", Toast.LENGTH_SHORT).show();
		goBackToStart();
	}
	
	private void goBackToStart(){
		controller.pauseReceiving();
		controller.close();
		GlobalState.getState().goToStart = true;
		finish();
	}

	@Override
	public void onInvalidMessageReceived() {
		// TODO Auto-generated method stub
		
	}
	
}
