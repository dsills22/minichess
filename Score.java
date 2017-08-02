package mc; 

public class Score {
	public boolean isWin = false;
	public boolean isLoss = false;
	public boolean isDraw = false;
	public Move bestMove;
	public byte nodeType = 1;
	public int score = 0;
	public int depth = 1;

	public Score() {}
	public Score(boolean isWin, boolean isLoss, boolean isDraw, int score) {
		this.isWin = isWin;
		this.isLoss = isLoss;
		this.isDraw = isDraw;
		this.score = score;
	}

	public boolean isGreaterThan(Score score) {
		if(this.isEqual(score)) {
			return false;
		}

		if(this.isWin) {
			return true;
		}

		if(score.isWin) {
			return false;
		}

		if(this.isLoss) {
			return false;
		}

		if(score.isLoss) {
			return true;
		}

		if(this.isDraw && score.isWin) {
			return false;
		}

		if(this.isDraw && score.isLoss) {
			return true;
		}

		if(this.score > score.score) {
			return true;
		}

		return false;
	}

	public boolean isTerminal() {
		return this.isWin || this.isLoss || this.isDraw;
	}

	public int toInt() {
		if(this.isWin) {
			return 100000;
		} else if(this.isLoss) {
			return -100000;
		} else {
			return this.score;
		}
	}

	public Score negate() {
		if(this.isWin) {
			this.isWin = false;
			this.isLoss = true;
		} else if(this.isLoss) {
			this.isLoss = false;
			this.isWin = true;
		} else {
			this.score = -1 * this.score;
		}
		return this;
	}

	public Score clone() {
		Score newScore = new Score();
		newScore.bestMove = (bestMove == null ? null : bestMove.clone());
		newScore.isDraw = this.isDraw;
		newScore.isLoss = this.isLoss;
		newScore.isWin = this.isWin;
		newScore.score = this.score;
		newScore.nodeType = this.nodeType;
		newScore.depth = this.depth;
		return newScore;
	}

	public boolean isEqual(Score score) {
		if(this.isWin || this.isLoss || this.isDraw || score.isWin || score.isLoss || score.isDraw) {
			return this.isWin == score.isWin && this.isLoss == score.isLoss && this.isDraw == score.isDraw;
		}
		return this.score == score.score;
	}

	public boolean isLessThan(Score score) {
		return !this.isEqual(score) && !this.isGreaterThan(score);
	}

	public boolean isLessThanEqual(Score score) {
		return this.isEqual(score) || this.isLessThan(score);
	}

	public boolean isGreaterThanEqual(Score score) {
		return this.isEqual(score) || this.isGreaterThan(score);
	}

	public static Score Loss() {
		return new Score(false, true, false, 0);
	}

	public static Score Win() {
		return new Score(true, false, false, 0);
	}

	public static Score Draw() {
		return new Score(false, false, true, 0);
	}

	public static Score Max(Score a, Score b) {
		if(a.isGreaterThan(b)) {
			return a;
		} else {
			return b;
		}
	}

	public static Score Min(Score a, Score b) {
		if(a.isLessThan(b)) {
			return a;
		} else {
			return b;
		}
	}

	@Override
	public String toString() {
		return "bestMove: " + (this.bestMove == null ? "X" : this.bestMove.piece.pieceChar + " -> " + this.bestMove.verbose()) + 
			", score: " + this.score + ", isWin: " + this.isWin + ", isLoss: " + isLoss +
			", isDraw: " + this.isDraw;
	}
}
