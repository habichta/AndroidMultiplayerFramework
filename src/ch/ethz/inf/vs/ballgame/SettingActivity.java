package ch.ethz.inf.vs.ballgame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import ch.ethz.inf.vs.controllerLib.AbstractSettingActivity;

public class SettingActivity extends AbstractSettingActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		String name = sharedPrefs.getString(PREF_NAME, "Unknown Player");
		EditText nameText = (EditText) findViewById(R.id.nameText);
		nameText.setText(name);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.SettingActivityLayout);
		v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

	        @Override
	        public void onGlobalLayout() {
	        	ViewGroup v = (ViewGroup) findViewById(R.id.SettingActivityLayout);
	            // Ensure you call it only once :
	            v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
	            
	            Typeface type = Typeface.createFromAsset(getAssets(),"fonts/grilled_cheese.ttf"); 
	            
	            TextView tv = (TextView) v.findViewById(R.id.NameLabel);
	            tv.setTypeface(type);
	            
				
				Button saveButton = (Button) v.findViewById(R.id.saveButton);
				saveButton.setTypeface(type);
				Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.textbg);
				Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, saveButton.getWidth(), saveButton.getHeight() + 20, true);
				saveButton.setBackground(new BitmapDrawable(v.getResources(), scaledBitmap));
				
				LinearLayout l = (LinearLayout) findViewById(R.id.edit_layout);
				scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, l.getWidth(), l.getHeight() + 20, true);
				l.setBackground(new BitmapDrawable(v.getResources(), scaledBitmap));
	            
	        }
	    });
	}
}
