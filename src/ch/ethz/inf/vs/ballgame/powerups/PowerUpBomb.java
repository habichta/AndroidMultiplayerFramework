package ch.ethz.inf.vs.ballgame.powerups;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import ch.ethz.inf.vs.ballgame.Ball;
import ch.ethz.inf.vs.ballgame.R;
import ch.ethz.inf.vs.ballgame.powerups.PowerUp;
import ch.ethz.inf.vs.ballgame.HostActivity;

//Simulates an explosen of a bomb. All other tokens inside the EffectRadius slide away from player token.
//TODO add image and test

public class PowerUpBomb extends PowerUp{
	
	//The Radius in that the bomb has effect on other tokens relative to the screenHeight
	double EffectRadiusFactor = 0.6;
	double effectRadius;
	
	//The strength of the explosion. Affects how the speedVectors increase
	int Strength = 25;
	
	private MediaPlayer explosionSound; 
	Drawable bombDraw;
	ImageView bombImgView;
	protected FrameLayout.LayoutParams bombParams;
	
	Drawable explosionDraw;
	Handler handler = new Handler();
	
	public PowerUpBomb(HostActivity act, int[] pos, int rad) {
		super(act, pos, rad);
		effectRadius = gc.Map.getScreenHeight() * EffectRadiusFactor;
		explosionSound  = MediaPlayer.create(act, R.raw.bomb);
		bombImgView = new ImageView(a);
		bombDraw = bombImgView.getResources().getDrawable(R.drawable.bomb); //TODO replace with bomb Image
		explosionDraw = bombImgView.getResources().getDrawable(R.drawable.explosion); //TODO replace with explosion image
	}

	@Override
	void setID() {
		ID = 4;
		
	}

	public void removeExplosion(){
		 bombParams.height /= 3;
		 bombParams.width /= 3;
		 a.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					bombImgView.setVisibility(View.GONE);
				}
	       });
			 
	}
	
	
	public void explode(double[] pos){
		//TODO add explosion
		explosionSound.start();
		//set explosion drawable
		bombParams.height *= 3;
		bombParams.width *= 3;
		bombImgView.setImageDrawable(explosionDraw);
		 
		for(Ball other : gc.Balls){
			double[] connectingVector = new double[2];
			connectingVector[0] = other.Position[0] - pos[0];
			connectingVector[1] = other.Position[1] - pos[1];
			double distance = Math.sqrt(Math.pow(connectingVector[0], 2) + Math.pow(connectingVector[1], 2));
			//double factor = 1 - (Math.min(effectRadius, distance) / effectRadius);
			double factor = 1000000/ Math.pow(distance, 2);
			other.speedVector[0] += (connectingVector[0] / distance) * factor * Strength ;
			other.speedVector[1] += (connectingVector[1] / distance) * factor * Strength;
		}
		
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				removeExplosion();				
			}
		}, 800);
	}
	
	
	
	@Override
	public void action(Ball b) {
		final double[] position = b.Position.clone();
		bombParams = new FrameLayout.LayoutParams(Radius * 2, Radius * 2);
		bombParams.topMargin = (int) (position[1] - Radius);
        bombParams.leftMargin = (int) (position[0] - Radius);
        bombImgView.setImageDrawable(bombDraw);
        a.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				a.addContentView(bombImgView, bombParams);
			}
        });
        
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				explode(position);
			}
			
		},2000);
	}

}