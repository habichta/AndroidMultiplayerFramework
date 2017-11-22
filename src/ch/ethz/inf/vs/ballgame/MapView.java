package ch.ethz.inf.vs.ballgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

/*
 * The graphical representation of a GameMap
 */
public class MapView extends View{
	private Paint paint = new Paint();
	private int color;
	private GameMap gameMap;
	
	//not used anymore, because I add directly an ImageView as Background
	@Deprecated
	public MapView(HostActivity a, GameMap gm){
		super(a);
		gameMap = gm;
		color = Color.WHITE;
		//ImageView image = new ImageView(a); 
		//image.setImageDrawable(image.getResources().getDrawable(R.drawable.icebackground));
		
		//FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		//FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(gameMap.getWidth(), gameMap.getHeight());
		//params.topMargin = gameMap.getTopMargin();
		//params.leftMargin = gameMap.getLeftMargin();
		
		//a.addContentView(image, params);
		
	}
	
	public void setColor(int c){
		this.color = c;
	}
	
	//Draws the map on the screen
	@Override
    public void onDraw(Canvas canvas) {
		//TODO Draw Holes, Barricades from GameMap
        /*paint.setColor(color);
        paint.setStrokeWidth(0);
        canvas.drawRect(gameMap.getLeftMargin(), gameMap.getTopMargin(), gameMap.getWidth() + gameMap.getLeftMargin(), gameMap.getHeight() + gameMap.getTopMargin(), paint);
    	*/
    }
}
