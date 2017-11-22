package ch.ethz.inf.vs.ballgame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import ch.ethz.inf.vs.Player;
import ch.ethz.inf.vs.controllerLib.AbstractStartActivity;
import ch.ethz.inf.vs.controllerLib.ControllerMessage;
import ch.ethz.inf.vs.controllerLib.AbstractSettingActivity;

public class StartActivity extends AbstractStartActivity<Serializable, Player>{

	private int REQUEST_ENABLE_BT = 12;
	
	public DesignedArrayAdapter hostListAdapter;
	public List<String> hostList;
	private ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_startactivity);
		

		setLobbyActivity(LobbyActivity.class,StartGameActivity.class);
		setSettingActivity(SettingActivity.class);

		
		hostList = new ArrayList<String>();
		hostListAdapter = new DesignedArrayAdapter(this, hostList);
		listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(hostListAdapter);
		listView.setOnItemClickListener(new OnItemClickListener () {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// on click on an item in the listView
				connectToHost(position);
			}
			
		});
		ViewGroup v = (ViewGroup) findViewById(R.id.start_layout);
		v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

	        @Override
	        public void onGlobalLayout() {
	        	ViewGroup v = (ViewGroup) findViewById(R.id.start_layout);
	            // Ensure you call it only once :
	            v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
	            
	    		for(int i = 0; i < v.getChildCount(); i++){
	    			if(v.getChildAt(i) instanceof Button){
	    				Button b = (Button) v.getChildAt(i);
	    				Typeface type = Typeface.createFromAsset(getAssets(),"fonts/grilled_cheese.ttf"); 
	    				b.setTypeface(type);
	    				
	    				//Scale Background
	    				Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.textbg);
	    				Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, b.getWidth(), b.getHeight(), true);
	    				b.setBackground(new BitmapDrawable(v.getResources(), scaledBitmap));
	    			}
	    		}
	        }
	    });
		
		
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		super.onWindowFocusChanged(hasFocus);
		//Set fonts
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT){
			if (resultCode == RESULT_CANCELED){
				Toast.makeText(getApplicationContext(), "Bluetooth not enabled", Toast.LENGTH_LONG).show();
				onDiscovryModeChanged(false);
			} else if (resultCode == RESULT_OK){
				onDiscovryModeChanged(true);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	

	@Override
	public UUID getUuid() {
		UUID id = UUID.fromString("35729b50-83da-11e4-b4a9-0800200c9a66");
		return id;
	}

	
	public void onSearchHostsClick(View v){
		onDiscovryModeChanged(!isSearching);
		
	}

	public void onStartHostClick(View v){
		createHost();
	}

	@Override
	public void onHostDiscovered(String host) {
		Toast.makeText(this, "Host Discovered", Toast.LENGTH_SHORT).show();
		hostListAdapter.add(host);
		hostListAdapter.notifyDataSetChanged();
		
	}


	public void onSettingClick(View v){
		settings();
	}

	@Override
	public void onDiscovryModeChanged(boolean searching) {
		Button btn = (Button) findViewById(R.id.button_searchhosts);
		if (!searching){
			Toast.makeText(this, "Stopped Discovery", Toast.LENGTH_SHORT).show();
			stopHostSearch();
			btn.setText(R.string.button_searchhosts);
		} else {
			//start bluetooth
			BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
			if (btAdapter == null) {
				//Device does not support Bluetooth
				Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show();
				onBackPressed();
			}
			if (!btAdapter.isEnabled()){
				Intent enableBT = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBT, REQUEST_ENABLE_BT);
			} else {
				//start seaching
				Toast.makeText(this, "Discovering Hosts", Toast.LENGTH_SHORT).show();
				searchHosts();
				btn.setText(R.string.button_stopsearchhosts);
			}
		}
		
	}


	
}