package edu.pqs19.connect_n.mll469;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public interface CNListener {
	public void notifiedGameMayHaveStarted();
	public void notifiedGameMayHaveEnded();
	public void notifiedMoveMade(ArrayList<ArrayList<CNPiece>> grid, String move, String s);
	public Color getColor();
	public List<String> getPublicLogs();
}
