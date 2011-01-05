package ibrokerkit.ibrokerfront.webapplication;

import ibrokerkit.ibrokerstore.store.User;

import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.WebSession;

public class IbrokerSession extends WebSession {

	private static final long serialVersionUID = -3945438585847130731L;

	protected static Log log = LogFactory.getLog(IbrokerSession.class.getName());

	/*
	 * This logs in and remembers users in the session and in cookies.
	 */

	private static final String SESSION_ATTRIBUTE_USER = "__user__";
	private static final String KEY_USER_IDENTIFIER = "__identifier__";
	private static final String KEY_USER_PASS = "__pass__";
	private static final String KEY_USER_NAME = "__name__";
	private static final String KEY_USER_EMAIL = "__email__";

	/*
	 * This allows a user to register, transfer and renew i-names for free.
	 */

	private static final String SESSION_ATTRIBUTE_FREE = "__free__";

	/*
	 * This allows a user to register, transfer and renew i-names at the GRS rate.
	 */

	private static final String SESSION_ATTRIBUTE_GRS = "__grs__";

	/*
	 * This allows a user to transfer and renew i-names without authenticating.
	 */

	private static final String SESSION_ATTRIBUTE_ANON = "__anon__";

	public IbrokerSession(Request request) {

		super(request);
	}

	public User getUser() {

		return((User) this.getAttribute(SESSION_ATTRIBUTE_USER));
	}

	public void loginUser(User user) {

		log.info("Logging in user: " + user.getIdentifier() + " (" + user.getName() + ")");

		// insert user in session

		this.setAttribute(SESSION_ATTRIBUTE_USER, user);
		this.setAttribute(KEY_USER_IDENTIFIER, user.getIdentifier());
		this.setAttribute(KEY_USER_PASS, user.getPass());
		this.setAttribute(KEY_USER_NAME, user.getName());
		this.setAttribute(KEY_USER_EMAIL, user.getEmail());

		// and also using a cookie

		WebResponse response = (WebResponse) RequestCycle.get().getResponse();

		try {

			Cookie[] cookies = new Cookie[4];
			cookies[0] = new Cookie(KEY_USER_IDENTIFIER, new String(Base64.encodeBase64(user.getIdentifier().getBytes("UTF-8"))));
			cookies[1] = new Cookie(KEY_USER_PASS, new String(Base64.encodeBase64(user.getPass().getBytes("UTF-8"))));
			cookies[2] = new Cookie(KEY_USER_NAME, new String(Base64.encodeBase64(user.getName().getBytes("UTF-8"))));
			cookies[3] = new Cookie(KEY_USER_EMAIL, new String(Base64.encodeBase64(user.getEmail().getBytes("UTF-8"))));
			for (Cookie cookie : cookies) cookie.setPath("/");
			for (Cookie cookie : cookies) response.addCookie(cookie);
		} catch (Exception ex) {

		}
	}

	public void logoutUser() {

		log.info("Logging out user.");

		// delete user from session

		this.removeAttribute(SESSION_ATTRIBUTE_USER);
		this.removeAttribute(KEY_USER_IDENTIFIER);
		this.removeAttribute(KEY_USER_PASS);
		this.removeAttribute(KEY_USER_NAME);
		this.removeAttribute(KEY_USER_EMAIL);

		// and also the cookies

		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();

		for (Cookie cookie : request.getCookies()) {

			if (cookie.getName().equals(KEY_USER_IDENTIFIER) ||
					cookie.getName().equals(KEY_USER_PASS) ||
					cookie.getName().equals(KEY_USER_NAME) ||
					cookie.getName().equals(KEY_USER_EMAIL)) {

				response.clearCookie(cookie);
			}
		}
	}

	public boolean isLoggedIn() {

		return(this.getUser() != null);
	}

	public boolean isFree() {

		return(this.getAttribute(SESSION_ATTRIBUTE_FREE) != null);
	}

	public void setFree(boolean free) {

		log.info("Setting free flag: " + Boolean.toString(free));

		// set free flag in session

		if (free) {

			this.setAttribute(SESSION_ATTRIBUTE_FREE, "1");
		} else {

			this.removeAttribute(SESSION_ATTRIBUTE_FREE);
		}
	}

	public boolean isGrs() {

		return(this.getAttribute(SESSION_ATTRIBUTE_GRS) != null);
	}

	public void setGrs(boolean grs) {

		log.info("Setting grs flag: " + Boolean.toString(grs));

		// set grs flag in session

		if (grs) {

			this.setAttribute(SESSION_ATTRIBUTE_GRS, "1");
		} else {

			this.removeAttribute(SESSION_ATTRIBUTE_GRS);
		}
	}

	public boolean isAnon() {

		return(this.getAttribute(SESSION_ATTRIBUTE_ANON) != null);
	}

	public void setAnon(boolean anon) {

		log.info("Setting anon flag: " + Boolean.toString(anon));

		// set anon flag in session

		if (anon) {

			this.setAttribute(SESSION_ATTRIBUTE_ANON, "1");
		} else {

			this.removeAttribute(SESSION_ATTRIBUTE_ANON);
		}
	}
}