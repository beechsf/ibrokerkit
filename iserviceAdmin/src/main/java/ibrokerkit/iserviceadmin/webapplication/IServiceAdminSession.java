package ibrokerkit.iserviceadmin.webapplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;

public class IServiceAdminSession extends WebSession {

	private static final long serialVersionUID = 2830198511972192850L;

	protected static Log log = LogFactory.getLog(IServiceAdminSession.class.getName());
	
	public IServiceAdminSession(Request request) {

		super(request);
	}
}
