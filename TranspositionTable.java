package mc;
import java.util.*;

public class TranspositionTable {
	private final static int ttableSize = 1048576; //16384; //65536; //32768; //524288; //32768; //1048576; //2097152;
	public final static byte EXACT = 1;
	public final static byte BETA = 2;
	public final static byte ALPHA = 3;
	private final Node[] fixedTTable = new Node[ttableSize];

	public TranspositionTable(){}

	public int getIndex(long hash) {
		//return (int) (hash >>> 43);
		return (int) (hash >>> 44);
		//return (int) (hash >>> 45);
		//return (int) (hash >>> 49);
		//return (int) (hash >>> 48);
		//return (int) (hash >>> 50);
	}

	public Node getExactDepth(long hash, byte depth) {
		Node node = this.getExact(hash, depth);
		if(node != null && node.depth == depth) {
			return node;
		} else {
			return null;
		}
	}

	public Node getExact(long hash, byte depth) {
		Node node = this.getNode(hash, depth);
		if(node != null && node.type == EXACT) {
			return node;
		} else {
			return null;
		}
	}

	public Node getNode(long hash, byte depth) {
		Node node = this.get(hash);
		if(node != null && node.hash == hash && node.depth >= depth && node.depth % 2 != depth % 2) {
			return node;
		} else {
			return null;
		}
	}

	public Node get(long hash) {
		return fixedTTable[this.getIndex(hash)];
	}
	public void putReplaceAlways(long hash, byte depth, Score score, byte type) {
		fixedTTable[this.getIndex(hash)] = new Node(hash, depth, score, type);
	}

	public void putReplaceDeeper(long hash, byte depth, Score score, byte type) {
		Node node = this.get(hash);
		if(node == null || depth >= node.depth) {
			this.putReplaceAlways(hash, depth, score, type);
		}
	}
}

class Node {
	public long hash;
	public byte depth;
	public Score score;
	public byte type;

	public Node(long hash, byte depth, Score score, byte type) {
		this.hash = hash;
		this.depth = depth;
		this.score = score;
		this.type = type;
	}
}