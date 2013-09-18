package ibrokerkit.iservicefront;

import java.util.Properties;

public interface IserviceApplication {

	public ibrokerkit.iservicestore.store.Store getIserviceStore();
	public org.openxri.store.Store getOpenxriStore();
	public Properties getProperties();
	public Properties getVelocityProperties();
}
