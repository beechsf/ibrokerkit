package ibrokerkit.iservicefront.authentication.webapplication;

import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;

public class AuthenticationSession extends WebSession {

	private static final long serialVersionUID = -5669746479827302910L;

	private static final String SESSION_ATTRIBUTE_USER_IDENTIFIER = "__user__";

	public AuthenticationSession(Request request) {

		super(request);
	}

	public String getUserIdentifier() {

		return((String) this.getAttribute(SESSION_ATTRIBUTE_USER_IDENTIFIER));
	}

	public void loginUser(String userIdentifier) {

		// insert user in session

		this.setAttribute(SESSION_ATTRIBUTE_USER_IDENTIFIER, userIdentifier);
	}

	public void logoutUser() {

		// delete user from session

		this.removeAttribute(SESSION_ATTRIBUTE_USER_IDENTIFIER);
	}

	public boolean isLoggedIn() {

		return(this.getAttribute(SESSION_ATTRIBUTE_USER_IDENTIFIER) != null);
	}
}