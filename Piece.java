package mc;

public class Piece {
	public final static int PAWN_VALUE = 100;
	public final static int KING_VALUE = -1;
	public final static byte PAWN = 1;
	public final static byte KNIGHT = 2;
	public final static byte BISHOP = 3;
	public final static byte ROOK = 4;
	public final static byte QUEEN = 5;
	public final static byte KING = 6;

	public byte pieceType;
	/*
		1: pawn
		2: knight
		3: bishop
		4: rook
		5: queen
		6: king
	*/

	public char pieceChar;
	public boolean isWhite;
	public byte row;
	public byte col;
	public byte hash;
	public int nominalValue;

	public Piece() {}
	public Piece(char pieceChar, byte pieceType, boolean isWhite, byte row, byte col) {
		setupPiece(pieceChar, pieceType, isWhite, row, col);
	}
	public Piece(char pieceChar, byte row, byte col) {
		boolean isWhite = Character.isUpperCase(pieceChar);
		byte pieceType;
		switch(Character.toUpperCase(pieceChar)) {
			case 'P': pieceType = PAWN; break; 
			case 'N': pieceType = KNIGHT; break; 
			case 'B': pieceType = BISHOP; break; 
			case 'R': pieceType = ROOK; break; 
			case 'Q': pieceType = QUEEN; break; 
			case 'K': pieceType = KING; break; 
			default: pieceType = -1;
		}
		setupPiece(pieceChar, pieceType, isWhite, row, col);
	}

	private void setupPiece(char pieceChar, byte pieceType, boolean isWhite, byte row, byte col) {
		this.pieceChar = pieceChar;
		this.pieceType = pieceType;
		this.isWhite = isWhite;
		this.row = row;
		this.col = col;
		this.hash = State.hash(row, col);
		this.nominalValue = this.getNominalValue(pieceType);
	}

	public static int getNominalValue(byte pieceType) {
		//values taken from: https://chess.stackexchange.com/questions/2409/how-many-points-is-each-chess-piece-worth
		switch(pieceType) {
			case PAWN: return 2 * PAWN_VALUE;
			case KNIGHT: return 3 * PAWN_VALUE; 
			case BISHOP: return 4 * PAWN_VALUE; //3
			case ROOK: return 5 * PAWN_VALUE;
			case QUEEN: return 11 * PAWN_VALUE; //9
			case KING: return KING_VALUE;
			default: return PAWN_VALUE;
		}
	}

	public void setPieceType(byte pieceType) {
		this.pieceType = pieceType;
		this.nominalValue = this.getNominalValue(pieceType);
		switch(pieceType) {
			case PAWN: this.pieceChar = 'P'; break;
			case KNIGHT: this.pieceChar = 'N'; break;
			case BISHOP: this.pieceChar = 'B'; break;
			case ROOK: this.pieceChar = 'R'; break;
			case QUEEN: this.pieceChar = 'Q'; break;
			case KING: this.pieceChar = 'K'; break;
		}

		if(!this.isWhite) {
			this.pieceChar = Character.toLowerCase(this.pieceChar);
		}
	}

	public void setPiecePosition(byte row, byte col) {
		this.row = row;
		this.col = col;
		this.hash = State.hash(row, col);
	}

	public Piece clone() {
		return new Piece(this.pieceChar, this.pieceType, this.isWhite, this.row, this.col);
	}

	@Override
	public String toString() {
		return "Piece: [pieceChar: " + pieceChar + ", pieceType: " + pieceType + ", isWhite: " + isWhite + ", row: " + row + ", col: " + col + ", Value: " +  nominalValue + ", Hash: " + hash + "]";
	}
}
