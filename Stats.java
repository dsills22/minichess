package mc;
import java.util.*;

public class Stats {
	public static void main(String[] args) {
		HashMap<String, Integer> stats = new HashMap<String, Integer>();
		for(int i = 0; i < 20; i++) {
			Game.main(new String[] {
				"--white",
				"ab:1:700",
				"--black",
				"ab:1:200",
				"--quiet",
				"1"
			});
			String result = Game.result;
			if(stats.get(result) == null) {
				stats.put(result, 1);
			} else {
				stats.put(result, stats.get(result) + 1);
			}

			for(String key : stats.keySet()) {
				System.out.println(key + " --> " + stats.get(key));
			}
			System.out.println();
		}

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		for(String key : stats.keySet()) {
			System.out.println(key + " --> " + stats.get(key));
		}
	}
}