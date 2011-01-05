package ibrokerkit.epptools4java.store.impl;

import ibrokerkit.epptools4java.store.Poll;

public abstract class AbstractPoll implements Poll {

	private static final long serialVersionUID = 1933033023307787629L;

	public int compareTo(Poll other) {

		if (other == this) return(0);
		if (other == null) return(0);

		return(this.getResponse().compareTo(other.getResponse()));
	}

	@Override
	public String toString() {

		return(this.getResponse());
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) return(true);
		if (o == null) return(false);

		if (this.getResponse() != null) return(this.getResponse().equals(((Poll) o).getResponse()));

		return(false);
	}

	@Override
	public int hashCode() {

		if (this.getResponse() != null) return(this.getResponse().hashCode());

		return(0);
	}
}
