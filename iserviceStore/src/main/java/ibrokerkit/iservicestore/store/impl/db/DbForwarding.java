package ibrokerkit.iservicestore.store.impl.db;

import ibrokerkit.iservicestore.store.impl.AbstractForwarding;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;


public class DbForwarding extends AbstractForwarding implements DbObject {

	private static final long serialVersionUID = -1509983706909175539L;

	private Long id;
	private Timestamp timestamp;
	private String qxri;
	private String name;
	private Boolean enabled;
	private Map<String, String> attributes;
	private String indx;
	private Boolean indexPage;
	private Boolean errorPage;
	private Map<String, String> mappings;

	DbForwarding() { 

		this.setEnabled(Boolean.FALSE);
		this.attributes = new HashMap<String, String> ();
		this.setIndexPage(Boolean.FALSE);
		this.setErrorPage(Boolean.FALSE);
		this.mappings = new HashMap<String, String> ();
	}

	public Long getId() {

		return(this.id);
	}

	void setId(Long id) {

		this.id = id;
	}

	public Timestamp getTimestamp() {

		return (this.timestamp);
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

	public Boolean getIndexPage() {

		return(this.indexPage);
	}

	public void setIndexPage(Boolean indexPage) {

		this.indexPage = indexPage;
	}

	public Boolean getErrorPage() {

		return(this.errorPage);
	}

	public void setErrorPage(Boolean errorPage) {

		this.errorPage = errorPage;
	}

	public Map<String, String> getMappings() {

		return(this.mappings);
	}

	public void setMappings(Map<String, String> mappings) {

		this.mappings = mappings;
	}

	@SuppressWarnings("unchecked")
	public static List<DbForwarding> All(Session session) {

		List<DbForwarding> list = session.getNamedQuery(DbForwarding.class.getName() + ".All")
		.list();

		return(list);
	}

	@SuppressWarnings("unchecked")
	public static List<DbForwarding> AllByQxri(Session session, String qxri) {

		List<DbForwarding> list = session.getNamedQuery(DbForwarding.class.getName() + ".AllByQxri")
		.setString("qxri", qxri)
		.list();

		return(list);
	}

	@SuppressWarnings("unchecked")
	public static List<DbForwarding> AllByIndx(Session session, String indx) {

		List<DbForwarding> list = session.getNamedQuery(DbForwarding.class.getName() + ".AllByIndx")
		.setString("indx", indx)
		.list();

		return(list);
	}

	public static DbForwarding EnabledByQxri(Session session, String qxri) {

		List<?> list = session.getNamedQuery(DbForwarding.class.getName() + ".EnabledByQxri")
		.setString("qxri", qxri)
		.list();

		if (list == null || list.size() == 0) return(null); else return((DbForwarding) list.get(0));
	}

	public static Long Count(Session session) {

		Long result = (Long) session.getNamedQuery(DbForwarding.class.getName() + ".Count")
		.uniqueResult();

		return(result);
	}
}
