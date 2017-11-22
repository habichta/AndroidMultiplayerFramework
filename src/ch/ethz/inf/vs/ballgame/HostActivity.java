package ch.ethz.inf.vs.ballgame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.*;
import ch.ethz.inf.vs.Player;
import ch.ethz.inf.vs.controllerLib.AbstractHostActivity;
import ch.ethz.inf.vs.controllerLib.ControllerMessage;
import ch.ethz.inf.vs.hostControllerLib.IHostCallback;


public class HostActivity extends AbstractHostActivity<Player, Transmission> {
	public GameController gc;
	List<Player> inactivePlayers;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//Create a new simple Map with Ball Radius 4% of screen height.
		GameMap simpleMap = new GameMap(this, 0.035);
		gc = new GameController(this, simpleMap);
		//Get the players from the lobby
		
		List<Player> players =  this.getPlayerList();
		
		//Add for testing a dummy player bec. I have nobody to play with :(
		if(players.size() < 2){
			Player dummy = new Player();
			dummy.setName("DUMMY");
			dummy.setColor(CustomColours.grey);
			players.add(dummy);
		}
		//Supported up to 12 different colors
		//TODO remove these
		int[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN, Color.WHITE, Color.BLACK,
						CustomColours.purple, //Purple
						CustomColours.orange, //Orange
						CustomColours.brown, //Brown
						CustomColours.grey}; //Grey
		
		//Crate a ball for each player
		int i = 0;
		for(Player p : players){
			Ball b = new Ball(this, simpleMap, p, p.getColor());
			b.name = p.getName();
			//all to center for testing
			//double[] acc = {simpleMap.getWidth()/2 - b.Position[0], simpleMap.getHeight()/2 - b.Position[1]};
			//double accLength = Math.sqrt(Math.pow(acc[0], 2) + Math.pow(acc[1], 2));
			//acc[0] = acc[0] / accLength * 60;
			//acc[1] = acc[1] / accLength * 60;
			//b.accVector = acc;
			gc.addBall(b);
		}
	
		inactivePlayers = new ArrayList<Player>();
			
		//Start animation by starting the GameController
		gc.start();
		
	}
	
	@Override
	public void onBackPressed() {
		if(gc.gameOver) goBackToLobby();
	    else gc.pause();
	}
	
	@Override
	public void onDestroy() {
		//if(!gc.gameOver) gc.pause();
		super.onDestroy();
	}
	@Override
	public void onPause() {
		if(!gc.gameOver) gc.pause();
	    super.onPause();
	}
	
	@Override
	public void onResume() {
	   super.onResume();
	}
	
	@Override
	public void onStop() {
	   super.onStop();
	}
	
	
	public void logDebug(String msg){
		Log.d("DEBUG", msg);
	}
	
	public void onDisconnect(Player p){
		//for new pause and wait for reconnect
		inactivePlayers.add(p);
		gc.backgroundSound.pause();
		gc.paused = true;
	}
	

	
	public void onReconnect(Player p){
		inactivePlayers.remove(p);
		if (inactivePlayers.isEmpty()){
			gc.paused = false;
			gc.backgroundSound.start();
		}
	}


	@Override
	public void onReceive(Transmission b, Player p) {
		
		//Communication Layer
		switch (b.getType()){
		case LEAVE_REQUEST:
			gc.stop();
			goBackToLobby();
			break;
		case PAUSE_REQUEST:
			if(gc.gameOver) goBackToLobby();
		    else {
		    	gc.backgroundSound.pause();
				gc.paused = true;
		    }
			break;
		case RESUME_REQUEST:
			gc.paused = false;
			gc.backgroundSound.start();
			break;
		case UPDATE:
			gc.update(b, p);
		default:
			break;
		
		}
		
	}

}
