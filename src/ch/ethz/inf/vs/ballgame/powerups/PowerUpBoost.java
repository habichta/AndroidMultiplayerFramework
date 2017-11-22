package ch.ethz.inf.vs.ballgame.powerups;

import ch.ethz.inf.vs.ballgame.Ball;
import ch.ethz.inf.vs.ballgame.HostActivity;
import ch.ethz.inf.vs.ballgame.R;

public class PowerUpBoost extends PowerUp{

	public PowerUpBoost(HostActivity act, int[] pos, int rad) {
		super(act, pos, rad);
	}

	@Override
	void setID() {
		ID = 3;
		
	}

	@Override
	public void action(Ball b) {
		b.speedVector[0] *= 2;
		b.speedVector[1] *= 2;
	}

}
