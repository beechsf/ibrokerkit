package ibrokerkit.epptools4java.store;

import java.io.Serializable;
import java.util.Date;

public interface Poll extends Serializable, Comparable<Poll> {

	public Long getId();
	public Date getTimestamp();
	public Character getGcs();
	public void setGcs(Character gcs);
	public String getTransactionId();
	public void setTransactionId(String transactionId);
	public String getResponse();
	public void setResponse(String response);
}
