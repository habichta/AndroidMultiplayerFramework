package ch.ethz.inf.vs.ballgame;

import java.io.Serializable;
import java.util.Arrays;

public class Transmission implements Serializable{
	
	private static final long serialVersionUID = 7217741481510883976L;

	public static enum MessageType{
		UPDATE,
		PAUSE_REQUEST,
		RESUME_REQUEST,
		LEAVE_REQUEST;
	}
	
	private Boolean newRound;
	private Integer rank;
	private Boolean gameover;
	private Boolean dead;
	private Integer powerup;
	private Integer vibrate;
	private Integer points;
	private double[] acceleration;
	private int senderID;
	private MessageType type;
	
	public Transmission(){
		this.type = MessageType.UPDATE;
		this.senderID = -1;
	}
	
	public Transmission(int playerID){
		this.type = MessageType.UPDATE;
		this.senderID = playerID;
	}
	
	public Transmission(MessageType type){
		this.type = type;
		this.senderID = -1;
	}
	
	public Transmission(MessageType type, int fromID){
		this.type = type;
		this.senderID = fromID;
	}

	public Boolean getNewRound() {
		return newRound;
	}

	public void setNewRound(Boolean newRound) {
		this.newRound = newRound;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public Boolean getGameover() {
		return gameover;
	}

	public void setGameover(Boolean gameover) {
		this.gameover = gameover;
	}

	public Boolean getDead() {
		return dead;
	}

	public void setDead(Boolean dead) {
		this.dead = dead;
	}

	public Integer getPowerup() {
		return powerup;
	}

	public void setPowerup(Integer powerup) {
		this.powerup = powerup;
	}

	public Integer getVibrate() {
		return vibrate;
	}

	public void setVibrate(Integer vibrate) {
		this.vibrate = vibrate;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public double[] getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(double[] acceleration) {
		this.acceleration = acceleration;
	}

	public int getPlayerID() {
		return senderID;
	}

	public void setPlayerID(int id) {
		this.senderID = id;
	}
	
	public MessageType getType(){
		return this.type;
	}
	
	public void setType(MessageType type){
		this.type = type;
	}

	//checking requirements given by http://www.onjava.com/pub/a/onjava/excerpt/JavaRMI_10/index.html?page=3
	//Required for Serializable I - Make sure that instance-level, locally defined state is serialized properly.
	/*Note to self:
		 Boolean implements Serializable
		 Integer implements Serializable
		 double is primitive (i.e. serializable) && arrays of serializable classes are serializable objects
		 String implements Serializable
		 => all instance-level locally defined variables are either serializable objects or primitive types
		 => no further effort required
	 */

	//Required for Serializable II - Make sure that superclass state is serialized properly.
	/*
	 Superclass: Object => no state to secure
	 => no further effort required
	 */

	//Required for Serializable III - Override equals( )and hashCode( ).
	/*
	 Default implementation (java.lang.Object): hashCode() = object's memory location
	 											equals() = this.hashCode() == other.hashCode()
	 											
	 Objects that are equal must have same hashCode.
	 */
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Transmission))
			return false;
		Transmission other = (Transmission)o;
		if(notEqual(senderID, other.senderID)) return false;
		if(notEqual(acceleration, other.acceleration)) return false;
		if(notEqual(points, other.points)) return false;
		if(notEqual(vibrate, other.vibrate)) return false;
		if(notEqual(powerup, other.powerup)) return false;
		if(notEqual(dead, other.dead)) return false;
		if(notEqual(gameover, other.gameover)) return false;
		if(notEqual(rank, other.rank)) return false;
		if(notEqual(newRound, other.newRound)) return false;
		
		return true;
	}
	
	private boolean notEqual(Object one, Object another){
		if(one == null && another == null) return false;
		if(another == null) return true;
		if(one == null) return true;
		return !one.equals(another);
	}

	@Override
	//following guidelines on http://developer.android.com/reference/java/lang/Object.html to write "a canonical hashCode method"
	public int hashCode() {
		//Start with a non-zero constant.
		int res = 17;
			
		//Include a hash for each field.
		res = 31 * res + (Arrays.asList(MessageType.values())).indexOf(type);
		
		res = 31 * res + (senderID < 0 ? 0 : 1 + senderID);
		
		if(acceleration ==null){
			res *= 31;
		}else{
			long d1, d2;
			d1 = Double.doubleToLongBits(acceleration[0]);
			d2 = Double.doubleToLongBits(acceleration[1]);
			
			res = 31 * res + (int)(d1 ^ (d1 >>> 32));
			res = 31 * res + (int)(d2 ^ (d2 >>> 32));
		}
		
		res = 31 * res + (vibrate == null ? 0 : vibrate);
		
		res = 31 * res + (powerup == null ? 0 : powerup);
		
		res = 31 * res + (dead == null ? 0 : (dead ? 1 : 2));
		
		res = 31 * res + (gameover == null ? 0 : (gameover ? 1 : 2));
		
		res = 31 * res + (rank == null ? 0 : rank);
		
		res = 31 * res + (newRound == null ? 0 : (newRound ? 1 : 2));		

		return res;
	}

}
