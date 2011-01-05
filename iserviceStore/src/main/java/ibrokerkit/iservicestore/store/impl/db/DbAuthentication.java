package ibrokerkit.iservicestore.store.impl.db;

import ibrokerkit.iservicestore.store.impl.AbstractAuthentication;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

public class DbAuthentication extends AbstractAuthentication implements DbObject {

	private static final long serialVersionUID = -2626155163400653723L;

	private Long id;
	private Timestamp timestamp;
	private String qxri;
	private String name;
	private Boolean enabled;
	private Map<String, String> attributes;
	private String indx;
	private String pass;

	DbAuthentication() { 

		this.setEnabled(Boolean.FALSE);
		this.attributes = new HashMap<String, String> ();
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

	public String getPass() {

		return(this.pass);
	}

	public void setPass(String pass) {

		this.pass = pass;
	}

	@SuppressWarnings("unchecked")
	public static List<DbAuthentication> All(Session session) {

		List<DbAuthentication> list = session.getNamedQuery(DbAuthentication.class.getName() + ".All")
		.list();

		return(list);
	}

	@SuppressWarnings("unchecked")
	public static List<DbAuthentication> AllByQxri(Session session, String qxri) {

		List<DbAuthentication> list = session.getNamedQuery(DbAuthentication.class.getName() + ".AllByQxri")
		.setString("qxri", qxri)
		.list();

		return(list);
	}

	@SuppressWarnings("unchecked")
	public static List<DbAuthentication> AllByIndx(Session session, String indx) {

		List<DbAuthentication> list = session.getNamedQuery(DbAuthentication.class.getName() + ".AllByIndx")
		.setString("indx", indx)
		.list();

		return(list);
	}

	public static DbAuthentication EnabledByQxri(Session session, String qxri) {

		List<?> list = session.getNamedQuery(DbAuthentication.class.getName() + ".EnabledByQxri")
		.setString("qxri", qxri)
		.list();

		if (list == null || list.size() == 0) return(null); else return((DbAuthentication) list.get(0));
	}

	public static Long Count(Session session) {

		Long result = (Long) session.getNamedQuery(DbAuthentication.class.getName() + ".Count")
		.uniqueResult();

		return(result);
	}
}
