package backgammon;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum Type {
		EXIT, ROLL_REQUEST, ROLL_RESULT, MOVE, BOARD, END_TURN, START_TURN
	};

	Type t;
	ArrayList<Integer> roll;
	volatile ArrayList<Piece>[] board;
	int tri1;
	int tri2;

	public Type getT() {
		return t;
	}

	public ArrayList<Integer> getRoll() {
		return roll;
	}

	public ArrayList<Piece>[] getBoard() {
		return board;
	}

	public int getTri1() {
		return tri1;
	}

	public int getTri2() {
		return tri2;
	}

	public Message(Type type) {
		t = type;
	}

	public Message(ArrayList<Integer> rollIn) {
		t = Type.ROLL_RESULT;
		roll = rollIn;
	}

	public Message(ArrayList[] boardIn) {
		t = Type.BOARD;
		board = boardIn;
	}

	public Message(int tri1In, int tri2In) {
		t = Type.MOVE;
		tri1 = tri1In;
		tri2 = tri2In;
	}

}
