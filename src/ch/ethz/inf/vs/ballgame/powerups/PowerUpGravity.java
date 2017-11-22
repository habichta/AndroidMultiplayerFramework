package ch.ethz.inf.vs.ballgame.powerups;

import ch.ethz.inf.vs.ballgame.Ball;
import ch.ethz.inf.vs.ballgame.Config;
import ch.ethz.inf.vs.ballgame.HostActivity;
import ch.ethz.inf.vs.ballgame.R;

/*
 * Simulates a gravity out of the center (as if it would be a mountain) for 10 seconds
 */
public class PowerUpGravity extends PowerUp{

	double Strength = 200;
	
	public PowerUpGravity(HostActivity act, int[] pos, int rad) {
		super(act, pos, rad);
		setActiveTime(10000);
		setActiveResource(R.drawable.tokengravity);
	}

	@Override
	void setID() {
		ID = 8;
	}

	@Override
	public void onGo(Ball b){
		
		//calculate vector out of middle
		double[] v = new double[2];
		v[0] = b.Position[0] - gc.Map.getScreenWidth() / 2;
		v[1] = b.Position[1] - gc.Map.getScreenHeight() / 2;
		
		//Normalize
		double l = Math.sqrt(Math.pow(v[0], 2) + Math.pow(v[1], 2));
		v[0] = v[0] / l;
		v[1] = v[1] / l;
		
		b.accVector[0] += v[0] * Strength;
		b.accVector[1] += v[1] * Strength;
		
		//max acceleration
		double maxAcc = 10*Config.ACCELERATION_SCALE;
		double acc = Math.sqrt(Math.pow(b.accVector[0], 2) + Math.pow(b.accVector[1], 2));
		if(acc > maxAcc){
			b.accVector[0] /= acc / maxAcc;
			b.accVector[1] /= acc / maxAcc;
		}
		
	}
	
	@Override
	public void action(Ball b) {
		for(Ball ball : gc.Balls) ball.addActivePowerUp(this);
	}

}
