package ch.ethz.inf.vs.ballgame.powerups;

import ch.ethz.inf.vs.ballgame.Ball;
import ch.ethz.inf.vs.ballgame.HostActivity;
import ch.ethz.inf.vs.ballgame.R;

/*
 * The token can't collide with other tokens for 10 seconds
 */
public class PowerUpGhost extends PowerUp{

	public PowerUpGhost(HostActivity act, int[] pos, int rad) {
		super(act, pos, rad);
		setActiveTime(10000);
		setActiveResource(R.drawable.tokenghost);
	}

	@Override
	void setID() {
		ID = 7;	
	}

	@Override
	public void action(Ball b) {
		b.addActivePowerUp(this);
		b.isGhost = true;
	}
	
	@Override
	public void onDeactivate(Ball b){
		b.isGhost = false;
	}

}
