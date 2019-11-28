package edu.pqs19.connect_n.mll469;

import java.util.List;

public interface CNListener {
	public void notifiedGameMayHaveStarted();
	public void notifiedGameMayHaveEnded();
	public void notifiedMoveMade(List<Object> grid, String move, String s);
	public List<String> getPublicLogs();
}
