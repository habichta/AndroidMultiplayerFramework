package ch.ethz.inf.vs.ballgame;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

import ch.ethz.inf.vs.Player;
import ch.ethz.inf.vs.controllerLib.AbstractStartGameActivity;

public class StartGameActivity extends AbstractStartGameActivity<Player, LobbyMessage> {
	
	private List<Integer> colors;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_abstract_start_game);
		int color = getIdentifier().getColor();
		setColor(color);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setPlayerActivity(PlayerActivity.class);

		//Add Background Image
		/*ImageView bg = new ImageView(this);
		bg.setImageDrawable(bg.getResources().getDrawable(R.drawable.playerbg));
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addContentView(bg, params);*/
		
		final int[] colorArray = {Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN, Color.WHITE, Color.BLACK,
				CustomColours.purple, //Purple
				CustomColours.orange, //Orange
				CustomColours.brown, //Brown
				CustomColours.grey}; //Grey
		colors = new ArrayList<Integer>();
		for (int c : colorArray){
			colors.add(c);
		}
		Spinner colorSpinner = (Spinner) findViewById(R.id.controller_spinner);
		List<String> spinnerList = new ArrayList<String>();
		spinnerList.add("Blue");
		spinnerList.add("Red");
		spinnerList.add("Green");
		spinnerList.add("Magenta");
		spinnerList.add("Yellow");
		spinnerList.add("Cyan");
		spinnerList.add("White");
		spinnerList.add("Black");
		spinnerList.add("Purple");
		spinnerList.add("Orange");
		spinnerList.add("Brown");
		spinnerList.add("Grey");
		DesignedSpinnerAdapter colorAdapter = new DesignedSpinnerAdapter(this, spinnerList);
		colorSpinner.setAdapter(colorAdapter);
		colorSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				int color = colorArray[position];
				if (color != getIdentifier().getColor()){
					LobbyMessage message = new LobbyMessage(LobbyMessage.MessageType.COLOR_CHANGE_REQUEST);
					message.color = color;
					send(message);
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		colorSpinner.setSelection(colors.indexOf(getIdentifier().getColor()));
		
		ViewGroup v = (ViewGroup) findViewById(R.id.StartGameActivityLayout);
		v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

	        @Override
	        public void onGlobalLayout() {
	        	ViewGroup v = (ViewGroup) findViewById(R.id.StartGameActivityLayout);
	            // Ensure you call it only once :
	            v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
	            
	            Typeface type = Typeface.createFromAsset(getAssets(),"fonts/grilled_cheese.ttf"); 
	            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.linebg);
	            
	            Button startButton = (Button) v.findViewById(R.id.start_button);
	            startButton.setTypeface(type);
				Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, startButton.getWidth(), startButton.getHeight(), true);
				startButton.setBackground(new BitmapDrawable(v.getResources(), scaledBitmap));
				
				Spinner sp = (Spinner) v.findViewById(R.id.controller_spinner);
				originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.textbg);
				scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, sp.getWidth(), sp.getHeight(), true);
				sp.setBackground(new BitmapDrawable(v.getResources(), scaledBitmap));
	        }
	    });
	}
	
	
		public void onStartButtonPressed(View v){
			startGame();
		}
	
	


	@Override
	public void onReceive(LobbyMessage m) {
		if (m.messageType.equals(LobbyMessage.MessageType.CHANGE_COLOR)){
			getIdentifier().setColor(m.color);
			setColor(m.color);
			int index = colors.indexOf(m.color);
			Spinner spinner = (Spinner) findViewById(R.id.controller_spinner);
			spinner.setSelection(index);
		}
		
	}

	private void setColor(final int c){
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				try{
					switch(c){
					
					case Color.BLACK:
						findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbgblack);
						break;
						
					case Color.WHITE:
						findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbgwhite);
						break;
					
					case Color.BLUE:
						findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbgblue);
						break;
						
					case Color.RED:
						findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbgred);
						break;
					
					case Color.YELLOW:
						findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbgyellow);
						break;
						
					case Color.GREEN:
						findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbggreen);
						break;
						
					case Color.CYAN:
						findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbgcyan);
						break;
						
					case Color.MAGENTA:
						findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbgmagenta);
						break;
						
					case Color.GRAY:
						findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbggray);
						break;
					}
					
					if(c == CustomColours.brown) findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbgbrown);
					else if(c == CustomColours.grey) findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbggray);
					else if(c == CustomColours.orange) findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbgorange);
					else if(c == CustomColours.purple) findViewById(R.id.StartGameActivityLayout).setBackgroundResource(R.drawable.playerbgpurple);
				
				}catch (Exception e){
					Log.e("DEBUG", "Failed to change image for token color: " + e.getMessage());
				}
			}
		});
	}
}
