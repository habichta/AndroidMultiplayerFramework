package ch.ethz.inf.vs.ballgame.powerups;

import ch.ethz.inf.vs.ballgame.Ball;
import ch.ethz.inf.vs.ballgame.Config;
import ch.ethz.inf.vs.ballgame.HostActivity;
import ch.ethz.inf.vs.ballgame.R;
import ch.ethz.inf.vs.ballgame.R.drawable;

public class PowerUpStop extends PowerUp {

	public PowerUpStop(HostActivity act, int[] pos, int rad) {
		super(act, pos, rad);
	}
	
	@Override
	void setID() {
		ID = 0;
	}

	@Override
	public void action(Ball b) {
		//Stop the ball
		b.speedVector[0] = 0;
		b.speedVector[1] = 0;
		b.accVector[0] = 0;
		b.accVector[1] = 0;
	}

	

}
