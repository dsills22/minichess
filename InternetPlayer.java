package mc;

public class InternetPlayer {
	public static State state = new State();
	public static int moveTimeMs = 6500;
	public static Client client;
	private static String username;
	private static String password;
	public static String argName = "";
	public static String arg = "";
	public static char color = '?';
	public static boolean isWhite = true;
	public static boolean isOffer = true;
	public static String gameId = "";
	public static boolean useTT = false;
	public static boolean useTimeMgmt = false;
	private static int i = 0;
	public static Player player = null;

	public InternetPlayer(){}

	public static void main(String[] args) {
		if(args != null) {
			for(i = 0; i < args.length; i++) {
				if(i % 2 == 0) {
					argName = args[i];
				} else {
					arg = args[i];
					switch(argName) {
						case "--color": 
							color = arg.toLowerCase().charAt(0);
							break;
						case "--type":
							isOffer = arg.toLowerCase().equals("offer");
							break;
						case "--game":
							gameId = arg;
							break;
						case "--username":
							username = arg;
							break;
						case "--password":
							password = arg;
							break;
						case "--movetime":
							moveTimeMs = Integer.parseInt(arg);
							break;
						case "--useTT": 
							useTT = arg.toLowerCase().charAt(0) == 'T';
							break;
						case "--useTM": 
							useTimeMgmt = arg.toLowerCase().charAt(0) == 'T';
							break;
					}
				}
			}
		}

		player = new Player((byte) 1, true, true, useTT, useTimeMgmt);

		try {
			client = new Client("imcs.svcs.cs.pdx.edu", "3589", username, password);

			try {
				if(isOffer) { //make offer, then play
					isWhite = (client.offer(color) == 'W');
				} else { //accept some game id with color pref
					isWhite = (client.accept(gameId, color) == 'W');
				}

				if(isWhite) {
					System.out.println("You are playing as White");

					//send move
					Move firstMove = makeMove();
					System.out.println("Your first move is: " + firstMove);
				} else {
					System.out.println("You are playing as Black");
				}

				while(true) {
					makeTheirMove();
					if(state.isGameOver()) {
						break;
					}

					/*System.out.println("BEFORE*************************");
					System.out.println(state.moveCounter);
					System.out.println(state.printBoard());*/
					
					Move myMove = makeMove();
					
					/*System.out.println("");
					System.out.println("AFTER*************************");
					System.out.println(state.moveCounter);
					System.out.println(state.printBoard());
					System.out.println("");*/

					System.out.println("Your move is: " + myMove);
					if(myMove == null || state.isGameOver()) {
						break;
					}
				}

				client.close();
				System.out.println(state.printBoard());
				System.out.println(state.getWinner());
				if(isWhite) {
					System.out.println("You played as White");
				} else {
					System.out.println("You played as Black");
				}
			} catch(Exception e) {
				client.close();
				e.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static Move makeMove() throws Exception {
		Move move = player.getBestScoredMove(state.clone(), moveTimeMs);
		String resp = client.sendMove(move.toString());
		Negamax.doMove(state, move);
		System.out.println("Depth: " + player.depth);
		state.switchSides();
		return move;
	}

	public static Move getTheirMove() throws Exception {
		String move = client.getMove();
		if(move != null) {
			Move theirMove = Negamax.stringToMove(state, move);
			//System.out.println("THEIR MOVE WAS [" + theirMove.verbose() + "]");
			return theirMove;
		}
		return null;
	}

	public static String makeTheirMove() throws Exception {
		Move m = getTheirMove();
		Negamax.doMove(state, m);
		state.switchSides();
		return m.toString();
	}
}