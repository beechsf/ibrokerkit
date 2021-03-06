package ibrokerkit.epptools4java.store.impl.db;

import ibrokerkit.epptools4java.store.impl.AbstractPoll;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Session;


@SuppressWarnings("unchecked")
public class DbPoll extends AbstractPoll implements DbObject {

	private static final long serialVersionUID = 4570496844208175029L;

	private Long id;
	private Timestamp timestamp;
	private Character gcs;
	private String transactionId;
	private String response;

	DbPoll() { 

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

	public String getResponse() {

		return(this.response);
	}

	public void setResponse(String response) {

		this.response = response;
	}

	public static List<DbPoll> All(Session session) {

		List<DbPoll> list = session.getNamedQuery(DbPoll.class.getName() + ".All")
		.list();

		return(list);
	}

	public static DbPoll ByTransactionId(Session session, String transactionId) {

		DbPoll result = (DbPoll) session.getNamedQuery(DbPoll.class.getName() + ".ByTransactionId")
		.setString("transactionId", transactionId)
		.uniqueResult();
	
		return(result);
	}

	public static Long Count(Session session) {

		Long result = (Long) session.getNamedQuery(DbPoll.class.getName() + ".Count")
		.uniqueResult();

		return(result);
	}
}
