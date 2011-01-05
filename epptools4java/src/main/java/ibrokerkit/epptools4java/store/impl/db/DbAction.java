package ibrokerkit.epptools4java.store.impl.db;

import ibrokerkit.epptools4java.store.impl.AbstractAction;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Session;


@SuppressWarnings("unchecked")
public class DbAction extends AbstractAction implements DbObject {

	private static final long serialVersionUID = 3686797907882969409L;

	private Long id;
	private Timestamp timestamp;
	private Character gcs;
	private String transactionId;
	private String request;
	private String response;

	DbAction() { 

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

	public Character getGcs() {

		return(this.gcs);
	}

	public void setGcs(Character gcs) {

		this.gcs = gcs;
	}

	public String getTransactionId() {

		return(this.transactionId);
	}

	public void setTransactionId(String transactionId) {

		this.transactionId = transactionId;
	}

	public String getRequest() {

		return(this.request);
	}

	public void setRequest(String request) {

		this.request = request;
	}

	public String getResponse() {

		return(this.response);
	}

	public void setResponse(String response) {

		this.response = response;
	}

	public static List<DbAction> All(Session session) {

		List<DbAction> list = session.getNamedQuery(DbAction.class.getName() + ".All")
		.list();

		return(list);
	}

	public static DbAction ByTransactionId(Session session, String transactionId) {

		DbAction result = (DbAction) session.getNamedQuery(DbAction.class.getName() + ".ByTransactionId")
		.setString("transactionId", transactionId)
		.uniqueResult();
	
		return(result);
	}

	public static Long Count(Session session) {

		Long result = (Long) session.getNamedQuery(DbAction.class.getName() + ".Count")
		.uniqueResult();

		return(result);
	}
}
