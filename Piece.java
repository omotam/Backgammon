package backgammon;

import java.io.Serializable;

public class Piece implements Serializable {
	public enum PlayerColor {
		BLACK, WHITE
	};

	PlayerColor color;

	public Piece(PlayerColor c) {
		color = c;
	}

	public PlayerColor getColor() {
		return color;
	}
	
	public boolean equals(Piece other) {
		return color == other.getColor();
	}

}
