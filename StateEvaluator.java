package mc;

public class StateEvaluator {
	public int incrementalWhiteScore = 0;
	public int incrementalBlackScore = 0;
	public boolean whiteHasKing = true;
	public boolean blackHasKing = true;

	public StateEvaluator() {}
	public StateEvaluator(int incrementalWhiteScore, int incrementalBlackScore, boolean whiteHasKing, boolean blackHasKing) {
		this.incrementalWhiteScore = incrementalWhiteScore;
		this.incrementalBlackScore = incrementalBlackScore;
		this.whiteHasKing = whiteHasKing;
		this.blackHasKing = blackHasKing;
	}

	public Score getSideOnMoveScore(State state) {
		if(!whiteHasKing) {
			if(state.isWhiteOnMove) {
				return Score.Loss();
			} else {
				return Score.Win();
			}
		}

		if(!blackHasKing) {
			if(state.isWhiteOnMove) {
				return Score.Win();
			} else {
				return Score.Loss();
			}
		}

		//check for draw last as last move increments counter and we don't want to declare a draw early
		if(state.moveCounter >= State.MAX_MOVES) {
			return Score.Draw();
		}

		return new Score(false, false, false, (state.isWhiteOnMove ? 1 : -1) * (incrementalWhiteScore - incrementalBlackScore));
	}

	public boolean hasWinner() {
		return !blackHasKing || !whiteHasKing;
	}

	public void adjustIncrementalScore(Piece capturedPiece, int dir) { //dir is 1 for doMove, -1 for undoMove
		if(capturedPiece.pieceType != Piece.KING) {
			if(capturedPiece.isWhite) {
				incrementalWhiteScore -= (dir * capturedPiece.nominalValue);
			} else {
				incrementalBlackScore -= (dir * capturedPiece.nominalValue);
			}
		}
	}

	public void adjustIncrementalScore(boolean isWhite, int amount) {
		if(isWhite) {
			incrementalWhiteScore += amount;
		} else {
			incrementalBlackScore += amount;
		}
	}

	public void updateOpponentKing(boolean isWhiteOnMove, boolean hasKing) {
		if(isWhiteOnMove) {
			blackHasKing = hasKing;
		} else {
			whiteHasKing = hasKing;
		}
	} 

	public StateEvaluator clone() {
		return new StateEvaluator(incrementalWhiteScore, incrementalBlackScore, whiteHasKing, blackHasKing);
	}
}
