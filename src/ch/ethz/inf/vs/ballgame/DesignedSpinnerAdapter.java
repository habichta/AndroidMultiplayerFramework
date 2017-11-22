package ch.ethz.inf.vs.ballgame;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DesignedSpinnerAdapter extends BaseAdapter {

	private List<String>        objects; // obviously don't use object, use whatever you really want
	private final Activity   context;
	
	public DesignedSpinnerAdapter(Activity context, List<String> objects) {
	    this.context = context;
	    this.objects = objects;
	}
	
	public void add(String s){
		this.objects.add(s);
	}
	
	@Override
	public int getCount() {
	    return objects.size();
	}
	
	@Override
	public Object getItem(int position) {
	    return objects.get(position);
	}
	
	@Override
	public long getItemId(int position) {
	    return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	
	    Object obj = objects.get(position);
	
	    TextView tv = new TextView(context);
	    tv.setText(obj.toString());
	    Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/grilled_cheese.ttf"); 
		tv.setTypeface(type);
		tv.setTextSize(25);
		tv.setPadding(40, 10, 20, 10);
		tv.setTextColor(Color.rgb(9, 39, 63));
	    return tv;
	}
}
