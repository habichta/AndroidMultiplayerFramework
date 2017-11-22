package ch.ethz.inf.vs.ballgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.ImageView;

/*
 * The graphical represantation of a Ball
 */
public class BallView extends View{
	private Paint paint;
	private int color;
	private Ball ball;
	private HostActivity a;
	private ImageView image;
	private Matrix matrix;
	public boolean imageLocked = false;
	
	//The images for the token with the points
	int[] tokens = {
			R.drawable.token0,
			R.drawable.token1,
			R.drawable.token2,
			R.drawable.token3,
			R.drawable.token4,
			R.drawable.token5,
			R.drawable.token6,
			R.drawable.token7,
			R.drawable.token8,
			R.drawable.token9,
			R.drawable.token10,
			R.drawable.token11,
			R.drawable.token12,
			R.drawable.token13,
			R.drawable.token14,
			R.drawable.token15,
			R.drawable.token16,
			R.drawable.token17,
			R.drawable.token18,
			R.drawable.token19,
			R.drawable.token20,
			R.drawable.token21,
			R.drawable.token22,
			R.drawable.token23,
			R.drawable.token24,
			R.drawable.token25,
			R.drawable.token26,
			R.drawable.token27,
			R.drawable.token28,
			R.drawable.token29,
			R.drawable.token30,
			R.drawable.token31,
			R.drawable.token32,
			R.drawable.token33,
			R.drawable.token34,
			R.drawable.token35,
			R.drawable.token36,
			R.drawable.token37,
			R.drawable.token38,
			R.drawable.token39,
			R.drawable.token40,
			R.drawable.token41,
			R.drawable.token42,
			R.drawable.token43,
			R.drawable.token44,
			R.drawable.token45,
			R.drawable.token46,
			R.drawable.token47,
			R.drawable.token48,
			R.drawable.token49,
			R.drawable.token50,
			R.drawable.token51,
			R.drawable.token52,
			R.drawable.token53,
			R.drawable.token54,
			R.drawable.token55,
			R.drawable.token56,
			R.drawable.token57,
			R.drawable.token58,
			R.drawable.token59,
			R.drawable.token60,
			R.drawable.token61,
			R.drawable.token62,
			R.drawable.token63,
			R.drawable.token64,
			R.drawable.token65,
			R.drawable.token66,
			R.drawable.token67,
			R.drawable.token68,
			R.drawable.token69,
			R.drawable.token70,
			R.drawable.token71,
			R.drawable.token72,
			R.drawable.token73,
			R.drawable.token74,
			R.drawable.token75,
			R.drawable.token76,
			R.drawable.token77,
			R.drawable.token78,
			R.drawable.token79,
			R.drawable.token80,
			R.drawable.token81,
			R.drawable.token82,
			R.drawable.token83,
			R.drawable.token84,
			R.drawable.token85,
			R.drawable.token86,
			R.drawable.token87,
			R.drawable.token88,
			R.drawable.token89,
			R.drawable.token90,
			R.drawable.token91,
			R.drawable.token92,
			R.drawable.token93,
			R.drawable.token94,
			R.drawable.token95,
			R.drawable.token96,
			R.drawable.token97,
			R.drawable.token98,
			R.drawable.token99,
			R.drawable.token100
	};
	
	public BallView(HostActivity act, Ball b, int c){
		super(act);
		this.a = act;
		paint = new Paint();
		color = c; //Default Color
		ball = b;

		image = new ImageView(a); 
		int p = (b.Points <= 99)?b.Points:100;
		image.setImageDrawable(image.getResources().getDrawable(tokens[p]));
		GradientDrawable bgCyrcle = (GradientDrawable) image.getResources().getDrawable(R.drawable.token_shape);
		bgCyrcle.setColor(color);
		bgCyrcle.setCornerRadius(b.getRadius());
		image.setBackground(bgCyrcle);
		
		//Uncomment for rotation support
		//matrix = new Matrix();
		//matrix.postScale((ball.getRadius() * 2f) / width, (ball.getRadius() * 2f) / height);
		//image.setScaleType(ScaleType.MATRIX);   //required
		//image.setImageMatrix(matrix);
		
        a.addContentView(image, b.params);
	}

	public void updatePoints(final int points){
		a.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				if(!imageLocked){
					int p = (points <= 99) ? points : 100;
					image.setImageDrawable(image.getResources().getDrawable(tokens[p]));
				}
			}
			
		});
	}
	
	public void upadateImage(final int id){
		a.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				image.setImageDrawable(image.getResources().getDrawable(id));
			}
		
		});
	}
	
	public void rotate(float angle){
		/*uncomment for rotation */
		//matrix.postRotate(angle, (float)(ball.getRadius()), (float)(ball.getRadius()));
		//image.setImageMatrix(matrix);
	}

}
