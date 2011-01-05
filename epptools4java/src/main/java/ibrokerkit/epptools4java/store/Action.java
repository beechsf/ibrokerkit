package ibrokerkit.epptools4java.store;

import java.io.Serializable;
import java.util.Date;

public interface Action extends Serializable, Comparable<Action> {

	public Long getId();
	public Date getTimestamp();
	public Character getGcs();
	public void setGcs(Character gcs);
	public String getTransactionId();
	public void setTransactionId(String transactionId);
	public String getRequest();
	public void setRequest(String request);
	public String getResponse();
	public void setResponse(String response);
}
