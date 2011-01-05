package ibrokerkit.iservicestore.store.impl;

import ibrokerkit.iservicestore.store.Contact;
import ibrokerkit.iservicestore.store.IService;


public abstract class AbstractContact implements Contact {

	public int compareTo(IService other) {

		if (other == this) return(0);
		if (other == null) return(0);

		return(this.getId().compareTo(other.getId()));
	}

	@Override
	public String toString() {

		return(this.getQxri());
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) return(true);
		if (o == null) return(false);

		if (this.getId() != null) return(this.getId().equals(((Contact) o).getId()));

		return(false);
	}

	@Override
	public int hashCode() {

		if (this.getId() != null) return(this.getId().hashCode());

		return(0);
	}
}
