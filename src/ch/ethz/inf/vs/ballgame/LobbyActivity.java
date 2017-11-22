package ch.ethz.inf.vs.ballgame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import ch.ethz.inf.vs.Player;
import ch.ethz.inf.vs.controllerLib.AbstractLobbyActivity;
import ch.ethz.inf.vs.controllerLib.ControllerMessage;

public class LobbyActivity extends AbstractLobbyActivity<Player, LobbyMessage>{

	private ListView playerListView;
	private List<Player> playerList;
	private ArrayAdapter<Player> playerAdapter;
	
	private Queue<Integer> availableColors;
	
	List<Integer> colors;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		playerListView = (ListView)findViewById(R.id.PlayerListView);
		
		playerList = new ArrayList<Player>();
		playerAdapter = new MyAdapter(this,playerList);
		
		playerListView.setAdapter(playerAdapter);
		
		int[] colorArray = {Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN, Color.WHITE, Color.BLACK,
				CustomColours.purple, //Purple
				CustomColours.orange, //Orange
				CustomColours.brown, //Brown
				CustomColours.grey}; //Grey
		colors = new ArrayList<Integer>();
		for (int c : colorArray){
			colors.add(c);
		}
		availableColors = new LinkedList<Integer>(colors);
		
		setHostActivity(HostActivity.class);
		ViewGroup v = (ViewGroup) findViewById(R.id.LobbyActivityLayout);
		v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

	        @Override
	        public void onGlobalLayout() {
	        	ViewGroup v = (ViewGroup) findViewById(R.id.LobbyActivityLayout);
	            // Ensure you call it only once :
	            v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
	            
	    		for(int i = 0; i < v.getChildCount(); i++){
	    			if(v.getChildAt(i) instanceof Button){
	    				Button b = (Button) v.getChildAt(i);
	    				Typeface type = Typeface.createFromAsset(getAssets(),"fonts/grilled_cheese.ttf"); 
	    				b.setTypeface(type);
	    				
	    				//Scale Background
	    				Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.linebg);
	    				Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, b.getWidth(), b.getHeight(), true);
	    				b.setBackground(new BitmapDrawable(v.getResources(), scaledBitmap));
	    			}
	    		}
	        }
	    });
	}
	

	


	@Override
	public Player createIdentifier() {
		
		return new Player();
	}

	


	@Override
	public Player initializeIdentifier(Player identifier,
			ControllerMessage message) {
		
		//TODO initialize player 
		identifier.setName(message.messageString);
		Integer color = availableColors.poll();
		if (color == null){
			return null;
		}
		identifier.setColor(color);
		return identifier;
	}


	@Override
	public void addIdentifierToList(Player identifier) {
		playerList.add(identifier);
		playerAdapter.notifyDataSetChanged();
		
	}
	public void removeIdentifierFromList(Player identifier){
		
		availableColors.add(identifier.getColor());
		playerList.remove(identifier);
		playerAdapter = new MyAdapter(this,playerList);
		ListView listView = (ListView) findViewById(R.id.PlayerListView);
		listView.setAdapter(playerAdapter);
		
	}





	@Override
	public boolean startRequest() {
		// TODO Auto-generated method stub
		return true;
	}

	
	public boolean changeColor(Player player, int color){
		if (!availableColors.contains(color)){
			//Toast.makeText(this, "failed to change color", Toast.LENGTH_SHORT).show();
			return false;
		}
		availableColors.add(player.getColor());
		player.setColor(color);
		availableColors.remove(color);
		Toast.makeText(this, player.getName() + " changed color", Toast.LENGTH_SHORT).show();
		LobbyMessage message = new LobbyMessage(LobbyMessage.MessageType.CHANGE_COLOR);
		message.color = color;
		sendTo(player, message);
		return true;
	}
	
	
	private class MyAdapter extends ArrayAdapter<Player> {
		private final Activity context;
		private final List<Player> players;
		
		private SpinnerAdapter spinnerAdapter;
		
		public MyAdapter (Activity context, List<Player> values){
			super(context, R.layout.list_element_layout,values);
			this.context = context;
			this.players = values;
			
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
			
			spinnerAdapter = new DesignedSpinnerAdapter(context, spinnerList);
			//spinnerAdapter = new MySpinnerAdapter(context,colors);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null){
				//make new View
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View elemView = inflater.inflate(R.layout.list_element_layout, parent, false);
				TextView textView = (TextView) elemView.findViewById(R.id.ListElemText);
				textView.setText(players.get(position).getName());
				
				//Design
			    Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/grilled_cheese.ttf"); 
				textView.setTypeface(type);
				textView.setTextSize(30);
				textView.setPadding(20, 10, 20, 10);
				textView.setTextColor(Color.rgb(9, 39, 63));
				//Scale Background
				Display display = context.getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int width = size.x;
				Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.linebg);
				Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width * 3/4, 80, true);
				textView.setBackground(new BitmapDrawable(context.getResources(), scaledBitmap));
				
				Spinner spinner = (Spinner) elemView.findViewById(R.id.spinner);
				spinner.setAdapter(spinnerAdapter);
				final int playerPos = position;
				spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						int color = colors.get(position);
						//int color = (int) parent.getItemAtPosition(position);
						if (changeColor(playerList.get(playerPos), color)) {
							//color changing worked
						} else {
							//color changing failed
							Spinner spinner = (Spinner) parent.findViewById(R.id.spinner);
							int tmp2 = players.get(playerPos).getColor();
							int tmp = colors.indexOf(tmp2);
							spinner.setSelection(tmp);
						}
						
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						//do nothing
						
					}
					
				});
				spinner.setSelection(colors.indexOf(players.get(position).getColor()));
				return elemView;
			} else {
				//use oldView
				return convertView;
			}
		}
		/*
		private class MySpinnerAdapter extends ArrayAdapter<Integer>{
			
			private final Context context;
			private final List<Integer> values;
			
			public MySpinnerAdapter (Context context, List<Integer> values){
				super(context, R.layout.spinner_element_layout,values);
				this.context = context;
				this.values = values;
			}
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null){
					//make new View
					LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
					View elemView = inflater.inflate(R.layout.spinner_element_layout, parent, false);
					ImageView image = (ImageView) elemView.findViewById(R.id.imageView1);
					//image.setBackgroundColor(values.get(position));
					return elemView;
				} else {
					//use old View
					ImageView image = (ImageView) convertView.findViewById(R.id.imageView1);
					//image.setBackgroundColor(values.get(position));
					return convertView;
				}
			}
			
		}*/
	}




	@Override
	public void onReceive(Player p, LobbyMessage m) {
		switch (m.messageType){
		case CHANGE_COLOR:
			break;
		case COLOR_CHANGE_REQUEST:
			changeColor(p,m.color);
			playerAdapter = new MyAdapter(this,playerList);
			ListView listView = (ListView) findViewById(R.id.PlayerListView);
			listView.setAdapter(playerAdapter);
			break;
		default:
			break;
		
		}
		
		
	}


	public void onVisibilityChanged(boolean visible){
		Button setVisibleBtn = (Button) findViewById(R.id.btn_setVisible);
		if (visible){
			setVisibleBtn.setVisibility(Button.GONE);
		} else {
			setVisibleBtn.setVisibility(Button.VISIBLE);
		}
	}
	
	public void setVisibleClick(View v){
		setVisible();
	}	

	

}
