package mc;
import java.util.*;

public class Negamax {
	public boolean isSearchInterrupted = false;
	public Move bestMove = null;
	public Move bestMoveSoFar = null;
	public Score bestScore = null;
	public Score bestScoreSoFar = null;
	public byte bestDepthSoFar = -1;
	public TranspositionTable tt = new TranspositionTable();
	public TranspositionTable ttDeep = new TranspositionTable();
	public boolean useAB = true;
	public boolean useTT = true;

	public Negamax() {}
	private static void scanLoop(State state, ArrayList<Move> moves, 
			byte row0, byte col0, 
			byte row, byte col,
			byte dRow, byte dCol, 
			Piece p, 
			boolean stopShort, Boolean canCapture) {
		do {
			row = (byte) (row + dRow);
			col = (byte) (col + dCol);

			if(row > 5 || col > 4 || row < 0 || col < 0) {
				break;
			}

			Piece capturedPiece = state.getPieceAt(row, col);
			if(capturedPiece != null) {
				if(capturedPiece.isWhite == p.isWhite) {
					break;
				}
				if(canCapture != null && canCapture == false) {
					break;
				}
				stopShort = true;
			} else if(canCapture == null) {
				break;
			}

			moves.add(new Move(row0, col0, row, col, p, capturedPiece));
		} while(stopShort==false);
	}

	public static void scan(State state, ArrayList<Move> moves, 
			byte row0, byte col0, 
			byte dRow, byte dCol, 
			Piece p, boolean stopShort, Boolean canCapture) {
		scanLoop(state, moves, row0, col0, row0, col0, dRow, dCol, p, stopShort, canCapture);
	}

	private static void symScan(State state, ArrayList<Move> moves, 
			byte row0, byte col0, byte dRow, byte dCol, Piece p, boolean stopShort, Boolean canCapture) {
		scan(state, moves, row0, col0, dRow, dCol, p, stopShort, canCapture);
		scan(state, moves, row0, col0, dCol, (byte) (-1 * dRow), p, stopShort, canCapture);
		scan(state, moves, row0, col0, (byte) (-1 * dRow), (byte) (-1 * dCol), p, stopShort, canCapture);
		scan(state, moves, row0, col0, (byte) (-1 * dCol), dRow, p, stopShort, canCapture);
	}

	public boolean isMoveLegal(State state, Move m) {
		ArrayList<Move> moves = generateMovesForPiece(state, m.piece);
		if(moves.size() == 0) {
			return false;
		}
		for(Move move : moves) {
			if(move.toString().equals(m.toString())) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<Move> generateMovesForPiece(State state, Piece p) {
		ArrayList<Move> moves = new ArrayList<Move>();
		switch(p.pieceType) {
			case Piece.QUEEN:
				symScan(state, moves, p.row, p.col, (byte) 0, (byte) 1, p, false, true);
				symScan(state, moves, p.row, p.col, (byte) 1, (byte) 1, p, false, true);
				break;
			case Piece.KING:
				symScan(state, moves, p.row, p.col, (byte) 0, (byte) 1, p, true, true);
				symScan(state, moves, p.row, p.col, (byte) 1, (byte) 1, p, true, true);
				break;
			case Piece.ROOK:
				symScan(state, moves, p.row, p.col, (byte) 0, (byte) 1, p, false, true);
				break;
			case Piece.BISHOP: 
				symScan(state, moves, p.row, p.col, (byte) 1, (byte) 1, p, false, true);
				symScan(state, moves, p.row, p.col, (byte) 0, (byte) 1, p, true, false);
				break;
			case Piece.KNIGHT:
				symScan(state, moves, p.row, p.col, (byte) 2, (byte) 1, p, true, true);
				symScan(state, moves, p.row, p.col, (byte) 2, (byte) -1, p, true, true);
				break;
			case Piece.PAWN:
				byte dir = -1;
				if(!p.isWhite) {
					dir = 1;
				}
				scan(state, moves, p.row, p.col, dir, (byte) 1, p, true, null);
				scan(state, moves, p.row, p.col, dir, (byte) -1, p, true, null);
				scan(state, moves, p.row, p.col, dir, (byte) 0, p, true, false);
				break;
		}

		return moves;
	}

	public static ArrayList<Move> generateAllSideOnMoveMoves(State state) {
		ArrayList<Move> moves = new ArrayList<Move>();
		for(Piece p : state.sideOnMoveHash.values()) {
			moves.addAll(generateMovesForPiece(state, p));
		}
		return moves;
	}

	public void initNegamaxState() {
		this.isSearchInterrupted = false;
		this.bestMoveSoFar = null;
		this.bestMove = null;
		this.bestScore = null;
		this.bestScoreSoFar = null;
		this.bestDepthSoFar = (byte) -1;
	}

	public Move iterativeDeepening(State state, int IDTimeMS) {
		Thread monitorThread = new Thread(new MonitorRunnable(IDTimeMS, this));
		initNegamaxState();
		for(byte i = 1;; i++) {
			this.bestScore = null;
			try {
				Score val = this.depthSearch(state.clone(), i, i, Score.Loss(), Score.Win());
				if(val != null && val.isLoss) {
					return this.bestMoveSoFar;
				}
				if(val != null && this.bestMove != null) {
					this.bestDepthSoFar = i;
					this.bestMoveSoFar = this.bestMove.clone();
					this.bestScoreSoFar = this.bestScore.clone();
					if(val.isWin || val.isDraw) {
						return this.bestMoveSoFar;
					}
				}
				if(i >= 40) {
					return this.bestMoveSoFar;
				}
				if(i==1) {
					monitorThread.start();
				}
			} catch(EscapeRecursionException e) {
				break;
			}
		}
		if(monitorThread != null) {
			monitorThread.interrupt();
		}
		return this.bestMoveSoFar;
	}

	public Score depthSearch(State state, byte depth, byte fullDepth, Score alpha, Score beta) throws EscapeRecursionException {
		if(this.isSearchInterrupted) {
			this.bestMove = null;
			throw new EscapeRecursionException();
		}

		Node cachedScore = null;
		if(useTT && this.bestMove != null && this.bestScore != null && depth != fullDepth) {
			cachedScore = tt.getNode(state.incrementalHash, (byte)(depth + 1));
			if(cachedScore == null) {
				cachedScore = ttDeep.getNode(state.incrementalHash, (byte)(depth + 1));
			}
			if(cachedScore != null && cachedScore.score != null) {
				if(cachedScore.type == TranspositionTable.EXACT) {
					state.switchSides();
					return cachedScore.score.clone();
				} else if(cachedScore.type == TranspositionTable.BETA) {
					alpha = cachedScore.score.clone().negate();
				} else if(cachedScore.type == TranspositionTable.ALPHA) {
					beta = cachedScore.score.clone().negate();
				}
			}
		}

		Score score = state.stateEvaluator.getSideOnMoveScore(state);
		if(score.isTerminal() || depth <= 0) {
			state.switchSides();
			return score.clone();
		}

		ArrayList<Move> moves = generateAllSideOnMoveMoves(state);
		if(moves.size() == 0) {
			state.switchSides();
			return Score.Loss();
		}
		Collections.shuffle(moves);
		if(this.bestMoveSoFar != null) {
			for(Move newMove : moves) {
				if(newMove.toString().equals(this.bestMoveSoFar.toString())) {
					newMove.isPV = true;
				}
			}
		}
		Collections.sort(moves, new MoveComparator());
		Score localBestValue = Score.Loss();
		byte flag = TranspositionTable.ALPHA;

		for(Move m : moves) {
			doMove(state, m);
			state.switchSides();
			Score val = depthSearch(state, (byte) (depth - 1), fullDepth, beta.clone().negate(), alpha.clone().negate()).negate();
			undoMove(state, m);

			if(val.isGreaterThan(localBestValue)) {
				localBestValue = val.clone();
			}

			if(depth == fullDepth) {
				if(this.bestScore == null || this.bestScore.isLessThan(val)) {
					this.bestScore = val.clone();
					this.bestMove = m.clone();
				}
			}

			if(useAB) {
				//fail-high -- CUT node -- score is a lower bound on actual score, 
				//which could be better but its already too good to be true
				if(val.isGreaterThanEqual(beta)) {
					if(useTT) {
						tt.putReplaceAlways(state.incrementalHash, depth, beta.clone(), TranspositionTable.BETA);
						ttDeep.putReplaceDeeper(state.incrementalHash, depth, beta.clone(), TranspositionTable.BETA);	
					}
					if(depth != fullDepth) {
						state.switchSides();
					}
					return beta.clone();
				}

				//fail-low -- ALL node -- score is an upper bound on actual score,
				//which could be worse, so we possibly shrink the window and keep searching 
				if(localBestValue.isGreaterThan(alpha)) {
					alpha = localBestValue.clone();
					flag = TranspositionTable.EXACT;
				}
			} else {
				if(val.isGreaterThan(alpha)) {
					alpha = val.clone();
				}
			}
		}

		if(useTT) {
			if(flag == TranspositionTable.ALPHA) {
				tt.putReplaceAlways(state.incrementalHash, depth, localBestValue.clone(), TranspositionTable.ALPHA);	
				ttDeep.putReplaceDeeper(state.incrementalHash, depth, localBestValue.clone(), TranspositionTable.ALPHA);
			} else {
				tt.putReplaceAlways(state.incrementalHash, depth, localBestValue.clone(), TranspositionTable.EXACT);
				ttDeep.putReplaceDeeper(state.incrementalHash, depth, localBestValue.clone(), TranspositionTable.EXACT);
			}
		}

		if(depth != fullDepth) {
			state.switchSides();
		}
		return alpha.clone();
	}

	public static Move stringToMove(State state, String move) {		
		String[] parts = move.split("-");
		String from = parts[0];
		String to = parts[1];
		
		byte fromCol = (byte) (((int) from.charAt(0)) - 97);
		byte toCol = (byte) (((int) to.charAt(0)) - 97);

		byte fromRow = (byte) (State.rowNum - Character.getNumericValue(from.charAt(1)));
		byte toRow = (byte) (State.rowNum - Character.getNumericValue(to.charAt(1)));

		Piece p = state.getPieceAt(fromRow, fromCol);
		Piece capturedPiece = state.getPieceAt(toRow, toCol);

		return new Move(fromRow, fromCol, toRow, toCol, p, capturedPiece);
	}

	public static void doMove(State state, Move move) {
		ZobristHash.placeMoveOnState(state, move);
		byte fromHash = State.hash(move.fromRow, move.fromCol);
		byte toHash = State.hash(move.toRow, move.toCol);
		state.moveCounter++;

		if(move.capturedPiece == null) { //move to blank space
			state.sideOnMoveHash.remove(fromHash); //remove piece from current position
			state.emptyHash.put(fromHash, true); //add blank slot to the from-position
			state.emptyHash.remove(toHash); //remove blank slot from to-position
			state.sideOnMoveHash.put(toHash, move.piece); //add piece to destination to-position
		} else { //move to capture
			state.sideOnMoveHash.remove(fromHash); //remove piece from current from-position
			state.emptyHash.put(fromHash, true); //add blank slot to the from-position
			state.otherSideHash.remove(toHash); //remove piece from other side's (to-) position
			state.sideOnMoveHash.put(toHash, move.piece); //add piece to same position for side-on-move
			state.stateEvaluator.adjustIncrementalScore(move.capturedPiece, 1);
			if(move.capturedPiece.pieceType == Piece.KING) {
				state.stateEvaluator.updateOpponentKing(state.isWhiteOnMove, false);
			}
		}
		move.piece.setPiecePosition(move.toRow, move.toCol);

		if(move.isPromotion) { //handle promotion
			move.piece.setPieceType(Piece.QUEEN);
			state.stateEvaluator.adjustIncrementalScore(state.isWhiteOnMove, Piece.getNominalValue(Piece.QUEEN) - Piece.PAWN_VALUE);
		}
	}

	public static void undoMove(State state, Move move) {
		byte fromHash = State.hash(move.fromRow, move.fromCol);
		byte toHash = State.hash(move.toRow, move.toCol);
		state.moveCounter--;

		if(move.capturedPiece == null) { //undo move to blank space
			state.sideOnMoveHash.put(fromHash, move.piece); //put piece back on old from-position
			state.emptyHash.remove(fromHash); //remove blank slot from the from-position
			state.emptyHash.put(toHash, true); //put blank slot back on to-position
			state.sideOnMoveHash.remove(toHash); //remove piece from to-position
		} else { //undo move to capture
			state.sideOnMoveHash.put(fromHash, move.piece); //put piece back on old from-position
			state.emptyHash.remove(fromHash); //remove blank slot from the from-position
			state.otherSideHash.put(toHash, move.capturedPiece); //put captured piece back on the other side's (to-) position
			state.sideOnMoveHash.remove(toHash); //remove piece from same position for side-on-move
			state.stateEvaluator.adjustIncrementalScore(move.capturedPiece, -1);
			if(move.capturedPiece.pieceType == Piece.KING) {
				state.stateEvaluator.updateOpponentKing(state.isWhiteOnMove, true);
			}
		}

		move.piece.setPiecePosition(move.fromRow, move.fromCol);

		if(move.isPromotion) { //reverse any promotion
			move.piece.setPieceType(Piece.PAWN);
			state.stateEvaluator.adjustIncrementalScore(state.isWhiteOnMove, 
				Piece.PAWN_VALUE - Piece.getNominalValue(Piece.QUEEN));
		}
		ZobristHash.placeMoveOnState(state, move);
	}
}

class EscapeRecursionException extends Exception {
}
