package ch.ethz.inf.vs.ballgame.powerups;

import ch.ethz.inf.vs.ballgame.Ball;
import ch.ethz.inf.vs.ballgame.Config;
import ch.ethz.inf.vs.ballgame.GameController;
import ch.ethz.inf.vs.ballgame.HostActivity;
import ch.ethz.inf.vs.ballgame.R;
import ch.ethz.inf.vs.ballgame.R.drawable;
import ch.ethz.inf.vs.ballgame.R.raw;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/*
 * Represents the logical PowerUp.
 */
public abstract class PowerUp {
	
	/*
	 * A unique ID to identify this kind of powerup. This ID will be sent to the player
	 */
	public int ID;
	
	/*
	 * The image for the visual represantation of the powerup. Should be a round png
	 */
	public Drawable imgSource;
	protected ImageView img;
	
	/*
	 * The position of the powerup. Anchor point is top left of the screen.
	 */
	public int[] Position;
	
	/*
	 * The radius of the powerup
	 */
	public int Radius;
	
	/*
	 * The parameters to set the position of the powerup
	 */
	protected FrameLayout.LayoutParams params;
	
	/*
	 * MediaPlayer to play pickup sound
	 */
	protected MediaPlayer pickUp;
	
	/*
	 * Access this to run powerup on other balls etc.
	 */
	protected GameController gc;
	
	//Context
	protected HostActivity a;
	
	/*
	 * The Resource of the image that has to be shown as long the PowerUp is active
	 */
	public int activeResourceID = -1;
	
	/*
	 * Time the PowerUp stays active
	 */
	public int ActiveTime = 0;
	
	/*
	 * Creates a new PowerUp with radius rad at position pos.
	 * @param act The HostActivity as context
	 * @param pos Position relative to the screen
	 * @param rad Radius of the PowerUp
	 */
	public PowerUp(HostActivity act, int[] pos, int rad){
		a = act;
		Position = pos;
		Radius = rad;
		img = new ImageView(a);
		
		setID();
		
		//Default source
		imgSource = img.getResources().getDrawable(R.drawable.powerupdefault);
		img.setImageDrawable(img.getResources().getDrawable(Config.powIDtoDrawable.get(ID)));
		params = new FrameLayout.LayoutParams(Radius * 2, Radius * 2);
		params.topMargin = Position[1] - Radius;
        params.leftMargin = Position[0] - Radius;
        a.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				a.addContentView(img, params);
			}
        });
        pickUp  = MediaPlayer.create(a, R.raw.pickup);
        gc = a.gc;
	}
	
	/*
	 * Defines the image that is shown as long as the PowerUp is active. Needed if ActiveTime > 0.
	 */
	public void setActiveResource(int id){
		this.activeResourceID = id;
	}
	
	/*
	 * Set the active time if you want the PowerUp to be active over a time span.
	 * @param tThe time the PowerUp stays active.
	 */
	public void setActiveTime(int t){
		ActiveTime = t;
	}
	
	/*
	 * Decreases the time by the defined time between two frames
	 */
	public void tick(Ball b){
		ActiveTime -= Config.WAITTIME;
	}
	
	/*
	 * Set ID = uniqueID, where uniqueID is an int that isn't used in another PowerUp. They should be continous.
	 */
	abstract void setID();
	
	/*
	 * Prints msg to LogCat with tag DEBUG
	 */
	public void debug(String msg){
		a.logDebug(msg);
	}
	
	/*
	 * Checks if the ball collides with the powerup.
	 */
	public boolean collides(Ball b){
		double[] connectingVector = new double[2];
		connectingVector[0] = Position[0] - b.Position[0];
		connectingVector[1] = Position[1] - b.Position[1];
		//Scalar Product
		double distance = Math.sqrt(Math.pow(connectingVector[0], 2) + Math.pow(connectingVector[1], 2));
		//returns true if the two balls touch
		return distance <= this.Radius + b.getRadius();
	}
	
	/*
	 * Gets called as soon as the active time is over
	 */
	public void onDeactivate(Ball b){
	}
	
	/*
	 * The given Ball takes the PowerUp
	 */
	public void take(Ball b){
		//If he has all powerup slots full he slides through it
		if(b.PowerUps.size() < 4){
			
			
			//Send power up to player
			b.sendPowerUp(ID);
			b.PowerUps.add(this);
			
			//Play sound for pick up
			pickUp.start();
			
			//Remove from PowerUp list and view
			remove();
			
		}
	}
	
	public void onGo(Ball b){
		
	}
	
	/*
	 * Removes the PowerUp from the map
	 */
	public void remove(){
		a.gc.PowerUps.remove(this);
		 a.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				img.setVisibility(View.GONE);
			}
        });
	}
	
	/*
	 * Triggers the action that is defined for the PowerUp and if it has an active time it's added to the ball as an active PowerUp
	 */
	public void run(Ball b){
		action(b);
	}
	
	/*
	 * Defines the action for this PowerUp. Do not call this function directly, use run(Ball b) instead.
	 */
	public abstract void action(Ball b);
	
	/*
	 * Returns if a PowerUp would collide with an other PowerUp at the given position
	 */
	public boolean collides(int[] pos) {
		double[] connectingVector = new double[2];
		connectingVector[0] = Position[0] - pos[0];
		connectingVector[1] = Position[1] - pos[1];
		//Scalar Product
		double distance = Math.sqrt(Math.pow(connectingVector[0], 2) + Math.pow(connectingVector[1], 2));
		return distance <= 2*this.Radius;
	}
}
