package ibrokerkit.epptools4java.store;

public interface Store {

	// general store methods

	public void init() throws StoreException;
	public boolean isInitialized();
	public void close();
	public boolean isClosed();

	// common database methods

	public void updateObject(Object object) throws StoreException;
	public void deleteObject(Object object) throws StoreException;

	// Action methods

	public Action[] listActions() throws StoreException;
	public Action createAction(Character gcs, String transactionId, String request, String response) throws StoreException;

	// Poll methods

	public Poll[] listPolls() throws StoreException;
	public Poll createPoll(Character gcs, String transactionId, String response) throws StoreException;
}
