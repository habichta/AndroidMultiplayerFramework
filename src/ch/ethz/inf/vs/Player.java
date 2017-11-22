package ch.ethz.inf.vs;

import java.io.Serializable;


/*
 * This class represents a Player that is connected to the host with a phone. The class should be implemented by the interface
 */
public class Player implements Serializable{
	
	public static final int oFront = 0;
	public static final int oLeft = 1;
	public static final int oRight = 2;
	public static final int oBehind = 3;
	

	private static int idCounter = 0;
	
	private int orientation = -1;
	
	//A unique ID
	private int ID;
	
	//The name that the player chose before connecting to the server
	private String Name;
	
	private int color;
	
	public Player(){
		ID = idCounter;
		idCounter++;
	}
	
	public int getID(){
		return ID;
	}
	
	public boolean equals(Player p){
		return this.ID == p.getID();
	}
	
	public void setName(String n){
		this.Name = n;
	}
	
	public String getName(){
		return this.Name;
	}
	
	public int getOrientation(){
		return this.orientation;
	}
	
	public Player clone(){
		Player res = new Player();
		res.ID = this.ID;
		res.Name = this.Name;
		res.color = this.color;
		res.orientation = this.orientation;
		
		return res;
	}

	/**
	 * @return the color
	 */
	public int getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(int color) {
		this.color = color;
	}

	public void setOrientation(int playerOrientation) {
		this.orientation = playerOrientation;
		
	}


	
}