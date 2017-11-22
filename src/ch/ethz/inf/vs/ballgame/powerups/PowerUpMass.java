package ch.ethz.inf.vs.ballgame.powerups;

import android.os.Handler;
import ch.ethz.inf.vs.ballgame.Ball;
import ch.ethz.inf.vs.ballgame.HostActivity;
import ch.ethz.inf.vs.ballgame.R;

public class PowerUpMass extends PowerUp{

	
	//Mass that is added to the token
	private double changeMass = 1;
	
	public PowerUpMass(HostActivity act, int[] pos, int rad) {
		super(act, pos, rad);
		
		//Active Time
		setActiveTime(10000);
		setActiveResource(R.drawable.tokenmass);
	}

	@Override
	void setID() {
		ID = 6;
		
	}

	@Override
	public void action(Ball b) {
		b.addActivePowerUp(this);
		b.Mass += changeMass;
	}
	
	@Override
	public void onDeactivate(Ball b){
		b.Mass -= changeMass;
	}
}
