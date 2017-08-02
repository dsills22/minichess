package mc;
import java.util.*;

public class State {
	public HashMap<Byte, Piece> blackHash = new HashMap<Byte, Piece>();
	public HashMap<Byte, Piece> whiteHash = new HashMap<Byte, Piece>();
	public HashMap<Byte, Piece> sideOnMoveHash;
	public HashMap<Byte, Piece> otherSideHash;
	public HashMap<Byte, Boolean> emptyHash = new HashMap<Byte, Boolean>();
	public final static byte rowNum = 6;
	public final static byte colNum = 5;
	public final static byte MAX_MOVES = 80;
	public byte moveCounter = 0;
	public boolean isWhiteOnMove = true;
	public final static String sep = System.getProperty("line.separator"); //new-line character for system
	public StateEvaluator stateEvaluator = new StateEvaluator();
	public long incrementalHash = 0L;

	public State(){
		this(true);
	}
	public State(boolean isWhiteOnMove) {
		this((byte) 0, isWhiteOnMove, 
			"kqbnr" + State.sep + 
			"ppppp" + State.sep + 
			"....." + State.sep + 
			"....." + State.sep + 
			"PPPPP" + State.sep + 
			"RNBQK");
	}
	public State(byte moveCounter, boolean isWhiteOnMove, String boardString) {
		this.moveCounter = moveCounter;
		this.isWhiteOnMove = isWhiteOnMove;
		char[][] board = new char[rowNum][colNum]; //form empty board as 2d character array, we only use this to quickly create our lookup tables
		String[] rows = boardString.split(sep); //rows are delimited by new lines (separator)

		//fill 2d char array board
		for(byte i = 0; i < rowNum; i++) {
			board[i] = rows[i].toCharArray();
		}

		//form look up tables, which we'll use in the actual negamax algorithm
		for(byte x = 0; x < rowNum; x++) {
			for(byte y = 0; y < colNum; y++) {
				if(board[x][y] == '.') {
					emptyHash.put(hash(x, y), true);
				} else {
					Piece p = new Piece(board[x][y], x, y);
					if(p.isWhite) {
						whiteHash.put(p.hash, p);
					} else {
						blackHash.put(p.hash, p);
					}
				}
			}
		}

		setSides();
		stateSetup();
	}
	public State(byte moveCounter, boolean isWhiteOnMove, HashMap<Byte, Piece> blackHash, HashMap<Byte, Piece> whiteHash, 
			HashMap<Byte, Boolean> emptyHash, StateEvaluator stateEvaluator, long incrementalHash) {
		this.moveCounter = moveCounter;
		this.isWhiteOnMove = isWhiteOnMove;
		this.blackHash = blackHash;
		this.whiteHash = whiteHash;
		this.emptyHash = emptyHash;
		this.stateEvaluator = stateEvaluator;
		this.incrementalHash = incrementalHash;

		setSides();
		stateSetup();
	}

	private void stateSetup() {
		stateEvaluator.incrementalWhiteScore = getPieceScore(whiteHash);
		stateEvaluator.incrementalBlackScore = getPieceScore(blackHash);

		if(this.incrementalHash == 0L) {
			this.incrementalHash = ZobristHash.boardHash(this);
		}
	}

	public State clone() {
		HashMap<Byte, Piece> blackHashCopy = new HashMap<Byte, Piece>();
		HashMap<Byte, Piece> whiteHashCopy = new HashMap<Byte, Piece>();
		HashMap<Byte, Boolean> emptyHashCopy = new HashMap<Byte, Boolean>();

		for(Byte key : this.blackHash.keySet()) {
			blackHashCopy.put(key, this.blackHash.get(key).clone());
		}
		for(Byte key : this.whiteHash.keySet()) {
			whiteHashCopy.put(key, this.whiteHash.get(key).clone());
		}
		for(Byte key : this.emptyHash.keySet()) {
			emptyHashCopy.put(key, new Boolean(this.emptyHash.get(key)));
		}

		return new State(moveCounter, isWhiteOnMove, blackHashCopy, whiteHashCopy, emptyHashCopy, stateEvaluator.clone(), this.incrementalHash);
	}

	public Piece getPieceAt(byte row, byte col) {
		Piece p = null;
		p = sideOnMoveHash.get(this.hash(row, col));
		if(p != null) {
			return p;
		}

		p = otherSideHash.get(this.hash(row, col));
		if(p != null) {
			return p;
		}

		return null;
	}

	public int getPieceScore(HashMap<Byte, Piece> pieceList) {
		int score = 0;
		for(Piece p : pieceList.values()) {
			if(p.pieceType != Piece.KING) {
				score += p.nominalValue;
			}
		}
		return score;
	}

	public int getSideOnMoveScore() {
		return getPieceScore(sideOnMoveHash) - getPieceScore(otherSideHash);
	}

	public void setSides() {
		if(isWhiteOnMove) {
			sideOnMoveHash = whiteHash;
			otherSideHash = blackHash;
		} else {
			sideOnMoveHash = blackHash;
			otherSideHash = whiteHash;
		}
	}

	public void switchSides() {
		this.incrementalHash ^= ZobristHash.zobristWhite;
		this.incrementalHash ^= ZobristHash.zobristBlack;
		if(isWhiteOnMove) {
			isWhiteOnMove = false;
		} else {
			isWhiteOnMove = true;
		}

		setSides();
	}

	public static byte hash(byte row, byte col) {
		return (byte) (((row + 1) * 16) + col);
	}

	public String printBoard() {
		StringBuilder board = new StringBuilder();
		Piece p;
		for(byte x = 0; x < rowNum; x++) {
			for(byte y = 0; y < colNum; y++) {
				byte hash = hash(x, y);
				if(emptyHash.get(hash) != null) {
					board.append(".");
				} else if((p = whiteHash.get(hash)) != null) {
					board.append(p.pieceChar);
				} else if((p = blackHash.get(hash)) != null) {
					board.append(p.pieceChar);
				}
			}
			board.append(sep);
		}
		return board.toString();
	}

	public boolean isDraw() {
		return moveCounter >= MAX_MOVES;
	}

	public boolean isGameOver() {
		return stateEvaluator.hasWinner() || isDraw();
	}

	public boolean hasSideOnMoveWon() {
		return (this.isWhiteOnMove ? !this.stateEvaluator.blackHasKing : !this.stateEvaluator.whiteHasKing);
	}

	public boolean hasOpponentWon() {
		return (this.isWhiteOnMove ? !this.stateEvaluator.whiteHasKing : !this.stateEvaluator.blackHasKing);
	}

	public boolean hasWhiteWon() {
		return !this.stateEvaluator.blackHasKing; 
	}

	public boolean hasBlackWon() {
		return !this.stateEvaluator.whiteHasKing; 
	}

	public String getWinner() {
		if(isGameOver()) {
			if(isDraw()) {
				return "Game was a Draw";
			} else if(hasWhiteWon()) {
				return "White has won";
			} else {
				return "Black has won";
			}
		} else {
			return "Game is not over";
		}
	}

	public byte gameResult() {
		if(isGameOver()) {
			//note: if last move was a win, do not score it a draw!
			//account for doMove incrementing counter by moving draw to the final check in the if-then-else checks
			if(hasOpponentWon()) { //side on move lost
				return 3;
			} else if(hasSideOnMoveWon()) {
				return 2; //side on move won
			} else {
				return 1; //draw
			}
		} else {
			return 0; //game not over
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		String ret = "State: [isWhiteOnMove: " + isWhiteOnMove + ", Move Counter: " + moveCounter; 

		List keys = new ArrayList(whiteHash.keySet());
		Collections.sort(keys);
		ret += "\n, White Pieces: \n\t[";
		for(Object key : keys) {
			ret += whiteHash.get((Byte) key).toString() + "\n\t";
		}
		ret += "]\n";

		keys = new ArrayList(blackHash.keySet());
		Collections.sort(keys);
		ret += ", Black Pieces: \n\t[";
		for(Object key : keys) {
			ret += blackHash.get((Byte) key).toString() + "\n\t";
		}
		ret += "]\n";

		return ret;
	}
}
