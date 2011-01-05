package ibrokerkit.iservicestore.store.impl.db;

import ibrokerkit.iservicestore.store.impl.AbstractLocator;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;


public class DbLocator extends AbstractLocator implements DbObject {

	private static final long serialVersionUID = -2623248771846187363L;
	
	private Long id;
	private Timestamp timestamp;
	private String qxri;
	private String name;
	private Boolean enabled;
	private Map<String, String> attributes;
	private String indx;
	private String address;
	private String description;
	private Double lat;
	private Double lng;
	private Double zoom;
	private Boolean contactLink;

	DbLocator() {

		this.setEnabled(Boolean.FALSE);
		this.setContactLink(Boolean.FALSE);
		this.attributes = new HashMap<String, String> ();
	}
	
	public Long getId() {

		return(this.id);
	}
	
	void setId(Long id) {

		this.id = id;
	}
	
	public Timestamp getTimestamp() {

		return(this.timestamp);
	}
	
	void setTimestamp(Timestamp timestamp) {

		this.timestamp = timestamp;
	}
	
	public String getQxri() {
		
		return(this.qxri);
	}

	public void setQxri(String qxri) {
		
		this.qxri = qxri;
	}

	public String getName() {
		
		return(this.name);
	}

	public void setName(String name) {
		
		this.name = name;
	}

	public Boolean getEnabled() {
		
		return(this.enabled);
	}
	
	public void setEnabled(Boolean enabled) {
		
		this.enabled = enabled;
	}

	public Map<String, String> getAttributes() {

		return(this.attributes);
	}

	public void setAttributes(Map<String, String> attributes) {
		
		this.attributes = attributes;
	}

	public String getIndx() {

		return(this.indx);
	}

	public void setIndx(String indx) {

		this.indx = indx;
	}

	public String getAddress() {
		
		return(this.address);
	}

	public void setAddress(String address) {
		
		this.address = address;
	}
	
	public String getDescription() {
		
		return(this.description);
	}
	
	public void setDescription(String pass) {
		
		this.description = pass;
	}

	public Double getLat() {
		
		return(this.lat);
	}

	public void setLat(Double lat) {
		
		this.lat = lat;
	}

	public Double getLng() {
		
		return(this.lng);
	}

	public void setLng(Double lng) {
		
		this.lng = lng;
	}

	public Double getZoom() {
		
		return(this.zoom);
	}

	public void setZoom(Double zoom) {

		this.zoom = zoom;
	}

	public Boolean getContactLink() {
		
		return(this.contactLink);
	}

	public void setContactLink(Boolean contactLink) {
		
		this.contactLink = contactLink;
	}

	@SuppressWarnings("unchecked")
	public static List<DbLocator> All(Session session) {
		
		List<DbLocator> list = session.getNamedQuery(DbLocator.class.getName() + ".All")
			.list();
		
		return(list);
	}
	
	@SuppressWarnings("unchecked")
	public static List<DbLocator> AllByQxri(Session session, String qxri) {
		
		List<DbLocator> list = session.getNamedQuery(DbLocator.class.getName() + ".AllByQxri")
			.setString("qxri", qxri)
			.list();
		
		return(list);
	}
	
	@SuppressWarnings("unchecked")
	public static List<DbLocator> AllByIndx(Session session, String indx) {
		
		List<DbLocator> list = session.getNamedQuery(DbLocator.class.getName() + ".AllByIndx")
			.setString("indx", indx)
			.list();
		
		return(list);
	}
	
	public static DbLocator EnabledByQxri(Session session, String qxri) {
		
		List<?> list = session.getNamedQuery(DbLocator.class.getName() + ".EnabledByQxri")
			.setString("qxri", qxri)
			.list();
	
		if (list == null || list.size() == 0) return(null); else return((DbLocator) list.get(0));
	}
	
	public static Long Count(Session session) {
		
		Long result = (Long) session.getNamedQuery(DbLocator.class.getName() + ".Count")
			.uniqueResult();
		
		return(result);
	}
}
