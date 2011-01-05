package ibrokerkit.iservicestore.store;

/**
 * A Locator i-service.
 * Each Locator i-service holds a textual address and description about a user's
 * physical location. In addition, it holds coordinates (lat, lng, zoom) which can 
 * be used to draw the user's location on a map.
 */
public interface Locator extends IService {

	public String getAddress();
	public void setAddress(String address);
	public String getDescription();
	public void setDescription(String pass);
	public Double getLat();
	public void setLat(Double lat);
	public Double getLng();
	public void setLng(Double lng);
	public Double getZoom();
	public void setZoom(Double zoom);
	public Boolean getContactLink();
	public void setContactLink(Boolean contactLink);
}
