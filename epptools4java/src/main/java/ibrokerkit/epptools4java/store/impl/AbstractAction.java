package ibrokerkit.epptools4java.store.impl;

import ibrokerkit.epptools4java.store.Action;

public abstract class AbstractAction implements Action {

	private static final long serialVersionUID = -7169318875900470588L;

	public int compareTo(Action other) {

		if (other == this) return(0);
		if (other == null) return(0);

		return(this.getRequest().compareTo(other.getRequest()));
	}

	@Override
	public String toString() {

		return(this.getRequest());
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) return(true);
		if (o == null) return(false);

		if (this.getRequest() != null) return(this.getRequest().equals(((Action) o).getRequest()));

		return(false);
	}

	@Override
	public int hashCode() {

		if (this.getRequest() != null) return(this.getRequest().hashCode());

		return(0);
	}
}
