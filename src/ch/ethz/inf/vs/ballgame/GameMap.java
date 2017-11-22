package ch.ethz.inf.vs.ballgame;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.ballgame.powerups.PowerUp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/*
 * A represantation of the logical (no graphics) map of the game. It is always finite and can have powerups, holes and barricades
 */

public class GameMap {
	List<PowerUp> PowerUps;
	
	private int Width;
	private int Height;
	private int TopMargin;
	private int LeftMargin;
	public MapView view;
	private HostActivity a;
	private Point size;
	//The Radius of the ball
	public int BallRadius;
	
	public GameMap(HostActivity act, double relativeRadius){
		a = act;
		PowerUps = new ArrayList<PowerUp>();
		
		//Default Height and Width
		WindowManager wm = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		size = new Point();
		display.getSize(size);
		
		//Calculate margin of the map (Equal to the Radius of a ball)
		this.BallRadius = (int) (size.x * relativeRadius);
		LeftMargin = BallRadius;
		TopMargin = BallRadius;
		
		//Calculate the size of the map
		Width = size.x - 2*LeftMargin;
		Height = size.y - 2*TopMargin;
		
		//Add Background
		ImageView bg = new ImageView(a);
		bg.setImageDrawable(bg.getResources().getDrawable(R.drawable.mapbg));
		bg.setScaleType(ScaleType.FIT_XY);
		a.setContentView(bg);
		
		//view = new MapView(a, this);
	}
	public int getTopMargin(){
		return this.TopMargin;
	}
	public void setTopMargin(int m){
		this.TopMargin = m;
	}
	public int getLeftMargin(){
		return this.LeftMargin;
	}
	public void setLeftMargin(int m){
		this.LeftMargin = m;
	}
	public void setWidth(int w){
		this.Width = w;
	}
	public int getWidth(){
		return this.Width;
	}
	public int getScreenWidth(){
		return size.x;
	}
	public int getScreenHeight(){
		return size.y;
	}
	public void setHeight(int h){
		this.Height = h;
	}
	public int getHeight(){
		return this.Height;
	}
	public void addPowerUp(PowerUp pu){
		this.PowerUps.add(pu);
	}
	
	public void removePowerUp(PowerUp pu){
		this.PowerUps.remove(pu);
	}
	
	
}
