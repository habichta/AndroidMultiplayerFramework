package ch.ethz.inf.vs.ballgame.powerups;

import ch.ethz.inf.vs.ballgame.Ball;
import ch.ethz.inf.vs.ballgame.Config;
import ch.ethz.inf.vs.ballgame.HostActivity;
import ch.ethz.inf.vs.ballgame.R;
import ch.ethz.inf.vs.ballgame.R.drawable;

public class PowerUpMinusOne extends PowerUp{

	public PowerUpMinusOne(HostActivity act, int[] pos, int rad) {
		super(act, pos, rad);
	}

	@Override
	void setID() {
		ID = 1;
		
	}

	@Override
	public void action(Ball b) {
		for(Ball ball : gc.Balls){
			if(ball != b){
				ball.Points = Math.max(0, ball.Points - 1);
				ball.updatePoints();
				ball.sendPoints();
			}
		}
	}

}
