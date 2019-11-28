package edu.pqs19.connect_n.mll469;

import java.util.List;

public interface CNListener {
	public void notifiedGameMayHaveStarted();
	public void notifiedGameEnded();
	public void notifiedMoveMade(String s);
	public List<String> getPublicLogs();
}
