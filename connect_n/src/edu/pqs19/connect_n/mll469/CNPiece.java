package edu.pqs19.connect_n.mll469;

public class CNPiece {

	private CNListener owner;
	
	CNPiece(CNListener player) {
		this.owner = player;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CNPiece other = (CNPiece) obj;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}
	
	public String getColor() {
		return "nice";
	}
	
	public CNListener getOwner() {
		return this.owner;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}

	@Override
	public String toString() {
		if (owner == null) {return "null";}
		return owner.toString();
	}
}

