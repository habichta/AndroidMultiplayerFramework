package ch.ethz.inf.vs.controllerLib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DeviceData implements SensorEventListener{


	/** The purpose of this class is to provide easy access to the device's internal data
	 * such as sensors.
	 * @author Arthur Habicht
	 */
	private SensorManager sensManager;
	private List<Sensor> sensors;

	private HashMap<Sensor, float[]> sensorToData;
	
	public DeviceData(Context context, List<Sensor> sensors,float results[]){
		
		this.sensors = sensors;
		sensManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		sensorToData = new HashMap<Sensor,float[]>();
		
	}
	
	
	/**
	 * Registers SensorEventListener. Call this before trying to fetch data.
	 */
	
	public void startReceivingDeviceData(){
		for(Sensor s: sensors){
		sensManager.registerListener(this,s,SensorManager.SENSOR_DELAY_GAME);	
		}
	}
	
	
	/**
	 * Deregisters SensorEventListener. Call this whenever possible to
	 * reduce energy consumption.
	 */
	public void stopReceivingDeviceData(){
		sensManager.unregisterListener(this);
	}
	
	
	@Override
	public void onSensorChanged(SensorEvent event) {

		
			sensorToData.put(event.sensor, event.values);
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	

	public float[] getSensorData(Sensor sensor){
		
		return sensorToData.get(sensor);
		
	}
	
	
	
	
}
