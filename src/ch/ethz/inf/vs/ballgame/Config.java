package ch.ethz.inf.vs.ballgame;

import java.util.HashMap;

import ch.ethz.inf.vs.ballgame.powerups.PowerUpStop;

import android.graphics.Color;

public class Config {
	
	//The time we wait between to frames
	public static final int WAITTIME = 30;
	//The time we wait befor we send a new acc vector
	public static final long WAITTIME_SEND = 40;
	
	//The epsilon used in the impulse conservation law. The higher it is the more elastic the balls seem.
	//Must be between 0 and 1
	public static final double EPSILON = 0.75;

	//The gravitiy, if a token falls out of the map
	public static final double FALL_GRAVITY = 5;
	
	//The higher this divisor, the less friction we have at a collision (for rotation)
	public static final double FRICTION_DIV = 5;
	
	//Used to scale the acceleration vector
	public static final int ACCELERATION_SCALE = 80;

	public static final int COUNTDOWN_SECONDS = 5;
	
	public static final int BG_COLOR = Color.argb(140, 140, 140, 140);
	
	public static final int VIBRATE_COLLISION = 50;
	public static final int VIBRATE_DIED = 200;
	public static final int VIBRATE_NEW_ROUND = 400;
	
	//Add powerup all 10 to 30 seconds (random value)
	public static final int[] PowerUpInterval = {5000, 15000};
	
	public static final HashMap<Integer, Integer> powIDtoDrawable = new HashMap<Integer,Integer>(){
		@Override
		public Integer get(Object key) {
			Integer entry = super.get(key);
			
			if(entry == null)
				return R.drawable.powerupdefault;
			
			return entry;
		}		
	};
	
	static{
		powIDtoDrawable.put(0, R.drawable.powerupstop);
		powIDtoDrawable.put(1, R.drawable.powerupminus1);
		powIDtoDrawable.put(2, R.drawable.powerupreverse);
		powIDtoDrawable.put(3, R.drawable.powerupboost);
		powIDtoDrawable.put(4, R.drawable.powerupbomb);
		powIDtoDrawable.put(5, R.drawable.powerupshuffle);
		powIDtoDrawable.put(6, R.drawable.powerupmass);
		powIDtoDrawable.put(7, R.drawable.powerupghost);
		powIDtoDrawable.put(8, R.drawable.powerupgravity);
	};

	
}
