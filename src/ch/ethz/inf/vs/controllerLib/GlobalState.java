package ch.ethz.inf.vs.controllerLib;

import java.util.List;

import android.bluetooth.BluetoothDevice;

import ch.ethz.inf.vs.Player;
import ch.ethz.inf.vs.hostControllerLib.ICommunicator;
import ch.ethz.inf.vs.hostControllerLib.IController;
import ch.ethz.inf.vs.hostControllerLib.IHost;

public class GlobalState {

	private GlobalState(){}
	
	private static GlobalState state;

	public static GlobalState getState(){
		if(state == null){
			state = new GlobalState();
		}
		return state;
	}
	
	public ICommunicator communicator;
	
	public IController controller;
	
	public IHost host;
	
	public boolean isHost;
	
	public List<?> players;
	
	BluetoothDevice device;
	
	public Object self;
	
	public boolean resumeLobby = false;
	
	public boolean goToStart = false;
}
