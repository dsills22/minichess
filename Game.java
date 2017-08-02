package mc;

public class Game {
	public static String result = "";

	public static void main(String[] args) {
		boolean isWhiteOnMove = true;
		boolean whiteAB = true;
		boolean blackAB = true;
		boolean whiteTT = true;
		boolean blackTT = true;
		boolean whiteTimeMgmt = false;
		boolean blackTimeMgmt = false;
		boolean ABTestMode = false;
		boolean quiet = false;
		byte whiteDepth = 1;
		byte blackDepth = 1;
		int whiteTime = -1;
		int blackTime = -1;
		byte moveCounter = 0;
		String boardString = 
			"kqbnr" + State.sep + 
			"ppppp" + State.sep + 
			"....." + State.sep + 
			"....." + State.sep + 
			"PPPPP" + State.sep + 
			"RNBQK";
		String whiteMoveSorter = "shuffle+score"; //shuffle, moveString
		String blackMoveSorter = "shuffle+score";
		String[] subArgs = null;
		String sep = System.getProperty("line.separator");

		int i = 0;
		int j = 0;
		String argName = "";
		String arg = "";
		if(args != null) {
			for(i = 0; i < args.length; i++) {
				if(i % 2 == 0) {
					argName = args[i];
				} else {
					arg = args[i];
					switch(argName) {
						case "--white": //ab:DEPTH:TIME:ORDERING
							if(arg.toLowerCase().contains("ab")) {
								whiteAB = true;
							} else {
								whiteAB = false;
							}
							if(arg.toLowerCase().contains("tt")) {
								whiteTT = true;
							} else {
								whiteTT = false;
							}
							if(arg.toLowerCase().contains("m")) {
								whiteTimeMgmt = true;
							} else {
								whiteTimeMgmt = false;
							}
							subArgs = arg.split(":");
							if(subArgs.length > 1) {
								whiteDepth = Byte.parseByte(subArgs[1]);
								if(subArgs.length > 2) {
									whiteTime = Integer.parseInt(subArgs[2]);
									if(subArgs.length > 3) {
										whiteMoveSorter = subArgs[3];
									}
								}
							}
							break;
						case "--black": //ab:DEPTH:TIME:ORDERING
							if(arg.toLowerCase().contains("ab")) {
								blackAB = true;
							} else {
								blackAB = false;
							}
							if(arg.toLowerCase().contains("tt")) {
								blackTT = true;
							} else {
								blackTT = false;
							}
							if(arg.toLowerCase().contains("m")) {
								blackTimeMgmt = true;
							} else {
								blackTimeMgmt = false;
							}
							subArgs = arg.split(":");
							if(subArgs.length > 1) {
								blackDepth = Byte.parseByte(subArgs[1]);
								if(subArgs.length > 2) {
									blackTime = Integer.parseInt(subArgs[2]);
									if(subArgs.length > 3) {
										blackMoveSorter = subArgs[2];
									}
								}
							}
							break;
						case "--board": 
							boardString = "";
							for (j = 0; j < arg.length(); j++){
							    char c = arg.charAt(j);        
							    boardString += c;
							    if((j + 1) % 5 == 0) {
							    	boardString += sep;
							    }
							}
							break;
						case "--move": //w or b
							if(arg.toLowerCase().equals("w")) {
								isWhiteOnMove = true;
							} else {
								isWhiteOnMove = false;
							}
							break;
						case "--counter": //0...80
							moveCounter = Byte.parseByte(arg);
							break;
						case "--testmode":
							ABTestMode = true;
							break;
						case "--quiet":
							quiet = true;
					}
				}
			}
		}

		//System.out.println(whiteAB + "\n" + blackAB + "\n" + whiteDepth + "\n" + blackDepth + "\n" + whiteTime + "\n" + blackTime + "\n" + boardString);
		
		State state = new State(moveCounter, isWhiteOnMove, boardString);
		boolean whiteID = whiteTime > 0;
		boolean blackID = blackTime > 0;
		Player white = new Player(whiteDepth, whiteAB, whiteID, whiteTT, whiteTimeMgmt);
		Player black = new Player(blackDepth, blackAB, blackID, blackTT, blackTimeMgmt);
		Player sideOnMove = null;

		if(ABTestMode) {
			white.ABTestMode = true;
			black.ABTestMode = true;
		}

		long startTime = System.currentTimeMillis();

		while(true) {
			sideOnMove = (state.isWhiteOnMove ? white : black);

			if(!quiet) {
				System.out.println(state.incrementalHash);
				System.out.println(ZobristHash.boardHash(state));
			}

			Move move = sideOnMove.getBestScoredMove(state, (state.isWhiteOnMove ? whiteTime : blackTime));

			if(!quiet) {
				System.out.println("----------------------> " + state.moveCounter + " " + (state.isWhiteOnMove ? "W" : "B"));
				System.out.println("DEPTH: " + sideOnMove.depth);
			}

			//System.out.println(state.incrementalHash);
			//System.out.println(ZobristHash.boardHash(state));

			if(move != null) {
				if(!quiet) { System.out.println(move.piece.pieceChar + ": " + move); }
				Negamax.doMove(state, move);
			} else {
				if(!quiet) { System.out.println("Uh oh, null move in Game.java..."); }
				state.moveCounter++;
			}

			if(!quiet) {
				//System.out.println(state.incrementalHash);
				//System.out.println(ZobristHash.boardHash(state));

				System.out.println(state.printBoard());
				//System.out.println(sideOnMove.negamax.bestScore);
				System.out.println(sideOnMove.negamax.bestScoreSoFar);
			}

			if(!state.stateEvaluator.whiteHasKing || !state.stateEvaluator.blackHasKing || state.moveCounter > State.MAX_MOVES) {
				break;
			}
			state.switchSides();
			if(!quiet) { System.out.println(); }
		}

		black.negamax.isSearchInterrupted = true;
		white.negamax.isSearchInterrupted = true;

		if(!quiet) {
			System.out.println("");
			System.out.println("Final Score:");
			System.out.println(sideOnMove.negamax.bestScore);
			System.out.println(sideOnMove.negamax.bestScoreSoFar);
		}

		System.out.println(state.getWinner());
		System.out.println("Game Time: " + (((double) (System.currentTimeMillis() - startTime)) / 1000.));

		result = state.getWinner();
	}
}