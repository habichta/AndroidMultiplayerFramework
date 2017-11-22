package ch.ethz.inf.vs.ballgame.powerups;

import ch.ethz.inf.vs.ballgame.Ball;
import ch.ethz.inf.vs.ballgame.Config;
import ch.ethz.inf.vs.ballgame.HostActivity;
import ch.ethz.inf.vs.ballgame.R;

public class PowerUpReverse extends PowerUp{

	public PowerUpReverse(HostActivity act, int[] pos, int rad) {
		super(act, pos, rad);
	}

	@Override
	void setID() {
		ID = 2;
		
	}

	@Override
	public void action(Ball b) {
		for(Ball ball : gc.Balls){
			ball.speedVector[0] = -ball.speedVector[0];
			ball.speedVector[1] = -ball.speedVector[1];
		}
	}

}
