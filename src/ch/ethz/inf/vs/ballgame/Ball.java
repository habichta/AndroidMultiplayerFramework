package ch.ethz.inf.vs.ballgame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import ch.ethz.inf.vs.Player;
import ch.ethz.inf.vs.ballgame.powerups.PowerUp;

/*
 * Represents the logical Ball (no graphic)
 */
public class Ball {
	
	/* Movement Variables */
	public int Radius; //Radius of the Ball in dp
	public double[] Position; //The current position, x and y value in dp
	public double[] speedVector = {0,0}; //The current speed in pixel/second and direction
	public double speedZ = 0; //The speed in the z - Axis (if we fall down)	
	public double[] accVector = {0,0}; //The acceleration vector in Pixel / second^2
	public double Rotation = 0; //Rotation of the ball on the plane in degrees/sec. Positive is clockwise.
	public double Mass = 1; //The relative mass of the ball, default is 1
	FrameLayout.LayoutParams params; //The parameters of the BallView and the image in the ball view. Used to move the BallView
	public boolean falling = false; //If true, then we animate a falling ball
	//Vars for the fall animation
	private int region = 0; //Where the ball is: 0 in map, 1 top of map, 2 right, 3 bottom and 4 is left.
	private double leftMargin; //Used for the falling animation
	private double topMargin; //Used for the falling animation

	/* Visualisation Variables */
	private BallView view; //The visual representation of this ball
	public int color; //The color of this ball
	
	/* Game Logic Variables */
	private Player player; //The Player as defined by the interface. Used to send messages
	public List<PowerUp> PowerUps; //Available PowerUps
	private HostActivity a; //Context: Used for log messages
	private GameMap gameMap; //The map on that this ball is.
	public int Points = 0;
	public boolean out = false; //true if the ball is out of the map
	
	/* Other Variables */
	MediaPlayer fallSound; //Plays a sound of a falling object
	public String name;
	
	/* PowerUps Variables */
	List<PowerUp> activePowerUps;
	private boolean PowerUpMass = false;
	public boolean isGhost = false;
	

	//Create a new Ball and draw its visual represantation on the map
	public Ball(HostActivity act, GameMap map, Player p, int c){
		PowerUps = new ArrayList<PowerUp>();
		activePowerUps = new ArrayList<PowerUp>();
		color = c;
		a = act;
		fallSound  = MediaPlayer.create(a, R.raw.splash);
		gameMap = map;
		Position = new double[2];
		this.player = p;
		
		this.Radius = gameMap.BallRadius; //The default radius is given in the map
		params = new FrameLayout.LayoutParams(Radius * 2, Radius * 2);
		reset();
		
		//Draw the Ball
		view = new BallView(a, this, color);
        view.setBackgroundColor(Color.TRANSPARENT); 
        a.addContentView(view, params);
        moveToPos(Position);
	}
	
	//Resets the values to the default, except of the points
	public void reset(){
		
		//Set first position
		Random r = new Random();
		do{
  			Position[0] = r.nextInt(gameMap.getWidth());
  			Position[1] = r.nextInt(gameMap.getHeight());
  		}while(!free());
		
		this.Radius = gameMap.BallRadius; //The default radius is given in the map
		this.Mass = 1; //Default Mass
		this.Rotation = 0; //Default Rotation
		this.region = 0;
		this.falling = false;
		this.speedVector[0] = 0;
		this.speedVector[1] = 0;
		this.speedZ = 0;
		this.accVector[0] = 0;
		this.accVector[1] = 0;
		this.out = false;
		
		params.topMargin = getY() - Radius;
        params.leftMargin = getX() - Radius;
		params.height = Radius * 2;
		params.width = Radius * 2;
		
        //search for a random free position
  		if(view != null) moveToPos(Position);
	}
	//returns if the Ball is at a free position
	private boolean free(){
		boolean free = true;
		
		//Check if on map
		if(outOfMap() != 0) free = false;
		
		//Check for collisions with other balls
		for(Ball b: a.gc.Balls){
			if(b != this && collides(b)){
				free = false;
				break;
			}
		}	
		
		//Check for collisions with powerups
		for(PowerUp pu: a.gc.PowerUps){
			if(pu.collides(this)){
				free = false;
				break;
			}
		}
		
		//TODO Check for holes
		
		//TODO Check for Barricades
		
		return free;
		
	}
	public double[] getSpeed(){
		return speedVector;
	}
	public double ySpeed(){
		return speedVector[0];
	}
	public double xSpeed(){
		return speedVector[1];
	}
	public void setSpeed(double[] s){
		this.speedVector = s;
	}
	public int getRadius(){
		return this.Radius;
	}
	public void setRadius(int r){
		this.Radius = r;
	}
	public void setPosition(double[] pos){
		this.Position = pos;
	}
	
	public double[] getPosition(){
		return this.Position;
	}
	
	public int getX(){
		return (int) Position[0];
	}
	
	public int getY(){
		return (int) Position[1];
	}
	
	public void setAcc(double[] acc){
		this.accVector = acc;
	}
	
	public int getColor(){
		return color;
	}
	
	public void setMass(double w){
		this.Mass = w;
	}
	
	public double getMass(){
		return this.Mass;
	}
	
	//Moves the Ball to the given Position on the screen (not map!)
	public void moveToPos(double [] Position){
		a.runOnUiThread(new Runnable(){
			public void run(){
				params.topMargin = getY() - Radius;
		        params.leftMargin = getX() - Radius;
		        view.setLayoutParams(params);
		        view.postInvalidate();
			}
		});		
	}
	
	public void sendStartRound(){
		if(this.name.equals("DUMMY")) return;
		Transmission tr = new Transmission(player.getID());
		tr.setNewRound(true);
		a.send(tr,player);
	}
	
	public void sendPowerUp(int id){
		if(this.name.equals("DUMMY")) return;
		Transmission tr = new Transmission(player.getID());
		tr.setPowerup(id);
		a.send(tr,player);
	}
	
	public void sendVibrate(int duration){
		if(this.name.equals("DUMMY")) return;
		Transmission tr = new Transmission(player.getID());
		tr.setVibrate(duration);
		a.send(tr,player);
	}
	
	public void sendDied(){
		if(this.name.equals("DUMMY")) return;
		Transmission tr = new Transmission(player.getID());
		tr.setDead(true);
		a.send(tr,player);
	}
	
	public void sendGameOver(boolean b){
		if(this.name.equals("DUMMY")) return;
		Transmission tr = new Transmission(player.getID());
		tr.setGameover(b);
		a.send(tr,player);
	}
	
	public void sendPoints(){
		if(this.name.equals("DUMMY")) return;
		Transmission tr = new Transmission(player.getID());
		tr.setPoints(Points);
		a.send(tr,player);
	}
	
	//Calculates the new vectors and moves the ball to the next position
	//Calls the functions to check for collisions and holes
	public void go(GameController gc){
				
		double t = (Config.WAITTIME / 1000.0);
		//if falling simulate a gravity by decreasing the size of the view
		if(!falling){
			boolean move = true;
			
			//Tick time in all active PowerUps
			for(PowerUp p : activePowerUps) p.tick(this);
			//Check if we have to remove powerups (no concurrent modification)
			List<PowerUp> toRemove = new ArrayList<PowerUp>();
			for(PowerUp p : activePowerUps){
				if(p.ActiveTime <= 0) toRemove.add(p);
				else p.onGo(this);
			}
			for(PowerUp p : toRemove) removeActivePowerUp(p);

			
			//New Orientation
			//view.rotate((float)(this.rotation * t));
			
			//Check for powerup collisions
			PowerUp p = null;
			for(PowerUp pu : gc.PowerUps){
				if(pu.collides(this)){
					p = pu;
					break;
				}
			}
			if(p != null) p.take(this);
			
			//New Position s = a/2 t^2 + vt + s0
			Position[0] += (accVector[0]/2) * Math.pow(t, 2) + speedVector[0]*t;
			Position[1] += (accVector[1]/2) * Math.pow(t, 2) + speedVector[1]*t;
			
			//New Speed v = at + v0
			speedVector[0] += (double)accVector[0] * t;
			speedVector[1] += (double)accVector[1] * t;
			
			
			if(checkOutOfMap()) move = false;
			
			//Check for collisions
			if(!isGhost){
			for(Ball b : gc.Balls){
				if(b != this && !b.falling && collides(b)){
					this.onCollide();
					b.onCollide();
					gc.onCollide();
					//Simulate the collision by updating positions and speed vectors of both balls
					bounce(b);
					move = false;
				}
			}
			}
			if(move)
				moveToPos(Position);
		}else{
			//Fall animation
			if(params.width > 10){
				
				speedZ += Config.FALL_GRAVITY * t;
				double dec = (Config.FALL_GRAVITY/2) * Math.pow(t, 2) + speedZ*t;
				if(params.width - dec > 0){
					params.width -= dec;
					params.height -= dec;
				}else{
					params.width = 1;
					params.height = 1;
				}
				
				//Keep at border of screen
				switch(region){
					case 1: //top
						if(params.height < gameMap.getTopMargin()) params.topMargin = gameMap.getTopMargin()/2 - params.height/2;
						else params.topMargin = 0;
						leftMargin += dec / 2;
						params.leftMargin = (int)leftMargin;
						break;
					case 2: //right
						if(params.width < gameMap.getLeftMargin()) params.leftMargin = gameMap.getScreenWidth() - gameMap.getLeftMargin()/2 - params.width/2;
						else params.leftMargin = gameMap.getScreenWidth() - params.width;
						topMargin += dec / 2;
						params.topMargin = (int)topMargin;
						break;
					case 3: //bottom
						if(params.height < gameMap.getTopMargin()) params.topMargin = gameMap.getScreenHeight() - gameMap.getTopMargin()/2 - params.height/2;
						else params.topMargin = gameMap.getScreenHeight() - params.height;
						leftMargin += dec / 2;
						params.leftMargin = (int)leftMargin;
						break;
					case 4: //left
						if(params.width < gameMap.getLeftMargin()) params.leftMargin = gameMap.getLeftMargin()/2 - params.width/2;
						else params.leftMargin = 0;
						topMargin += dec / 2;
						params.topMargin = (int)topMargin;
						break;
				}
				
				view.setLayoutParams(params);
				view.postInvalidate();
			}else{
				out = true;
			}
		}
	}
	
	//Checks if the ball is out of the map at one of the borders
	public boolean checkOutOfMap(){
		int outMap = outOfMap();
		if(outMap != 0){
			
			//Store information for falling informatio
			region = outMap;
			leftMargin = (int) params.leftMargin;
			topMargin = (int) params.topMargin;
			speedZ = Math.sqrt(Math.pow(Position[0], 2) + Math.pow(Position[1], 2)) * Config.EPSILON/1.5;
			//Trigger event
			onFall();
			
			return true;
		}else{
			return false;
		}
	}
	
	//returns 0 if not out of map or: 1=top, 2=right, 3=bottom, 4=left
	public int outOfMap(){
		if(Position[0] < gameMap.getLeftMargin()) return 4;
		if(Position[1] < gameMap.getTopMargin()) return 1;
		if(Position[0] > gameMap.getLeftMargin() + gameMap.getWidth()) return 2;
		if(Position[1] > gameMap.getTopMargin() + gameMap.getHeight()) return 3;
		return 0;
	}
	
	//Gets triggered if the Ball collides with an other ball or barricade
	public void onCollide(){
		sendVibrate(Config.VIBRATE_COLLISION);
	}
	
	//Gets triggered if the ball falls out of the map.
	public void onFall(){
		//a.gc.running = false; //Pauses the game for testing
		falling = true;
		fallSound.start();
		//Update Points of all other balls
		for(Ball b : a.gc.Balls){
			if(b != this && !b.falling){
				b.Points++;
				b.updateView();
				b.sendPoints();
			}
		}
		sendVibrate(Config.VIBRATE_DIED);
		sendDied();
	}
	
	//updates the image on the token to the points
	public void updateView(){
		view.updatePoints(Points);
	}
	
	//Calculates the new Positions and SpeedVectors of two colliding balls (this and b)
	private void bounce(Ball b){
		
		//Compute the normals and the distance between the two balls (length)
		double normalx = b.Position[0] - this.Position[0];
		double normaly = b.Position[1] - this.Position[1];
		double length = (double)Math.sqrt(Math.pow(normalx, 2) + Math.pow(normaly, 2));
		normalx /= length;
		normaly /= length;
		
		//TODO Calculate new rotation of both balls
		//Othogonal to normal: x1 * y1 + x2 * y2 = 0 => set y1 = 1 => y2 = (-x1 * y1) / x2 = - x1/x2
		double[] rotDir = {1, -(normalx / normaly)};
		double dirLength = (double)Math.sqrt(Math.pow(rotDir[0], 2) + Math.pow(rotDir[1], 2));
		rotDir[0] /= dirLength; //normalize
		rotDir[1] /= dirLength;
		
		/*//Get difference of both speed vectors
		double[] diff = {b.speedVector[0] - speedVector[0], b.speedVector[1] - speedVector[0]};
		double diffLength = (double)Math.sqrt(Math.pow(diff[0], 2) + Math.pow(diff[1], 2));
		double rotDiff = diffLength / Config.FRICTION_DIV;
		*/

		
		
		
		

		//It's possible that they overlap. Correct position.
		double overlapplength = b.getRadius() + this.getRadius() - length;
		double masses = this.getMass() + b.getMass();
		this.Position[0] += -normalx * overlapplength * b.getMass() / masses;
		this.Position[1] += -normaly * overlapplength * b.getMass() / masses;
		b.Position[0] += normalx * overlapplength * this.getMass() / masses;
		b.Position[1] += normaly * overlapplength * this.getMass() / masses;
		
		double f = (-(1+Config.EPSILON)*((b.speedVector[0] - this.speedVector[0])*normalx + (b.speedVector[1]-this.speedVector[1])*normaly))/(1/this.getMass() + 1/b.getMass());
		
		//Set the new Speeds according to the impulse conservation law
		this.speedVector[0] -= f/this.getMass() * normalx;
		this.speedVector[1] -= f/this.getMass() * normaly;
		b.speedVector[0] += f/b.getMass() * normalx;
		b.speedVector[1] += f/b.getMass() * normaly;
		
	}
	
	public boolean collides(Ball ball) {
		if(!isGhost && !ball.isGhost){
			double[] connectingVector = new double[2];
			connectingVector[0] = Position[0] - ball.Position[0];
			connectingVector[1] = Position[1] - ball.Position[1];
			//Scalar Product
			double distance = Math.sqrt(Math.pow(connectingVector[0], 2) + Math.pow(connectingVector[1], 2));
			//returns true if the two balls touch
			return distance <= this.Radius + ball.getRadius();
		}else{
			return false;
		}
	}
	
	public boolean collides(int[] pos) {
		double[] connectingVector = new double[2];
		connectingVector[0] = Position[0] - pos[0];
		connectingVector[1] = Position[1] - pos[1];
		//Scalar Product
		double distance = Math.sqrt(Math.pow(connectingVector[0], 2) + Math.pow(connectingVector[1], 2));
		//returns true if the two balls touch
		return distance <= 2*this.Radius;
	}
	
	public Player getPlayer(){
		return this.player.clone();
	}

	public void updatePoints(){
		view.updatePoints(this.Points);
	}
	public void activatePowerUp(int id) {
		
		for(PowerUp pu : PowerUps){
			if(pu.ID == id){
				pu.run(this);
				PowerUps.remove(pu);
				break;
			}
		}
	}
	public void removeActivePowerUp(PowerUp p){
		p.onDeactivate(this);
		activePowerUps.remove(p);
		if(activePowerUps.size() == 0){
			view.imageLocked = false;
			view.updatePoints(Points);
		}else{
			int t = 0;
			for(PowerUp pu : activePowerUps){
				if(pu.ActiveTime > t){
					view.upadateImage(pu.activeResourceID);
					t = pu.ActiveTime;
				}
			}
		}
	}

	public void addActivePowerUp(PowerUp p) {
		activePowerUps.add(p);
		if(p.activeResourceID >= 0){
			view.imageLocked = true;
			view.upadateImage(p.activeResourceID);
		}
	}
}
