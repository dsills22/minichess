package mc; 
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Move {
	public byte fromRow;
	public byte fromCol;
	public byte toRow;
	public byte toCol;
	public Piece piece;
	public Piece capturedPiece;
	public boolean isPromotion;
	public boolean isPV = false;

	public Move() {}
	
	public Move(byte fromRow, byte fromCol, byte toRow, byte toCol, Piece piece, Piece capturedPiece) {
		this.fromRow = fromRow;
		this.fromCol = fromCol;
		this.toRow = toRow;
		this.toCol = toCol;
		this.piece = piece;
		this.capturedPiece = capturedPiece;

		if(piece.pieceType == Piece.PAWN) {
			if(piece.isWhite && toRow == 0) {
				this.isPromotion = true;
			} else if(!piece.isWhite && toRow == 5) {
				this.isPromotion = true;
			}
		}
	}

	public Move clone() {
		return new Move(this.fromRow, this.fromCol, this.toRow, this.toCol, this.piece.clone(), 
			(this.capturedPiece == null ? null : this.capturedPiece.clone()));
	}

	public String verbose() {
		return this.toString() + ", Piece: " + this.piece.toString() + ", Capture: " + 
			(this.capturedPiece == null ? "" : this.capturedPiece.toString());
 	}

	@Override
	public String toString() {
		char fromColChar = (char) ((int) (this.fromCol + 97));
		char toColChar = (char) ((int) (this.toCol + 97));
		return fromColChar + Integer.toString(State.rowNum - this.fromRow) + '-' + toColChar + Integer.toString(State.rowNum - this.toRow);
	}

	public boolean isEqual(Move comp) {
		return comp.toString().equals(this.toString());
	}
}

class MoveComparator implements Comparator<Move> {
	public MoveComparator() {}

    @Override
    public int compare(Move a, Move b) {
    	int aVal = 0;
    	int bVal = 0;

    	if(a.isPV) {
    		aVal += 50000;
    	}
    	if(a.capturedPiece != null) {
    		if(a.capturedPiece.pieceType == Piece.KING) {
    			aVal += 100000;
    		} else {
    			aVal += a.capturedPiece.nominalValue;
    		}
    	}

    	if(b.isPV) {
    		bVal += 50000;
    	}
    	if(b.capturedPiece != null) {
    		if(b.capturedPiece.pieceType == Piece.KING) {
    			bVal += 100000;
    		} else {
    			bVal += b.capturedPiece.nominalValue;
    		}
    	}

    	return aVal > bVal ? -1 : aVal < bVal ? 1 : 0;
    }
}