package ibrokerkit.oauthfront.webapplication;

import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;

public class OauthSession extends WebSession {

	private static final long serialVersionUID = -5669746479827302910L;

	public OauthSession(Request request) {

		super(request);
	}
}