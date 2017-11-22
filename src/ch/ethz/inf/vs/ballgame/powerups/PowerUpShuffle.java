package ch.ethz.inf.vs.ballgame.powerups;

import ch.ethz.inf.vs.ballgame.Ball;
import ch.ethz.inf.vs.ballgame.HostActivity;

//Shuffles the positions and speedVectors of all balls
//TODO add image and test

public class PowerUpShuffle extends PowerUp{

	public PowerUpShuffle(HostActivity act, int[] pos, int rad) {
		super(act, pos, rad);
	}

	@Override
	void setID() {
		ID = 5;
	}

	@Override
	public void action(Ball b) {
		double[] oldPos, oldSpeed;
		double[] newPos = gc.Balls.get(0).Position;
		double[] newSpeed = gc.Balls.get(0).speedVector;
		for(int i = 1; i < gc.Balls.size(); i++){
			oldPos = gc.Balls.get(i).Position;
			oldSpeed = gc.Balls.get(i).speedVector;
			gc.Balls.get(i).setPosition(newPos);
			gc.Balls.get(i).setSpeed(newSpeed);
			newPos = oldPos;
			newSpeed = oldSpeed;
		}
		gc.Balls.get(0).setPosition(newPos);
		gc.Balls.get(0).setSpeed(newSpeed);
	}
	
}
