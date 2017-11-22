package ch.ethz.inf.vs.controllerLib;

import ch.ethz.inf.vs.ballgame.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

public class AbstractSettingActivity extends Activity {

	public static final String PREF_NAME = "PlayerName";
	
	protected SharedPreferences sharedPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		sharedPrefs = getSharedPreferences(getApplicationInfo().name + "_preferences", 0);
	}
	
	public void onSaveClicked(View v){
		EditText nameText = (EditText) findViewById(R.id.nameText);
		String name = nameText.getText().toString();
		sharedPrefs.edit().putString(PREF_NAME, name).apply();
		onBackPressed();
	}
	
}
