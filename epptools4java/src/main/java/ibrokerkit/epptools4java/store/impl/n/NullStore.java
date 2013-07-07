package ibrokerkit.epptools4java.store.impl.n;

import ibrokerkit.epptools4java.store.Action;
import ibrokerkit.epptools4java.store.Poll;
import ibrokerkit.epptools4java.store.Store;
import ibrokerkit.epptools4java.store.StoreException;

import java.util.Properties;

public class NullStore implements Store {

	public NullStore() {

	}

	public NullStore(Properties properties) {

	}

	@Override
	public void init() throws StoreException {

	}

	@Override
	public boolean isInitialized() {

		return false;
	}

	@Override
	public void close() {

	}

	@Override
	public boolean isClosed() {

		return false;
	}

	@Override
	public void updateObject(Object object) throws StoreException {

	}

	@Override
	public void deleteObject(Object object) throws StoreException {

	}

	@Override
	public Action[] listActions() throws StoreException {

		return null;
	}

	@Override
	public Action createAction(Character gcs, String transactionId, String request, String response) throws StoreException {

		return null;
	}

	@Override
	public Poll[] listPolls() throws StoreException {

		return null;
	}

	@Override
	public Poll createPoll(Character gcs, String transactionId, String response) throws StoreException {

		return null;
	}
}