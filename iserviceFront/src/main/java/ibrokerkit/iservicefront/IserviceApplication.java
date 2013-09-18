package ibrokerkit.iservicefront;

import java.util.Properties;

import org.openid4java.server.ServerManager;

public interface IserviceApplication {

	public ibrokerkit.iservicestore.store.Store getIserviceStore();
	public org.openxri.store.Store getOpenxriStore();
	public Properties getProperties();
	public Properties getVelocityProperties();
	public ServerManager getServerManager();
}
