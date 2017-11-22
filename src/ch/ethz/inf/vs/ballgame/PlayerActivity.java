package ch.ethz.inf.vs.ballgame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import ch.ethz.inf.vs.Player;
import ch.ethz.inf.vs.ballgame.Transmission.MessageType;
import ch.ethz.inf.vs.controllerLib.AbstractPlayerActivity;
import ch.ethz.inf.vs.controllerLib.ControllerMessage;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout.LayoutParams;

public class PlayerActivity extends AbstractPlayerActivity<Player, Transmission> implements SensorEventListener{
//by lukas: if you want to send a specific messageType then you can replace Serializable with that type. 
//(this type needs to implement Serializable and should be the same type as in HostActivity)
	private final static String TAG = "game_PlayerActivity";

	private Handler h;

	private Player me;
	private long timestamp;
	
	private boolean dead;
	private boolean go;
	private int rank;
	private int orientation = Player.oFront;

	private float[] initialVector = new float[]{0,0,0};
	private float[] vector = null;
	private Object vectorLock = new Object();

	private TextView debugFwd;
	private TextView debugRght;
	
	private ImageView scoreView;
	
	private ImageButton powBtn1;
	private ImageButton powBtn2;
	private ImageButton powBtn3;
	private ImageButton powBtn4;
	
	private PowerButton pb1;
	private PowerButton pb2;
	private PowerButton pb3;
	private PowerButton pb4;
	
	private PowerPool powerpool;

	private Sensor acc;
	private SensorManager sManager;
	
	private Vibrator vib;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	
		h = new Handler();
		
		dead = false;
		go = false;

		View decorView = getWindow().getDecorView();
		// Hide the status bar.
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		// hide the action bar.
		ActionBar actionBar = getActionBar();
		if(actionBar != null)
			actionBar.hide();
		
		scoreView = (ImageView) findViewById(R.id.playerScore);

		int colour;
		me = this.getIdentifier();
		colour = me.getColor();

		String name;
		name = me.getName();

		TextView welcomeTxt = (TextView)findViewById(R.id.playerGreeting);
		welcomeTxt.setTextColor(colour);
		welcomeTxt.setText("Go, " + name + "!");

		setBG(colour);
		prepareScoreView(colour);

		timestamp = System.currentTimeMillis();

		debugFwd = (TextView)findViewById(R.id.debugForwardDisplay);
		debugRght = (TextView)findViewById(R.id.debugLeftDisplay);

		debugFwd.setVisibility(TextView.INVISIBLE);
		this.debugRght.setVisibility(TextView.INVISIBLE);
		//TODO: comment the above two lines if necessary

		sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		acc = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_FASTEST);

		ImageButton recalBtn = (ImageButton)findViewById(R.id.btnRecalibrate);
		//recalBtn.setBackground(null);
		recalBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				synchronized(vectorLock){
					if(vector != null){
						initialVector = vector;
					}
				}

				h.post(new Runnable(){

					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), "recalibrated", Toast.LENGTH_SHORT).show();
					}

				});
			}
		});
		

		powBtn1 = (ImageButton)findViewById(R.id.btnPowerup1);
		powBtn2 = (ImageButton)findViewById(R.id.btnPowerup2);
		powBtn3 = (ImageButton)findViewById(R.id.btnPowerup3);
		powBtn4 = (ImageButton)findViewById(R.id.btnPowerup4);
		
		powerpool = new PowerPool(new PowerButton[]{
				pb1 = new PowerButton(powBtn1),
				pb2 = new PowerButton(powBtn2),
				pb3 = new PowerButton(powBtn3),
				pb4 = new PowerButton(powBtn4)
		});
		
		powBtn1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {pb1.fire();}
		});
		powBtn2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {pb2.fire();}
		});
		powBtn3.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {pb3.fire();}
		});
		powBtn4.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {pb4.fire();}
		});
		
		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		
		int myOrientation = me.getOrientation();
		
		Spinner spinner = (Spinner)findViewById(R.id.orientation_spinner);
		List<String> list = new ArrayList<String>(Arrays.asList(new String[]{
				"FRONT",
				"LEFT",
				"RIGHT",
				"BEHIND"
		}));
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,R.layout.spinner_orientation_item, list);
		spinner.setAdapter(dataAdapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String entry = arg0.getItemAtPosition(arg2).toString();
				
				if(entry.equals("FRONT")){
					orientation = Player.oFront;
				}else if(entry.equals("LEFT")){
					orientation = Player.oLeft;
				}else if(entry.equals("RIGHT")){
					orientation = Player.oRight;
				}else if(entry.equals("BEHIND")){
					orientation=Player.oBehind;
				}else{
					Log.e(TAG, "invalid item selected: " + entry);
				}
				
				me.setOrientation(orientation);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//do nothing?
			}
			
		});
		
		if(myOrientation == -1){
			myOrientation = 0;
		}else{
			spinner.setSelection(myOrientation);
		}
		
		this.orientation = myOrientation;
	}

	private void prepareScoreView(int colour) {
		try{
			scoreView.setImageResource(getResources().getIdentifier("token", "drawable", getPackageName()));
		}catch(Exception e){ Log.e("DEBUG", "Failed to prepare Score Image: " + e.getMessage()); }
		GradientDrawable bgCircle = (GradientDrawable) scoreView.getResources().getDrawable(R.drawable.token_shape);
		bgCircle.setColor(colour);
		bgCircle.setCornerRadius(300);
		bgCircle.setSize(scoreView.getWidth()-2, scoreView.getHeight() -2);
		scoreView.setBackground(bgCircle);
		
	}

	private void setBG(int colour) {
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.playerActivityLayout);

		//layout.setBackgroundColor(colour);
		
		String bgImage = "bg_player_";

		if (colour == Color.BLUE) {

			bgImage += "blue";

		} else if (colour == Color.RED) {

			bgImage += "red";

		} else if (colour == Color.GREEN) {

			bgImage += "green";

		} else if (colour == Color.MAGENTA) {

			bgImage += "magenta";

		} else if (colour == Color.YELLOW) {

			bgImage += "yellow";

		} else if (colour == Color.CYAN) {

			bgImage += "cyan";

		} else if (colour == Color.WHITE) {

			bgImage += "white";

		} else if (colour == Color.BLACK) {

			bgImage += "black";

		} else if (colour == CustomColours.purple) {

			bgImage += "purple";

		} else if (colour == CustomColours.orange) {

			bgImage += "orange";

		} else if (colour == CustomColours.brown) {

			bgImage += "brown";

		} else if (colour == CustomColours.grey) {

			bgImage += "grey";

		} else {

			Log.e(TAG, "illegal colour");

			bgImage = null;

		}
		if(bgImage == null){
			Log.e(TAG, "no background: " + bgImage);			
			return;
		}
		
		Log.d(TAG, "background: " + bgImage);
		
		try{
			int layerID = getResources().getIdentifier(bgImage, "drawable", getPackageName());
			layout.setBackgroundResource(layerID);
		}catch(Exception e){
			Log.e("DEBUG", "Failed to set Background Resource in PlayerActivity (setBG): " + e.getMessage());
		}

	}

	@Override
	public void onPause(){
		super.onPause();
		sManager.unregisterListener(this);
	}

	@Override
	public void onResume(){
		super.onResume();
		sManager.registerListener(this, acc,SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		//do nothing
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		if(!go || dead)
			return;
		
		Sensor mySensor = arg0.sensor;


		if (mySensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;

		long myTime = System.currentTimeMillis();

		if(myTime - this.timestamp < Config.WAITTIME_SEND)
			return;

		this.timestamp = myTime;

		float accX = arg0.values[0]; //negative: forwards, positive: backwards
		float accY = arg0.values[1]; //negative: left, positive: right
		float accZ = arg0.values[2]; //negative: rotate left, positive: rotate right

		synchronized(this.vectorLock){
			vector = new float[]{accX, accY, 0};

			if(initialVector == null)
				initialVector = vector.clone();
		}


		float accFwd = accX - initialVector[0];
		float accRght = accY - initialVector[1];

		accFwd *= (-1) * Config.ACCELERATION_SCALE;
		accRght *= Config.ACCELERATION_SCALE;

		/*if(accZ < 0){
			accFwd *= -1;
			accRght *= -1;
		}*/
		
		float tmp;
		switch(orientation){
		case(Player.oBehind):
			accFwd *= -1;
			accRght *= -1;
			break;
		case(Player.oLeft):
			tmp = accRght;
			accRght = accFwd;
			accFwd = -tmp;
			break;
		case(Player.oRight):
			tmp = accFwd;
			accFwd = accRght;
			accRght = -tmp;
			break;
		case(Player.oFront):
		default:
		}

		debugFwd.setText("" + accFwd);
		debugRght.setText("" + accRght);

		double[] newAcc = new double[]{accRght, -accFwd};
		Transmission t = new Transmission(me.getID());
		t.setAcceleration(newAcc);
		
		send(t);
	}

	@Override
	public void onDisconnect() {
		super.onDisconnect();
	}



	@Override
	public void onReceive(Transmission t) {
		Transmission.MessageType type;
		if((type = t.getType()) != Transmission.MessageType.UPDATE){
			//TODO: react to exceptional events
			return;
		}
		
		Boolean newRound;
		Integer rank;
		Boolean gameover;
		Boolean dead;
		Integer powerup;
		Integer vibrate;
		Integer points;
		
		if((points = t.getPoints()) != null) updateScore(points);
		if((vibrate = t.getVibrate()) != null) vibrate(vibrate);
		if((powerup = t.getPowerup()) != null) preparePowerup(powerup);
		if((dead = t.getDead()) != null) die(dead);
		if((rank = t.getRank()) != null) setRank(rank);
		if((gameover = t.getGameover()) != null) gameover(gameover);
		if((newRound = t.getNewRound() != null)) startRound(newRound);
		
	}

	private void updateScore(int points){
		
		if(points > 99){
			try{
				scoreView.setImageResource(R.drawable.token100);
			}catch(Exception e){
				Log.e("DEBUG", "Failed to update Score image: " + e.getMessage());
			}
			return;
		}
		String token = "token" + points;
		try{
			scoreView.setImageResource(getResources().getIdentifier(token, "drawable", getPackageName()));
		}catch(Exception e){
			Log.e("DEBUG", "Failed to update Score image: " + e.getMessage());
		}
	}
	
	private void setRank(int rank){
		this.rank = rank;
	}
	
	private void preparePowerup(final int powerup) {
		PowerButton btn = powerpool.request();
		if(btn == null)
			return;
		
		btn.arm(powerup);
		
	}

	private void vibrate(final int duration) {
		vib.vibrate(duration);
	}
	
	private void die(boolean dead){
		this.dead = dead;
	}
	
	private void gameover(boolean over){
		this.go = !over;
		//TODO?: show rank
	}
	
	private void startRound(boolean go){
		if(go){
			//TODO?:hide rank
			//updateScore(0); Removed by Dario (We wan't to keep the points from the last round)
			this.dead = false;
			this.go = true;
		}
	}
	
	@Override
		public void onBackPressed() {
			Transmission msg = new Transmission(Transmission.MessageType.PAUSE_REQUEST);
			send(msg);
			pause();
		}
	
	public void pause() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String[] stringArray = new String[] { "Resume", "Leave" };
		ListView pausedList = new ListView(this);
		ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
		pausedList.setAdapter(modeAdapter);
		builder.setTitle("Game paused");
		builder.setView(pausedList);

		final Dialog dialog = builder.create();

		pausedList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					Transmission msg = new Transmission(Transmission.MessageType.RESUME_REQUEST);
					send(msg);
					dialog.hide();
				} else {
					Transmission msg = new Transmission(Transmission.MessageType.LEAVE_REQUEST);
					send(msg);
				}

			}
		});
		dialog.show();
	}
	
	private class PowerPool{
		private ArrayList<PowerButton> btns;
		private boolean[] available;
		
		public PowerPool(Collection<PowerButton> btns){
			this.btns = new ArrayList<PowerButton>();
			this.btns.addAll(btns);
			available = new boolean[this.btns.size()];
			Arrays.fill(available, true);
		}
		
		public PowerPool(PowerButton[] btns){
			this(Arrays.asList(btns));
		}
		
		public synchronized PowerButton request(){
			for(int i = 0; i < available.length; i++){
				if(available[i]){
					available[i] = false;
					return btns.get(i);
				}
			}
			return null;
		}
		
		public synchronized void release(PowerButton btn){
			int index = btns.indexOf(btn);
			if(index < 0) return;
			
			available[index] = true;
		}
	}
	
	private class PowerButton{
		ImageButton btn;
		int powID;
		
		public PowerButton(ImageButton btn){
			this.btn = btn;
		}
		
		public void arm(int powID){
			this.powID = powID;
			try{
				btn.setImageDrawable(btn.getResources().getDrawable(Config.powIDtoDrawable.get(powID)));
			}catch (Exception e){
				Log.e("DEBUG", "Failed to set image at arm: " + e.getMessage());
			}
			
		}
		
		public void fire(){
			if(dead) return;
			try{
				btn.setImageDrawable(null);
			}catch(Exception e){
				Log.e("DEBUG", "Faild to remove image at fire: " + e.getMessage());
			}
			
			Transmission t = new Transmission(me.getID());
			t.setPowerup(this.powID);
			send(t);
			
			powerpool.release(this);
		}
	}
	
}
