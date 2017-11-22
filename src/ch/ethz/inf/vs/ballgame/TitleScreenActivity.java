package ch.ethz.inf.vs.ballgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class TitleScreenActivity extends Activity {

	private static int SHOW_TIME = 1200;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_title_screen);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				Intent i = new Intent(TitleScreenActivity.this,StartActivity.class);
				startActivity(i);
				
				
				finish();
			}
			
			
			
			
			
		}, SHOW_TIME);
	
	}

	
	
}
