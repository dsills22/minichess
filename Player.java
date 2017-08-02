package mc; 
import java.util.concurrent.*;
import java.util.*;

public class Player {
	public static boolean ABTestMode = false;
	public byte depth = 1;
	private boolean useID = true;
	private boolean useTimeMgmt = false;
	public Negamax negamax = new Negamax();

	public Player() {}
	public Player(boolean useAB, boolean useID) {
		negamax.useAB = useAB;
		this.useID = useID;
	}
	public Player(boolean useAB) {
		negamax.useAB = useAB;
	}
	public Player(byte depth, boolean useAB, boolean useID) {
		this.depth = depth;
		negamax.useAB = useAB;
		this.useID = useID;
	}
	public Player(byte depth, boolean useAB, boolean useID, boolean useTT, boolean useTimeMgmt) {
		this.depth = depth;
		negamax.useAB = useAB;
		negamax.useTT = useTT;
		this.useID = useID;
		this.useTimeMgmt = useTimeMgmt;
	}

	public Move getBestScoredMove(State state, int timeLimitMS) {
		negamax.initNegamaxState();
		if(useID) {
			if(useTimeMgmt) {
				timeLimitMS = getTimeLimitMs(state.moveCounter);
			}
			negamax.iterativeDeepening(state, timeLimitMS);
			this.depth = negamax.bestDepthSoFar;
			return negamax.bestMoveSoFar;
		} else {
			try {
				Score score = null;
				if(!ABTestMode) {
					score = negamax.depthSearch(state, depth, depth, Score.Loss(), Score.Win());
				} else {
					negamax.useAB = false;
					score = negamax.depthSearch(state, depth, depth, Score.Loss(), Score.Win());
					System.out.println("NO AB: " + negamax.bestScore);
					negamax.useAB = true;
					score = negamax.depthSearch(state, depth, depth, Score.Loss(), Score.Win());
					System.out.println("WITH AB: " + negamax.bestScore);
					System.out.println();
				}
				//System.out.println("MOVE: " + negamax.bestMove);
				return negamax.bestMove;
			} catch(EscapeRecursionException e) {
				//System.out.println("OUT OF TIME IN PLAYER");
				return null;
			}
		}
	}

	public int getTimeLimitMs(int moveCounter) {
		int move = moveCounter / 2;
		if(move <= 1) {
			return 100;
		} else if(move <= 3) {
			return 500;
		} else if(move <= 4) {
			return 6500;
		} else if(move <= 12) {
			return 20000;
		} else if(move <= 29) {
			return 6500;
		} else if(move <= 30) {
			return 2000;
		} else if(move <= 32) {
			return 500;
		} else {
			return 100;
		}
	}
}

class MonitorRunnable implements Runnable {
	private int sleep = 0;
	private Negamax negamax;
	public MonitorRunnable(int sleep, Negamax negamax) {
		this.sleep = sleep;
		this.negamax = negamax;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(this.sleep);
			negamax.isSearchInterrupted = true;
		} catch (Exception e) {}
	}
}