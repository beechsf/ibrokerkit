package ibrokerkit.iservicefront.authentication.webapplication;

import org.openid4java.server.ServerManager;

import ibrokerkit.iservicefront.IserviceApplication;

public interface AuthenticationApplication extends IserviceApplication {

	public ServerManager getServerManager();
}
