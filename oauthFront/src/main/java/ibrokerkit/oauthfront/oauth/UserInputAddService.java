package ibrokerkit.oauthfront.oauth;

import java.util.List;

import org.openxri.xml.Service;

public class UserInputAddService implements UserInput {

	private List<Service> removeConflictingServices;

	public UserInputAddService() {

	}

	public List<Service> getRemoveConflictingServices() {

		return (this.removeConflictingServices);
	}

	public void setRemoveConflictingServices(List<Service> removeConflicting) {

		this.removeConflictingServices = removeConflicting;
	}
}
