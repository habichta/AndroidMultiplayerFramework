package ch.ethz.inf.vs.ballgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import ch.ethz.inf.vs.Player;
import ch.ethz.inf.vs.ballgame.powerups.PowerUp;
import ch.ethz.inf.vs.ballgame.powerups.PowerUpBomb;
import ch.ethz.inf.vs.ballgame.powerups.PowerUpBoost;
import ch.ethz.inf.vs.ballgame.powerups.PowerUpGhost;
import ch.ethz.inf.vs.ballgame.powerups.PowerUpGravity;
import ch.ethz.inf.vs.ballgame.powerups.PowerUpMass;
import ch.ethz.inf.vs.ballgame.powerups.PowerUpMinusOne;
import ch.ethz.inf.vs.ballgame.powerups.PowerUpReverse;
import ch.ethz.inf.vs.ballgame.powerups.PowerUpShuffle;
import ch.ethz.inf.vs.ballgame.powerups.PowerUpStop;
import ch.ethz.inf.vs.controllerLib.ControllerMessage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * The controller computes any logical game behaviour (physics, popups etc.) and calls the functions to the views of the map, balls etc.
 */

public class GameController {
	
	/* Sound Variables */
	private List<Integer> bgSounds;//The sounds that are played in the background
	private MediaPlayer collisionSound; //Plays a sound of colliding balls
	protected MediaPlayer backgroundSound; //Plays a background sound from bgSounds
	protected MediaPlayer appearSound; //Plays a background sound from bgSounds
	private Iterator<Integer> soundIt; //Used to iterate over the bgSounds
	
	/**
	 * List of all Balls that are on the map
	 */
	public List<Ball> Balls;
	/**
	 * List of all PowerUps that are currently on the map.
	 */
	public List<PowerUp> PowerUps;
	/**
	 * Represents the map
	 */
	public GameMap Map;
	
	/* Control Variables */
	/**
	 * As long this is true the tokens are animated (during the round)
	 */
	public boolean running = true; 
	
	/**
	 * As long as this is false we start a new round after each round.
	 */
	public boolean gameOver = false;
	/**
	 * If true, the game pauses (stops animation)
	 */
	public boolean paused = false;
	/**
	 * The current round in the game
	 */
	public int round = 1;
	/**
	 * Time until the next powerup appears in miliseconds
	 */
	public int puCountDown = 0;
	
	/* Other Variables */
	private GameController gc = this; //Needed to pass in Runnable
	private HostActivity a; //Used for debugging
	public TextView text; //Used to display messages
	public TextView[] scores = new TextView[0]; //Display the scores
	public LinearLayout scoreTable;
	
	public GameController(HostActivity act, GameMap m){
		bgSounds = new ArrayList<Integer>();
		
		//Add all sounds here
		//Source of this files is http://www.newgrounds.com/
		//TODO check license of media files (only music, sounds are under free license)
		bgSounds.add(R.raw.background1);
		bgSounds.add(R.raw.background2);
		bgSounds.add(R.raw.background3);
		bgSounds.add(R.raw.background4);
		
		//Random order
		Collections.shuffle(bgSounds);
		soundIt = bgSounds.iterator();
		
		a = act;
		Balls = new ArrayList<Ball>();
		PowerUps = new ArrayList<PowerUp>();
		Map = m;
		appearSound  = MediaPlayer.create(a, R.raw.appear);
		collisionSound  = MediaPlayer.create(a, R.raw.collision);
		backgroundSound = MediaPlayer.create(a, (int) soundIt.next());
		backgroundSound.setVolume(0.7f, 0.7f);
		backgroundSound.setOnCompletionListener(new OnCompletionListener() {
		    @Override
		    public void onCompletion(MediaPlayer mp) {
		    	backgroundSound.release();
		    	if(running){
			        if(soundIt.hasNext()){
			        	backgroundSound = MediaPlayer.create(a, (int) soundIt.next());
			        	backgroundSound.start();
			        }else{
			        	Collections.shuffle(bgSounds);
			        	soundIt = bgSounds.iterator();
			        	backgroundSound = MediaPlayer.create(a, (int) soundIt.next());
			        	backgroundSound.start();
			        }
		    	}
		    }
		});
		
		//Add Text view for information
		text = new TextView(a);
		text.setTextSize(50);
		text.setTextColor(Color.WHITE);
		text.setShadowLayer(3.5f, 2, 2, Color.BLACK);
		//FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		//params.topMargin = Map.getScreenHeight()/2 - 70;
		text.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		//a.addContentView(text, params);	
	}
	/**
	 * Adds a random powerup at a random position to the game
	 */
		public void addPowerUp(){
			
			//Search for free position
			boolean collided = false;
			int[] PUpos = new int[2];
			Random r = new Random();
			do{
				collided = false;
				PUpos[0] = r.nextInt(Map.getWidth());
				PUpos[1] = r.nextInt(Map.getHeight());
				if(outOfMap(PUpos) != 0) collided = true;
				for(Ball b: Balls) if(b.collides(PUpos)) collided = true;
				for(PowerUp p : PowerUps) if(p.collides(PUpos)) collided = true;
			}while(collided);
			
			
			//__________________________INCREASE TO ADD POWERUP ________________________________________
			int NUM_POWERUPS = 9;
			
			int PowerUpRadius = (Map.BallRadius * 2) / 3;
			
			int puID = r.nextInt(NUM_POWERUPS);
			PowerUp pu;
			
			//just for testing
			//puID = 4;
			
			//_______________________ADD CASE FOR NEW POWERUP________________________
			switch(puID){
			case 0:
				pu = new PowerUpStop(a, PUpos, PowerUpRadius);
				break;
			case 1:
				pu = new PowerUpMinusOne(a, PUpos, PowerUpRadius);
				break;
			case 2:
				pu = new PowerUpReverse(a, PUpos, PowerUpRadius);
				break;
			case 3:
				pu = new PowerUpBoost(a, PUpos, PowerUpRadius);
				break;
			case 4:
				pu = new PowerUpBomb(a, PUpos, PowerUpRadius);
				break;
			case 5:
				pu = new PowerUpShuffle(a, PUpos, PowerUpRadius);
				break;
			case 6:
				pu = new PowerUpMass(a, PUpos, PowerUpRadius);
				break;
			case 7:
				pu = new PowerUpGhost(a, PUpos, PowerUpRadius);
				break;
			case 8:
				pu = new PowerUpGravity(a, PUpos, PowerUpRadius);
				break;
			default:
				pu = new PowerUpStop(a, PUpos, PowerUpRadius);
			}

			PowerUps.add(pu);
			appearSound.start();
			
			setPUCountDown();
		}
		
	public class PointsComparator implements Comparator<Ball> {
	    @Override
	    public int compare(Ball b1, Ball b2) {
	        return (-1) * ((Integer)b1.Points).compareTo((Integer)b2.Points);
	    }
	}
	
	/**
	 * Prepares the scores of all players to be shown later
	 */
	public void setScores(){
		scoreTable = new LinearLayout(a);
		scoreTable.setGravity(Gravity.CENTER);
		scoreTable.setOrientation(LinearLayout.VERTICAL);

		scoreTable.addView(text);
		for(int i = 0; i < Balls.size(); i++){
			TextView t = new TextView(a);
			t.setTextSize(30);
			t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
			t.setShadowLayer(3.5f, 2, 2, Color.BLACK);
			scoreTable.addView(t);
		}
		FrameLayout.LayoutParams listParams = new FrameLayout.LayoutParams(Map.getScreenWidth(), Map.getScreenHeight());
		a.addContentView(scoreTable, listParams);
	}
	
	/**
	 * Displays the score of all players on the screen
	 */
	public void showScores(){
		a.runOnUiThread(new Runnable(){
			public void run(){

				if(scoreTable == null){
					setScores();
				}
				for(int i = 1; i < scoreTable.getChildCount();i++){
					TextView t = (TextView) scoreTable.getChildAt(i);
					setText(t,Balls.get(i-1).name + ": " + Balls.get(i-1).Points, Balls.get(i-1).color);
				}
				scoreTable.bringToFront();
				scoreTable.invalidate();
			}
		});
	}
	
	/**
	 * Hides the scores of the players from the screen
	 */
	public void hideScores(){
		a.runOnUiThread(new Runnable(){
			public void run(){
				for(int i = 0; i < scoreTable.getChildCount();i++){
					TextView t = (TextView) scoreTable.getChildAt(i);
					t.setText("");
					t.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});
	}
	
	/**
	 * Hides the textfield that is used to show messages.
	 */
	public void hideText(){
		a.runOnUiThread(new Runnable(){
			public void run(){
				text.setBackgroundColor(Color.TRANSPARENT);
				text.setText("");
			}
		});
	}
	
	/**
	 * Adds the Ball to the Balls array. This means it will be a part of the game
	 */
	public void addBall(Ball b){
		Balls.add(b);
	}
	
	/**
	 * Returns 0 if the given position is on the map. Otherwise it returns 1 if top of the map, 2 if right, 3 if bottom and 4 if top.
	 */
	private int outOfMap(int[] Position){
		if(Position[0] < Map.getLeftMargin()) return 4;
		if(Position[1] < Map.getTopMargin()) return 1;
		if(Position[0] > Map.getLeftMargin() + Map.getWidth()) return 2;
		if(Position[1] > Map.getTopMargin() + Map.getHeight()) return 3;
		return 0;
	}
	
	
	/**
	 * Pauses the Game and shows the pause dialog
	 */
	public void pause() {
		if(backgroundSound.isPlaying())
			backgroundSound.pause();
		paused = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(a);
		String[] stringArray = new String[] { "Resume", "Leave" };
		ListView pausedList = new ListView(a);
		ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(a, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
		pausedList.setAdapter(modeAdapter);
		builder.setTitle("Game paused");
		builder.setView(pausedList);

		final Dialog dialog = builder.create();

		pausedList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					gc.paused = false;
					dialog.hide();
					if(!backgroundSound.isPlaying())
						backgroundSound.start();
				} else {
					gc.stop();
					// Go back to start
					// TODO Go to lobby or setup activity
					a.goBackToLobby();
				}

			}
		});
		dialog.show();
	}

	/**
	 * Handles the information given in the Transmission t for the player p
	 */
	public void update(Transmission t, Player p){
		for(Ball b : Balls){
			if(b.getPlayer().equals(p)){
				if(t.getAcceleration() != null) b.setAcc(t.getAcceleration());
				if(t.getPowerup() != null) b.activatePowerUp(t.getPowerup());
			}
		}
	}
	
	/**
	 * Shows the Countdown text and wait's until its 0.
	 */
	public void countdown(){
		for(int i = Config.COUNTDOWN_SECONDS; i > 0; i--){
			setText(text, "Starting Round " + round + " in " + i, Color.WHITE);
			SystemClock.sleep(1000);
		}
		hideText();
	}
	
	/**
	 * Displays the given text t in tv in the given color
	 */
	public void setText(final TextView tv, final String t, final int color){
		a.runOnUiThread(new Runnable(){
			public void run(){
				tv.setTextColor(color);
				tv.setBackgroundColor(Config.BG_COLOR);
				tv.setText(t);
				tv.invalidate();
			}
		});
	}
	
	/**
	 * Stops the game (GameOver)
	 */
	public void stop(){
		running = false;
		gameOver = true;
		paused = false;
		if(backgroundSound.isPlaying())
			backgroundSound.pause();
		round = 1;
	}
	
	/**
	 * Start the game by listening to the players and updating the views
	 */
	public void start(){
		if(!backgroundSound.isPlaying())
			backgroundSound.start();
		final Handler h = new Handler();
		final Runnable r = new Runnable()
		{
		    public void run() 
		    {
		    	int alive = 0;
		    	for(Ball b : Balls){
		    		if(!b.out){
		    			b.go(gc);
		    			alive++;
		    		}
		    	}
		    	//Check if we have to add a powerup
		    	puCountDown -= Config.WAITTIME;
		    	if(puCountDown <= 0){
		    		addPowerUp();
		    	}
		    	
		    	if(alive <= 1) running = false;
		    }
		};
		Thread t = new Thread()
		{
		    @Override
		    public void run() {
		        try {
		        	//Set points to zero
		        	for(Ball b : Balls){
	        			b.sendPoints();
	        		}
		        	
		        	//Set puCountDown
		        	setPUCountDown();
		        	
		        	while(!gameOver){
		        		//New Round
		        		setText(text, "Starting Round " + round + " in " + Config.COUNTDOWN_SECONDS, Color.WHITE);
			            showScores();
			            
			            //remember that round is starting
			            for(Ball b : Balls){
		        			b.sendVibrate(Config.VIBRATE_NEW_ROUND);
		        			b.sendStartRound();
		        		}
			            			    		
			            //wait 5 seconds that player can prepare
			            countdown();
			            
			            //Make sure everybody received start (if phone was not ready before countdown
			            for(Ball b : Balls){
		        			b.sendStartRound();
		        		}
			            
			            hideScores();
			            
		        	    while(running) {
		        	    	//if paused wait
		        	    	while(paused) sleep(300);
			                sleep(Config.WAITTIME);
			                h.post(r);
			            }
			            			            
			            Collections.sort(Balls, new PointsComparator());
			            
			           checkPlayerWon();
			            
			            if(gameOver){
			            	for(Ball b : Balls){
			        			b.sendGameOver(true);
			        		}
			            	break;
			            }else{
			            	for(Ball b : Balls){
			        			b.sendGameOver(false);
			        		}
			            }
			            
			            round++;
			            
			            //Let him realize, that he won. Otherwise it looks like the game crashes and restarts
			            sleep(1000);
			            
			            //Initialize the next round
		        		for(Ball b: Balls) {
		        			for(PowerUp p : b.activePowerUps){
		        				b.removeActivePowerUp(p);
		        			}
		        			b.reset();
		        		}
		        				        	
			            running = true;
		        	}
		        	if(backgroundSound.isPlaying())
		        		backgroundSound.stop();
		            
		            //showScores and won message
		            setText(text, "Player " + Balls.get(0).name + " won!", Balls.get(0).color);
		            showScores();
		            
		            
		        } catch (InterruptedException e) {
		            e.printStackTrace();
		        }
		    }

		};
		t.start();
	}
	
	/**
	 * Sets a random time in an interval (config) until the next powerup appears
	 */
	protected void setPUCountDown() {
		Random r = new Random();
		int max = Config.PowerUpInterval[1] - Config.PowerUpInterval[0];
		int cd = r.nextInt(max);
		cd += Config.PowerUpInterval[0];
		this.puCountDown = cd;
	}

	/**
	 * Checks if a player won. If true it ends the game and displays the final scores
	 */
	public void checkPlayerWon(){
		//Assume Balls array is sorted by points (desc)
		if(Balls.get(0).Points >= 10 * (Balls.size()-1) && Balls.get(0).Points > Balls.get(1).Points + 1)
			stop();
	}
	/**
	 * Gets triggered if two balls collide
	 */
	public void onCollide(){
		collisionSound.start();
	}
}
