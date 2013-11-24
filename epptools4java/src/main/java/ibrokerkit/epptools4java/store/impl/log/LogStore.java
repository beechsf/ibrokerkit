package ibrokerkit.epptools4java.store.impl.log;

import ibrokerkit.epptools4java.store.Action;
import ibrokerkit.epptools4java.store.Poll;
import ibrokerkit.epptools4java.store.Store;
import ibrokerkit.epptools4java.store.StoreException;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogStore implements Store {

	private static Logger log = LoggerFactory.getLogger(LogStore.class.getName());

	public LogStore() {

	}

	public LogStore(Properties properties) {

	}

	@Override
	public void init() throws StoreException {

		log.debug("init()");
	}

	@Override
	public boolean isInitialized() {

		log.debug("isInitialized()");

		return true;
	}

	@Override
	public void close() {

		log.debug("close()");
	}

	@Override
	public boolean isClosed() {

		log.debug("isClosed()");

		return false;
	}

	@Override
	public void updateObject(Object object) throws StoreException {

		log.debug("updateObject(): " + object);
	}

	@Override
	public void deleteObject(Object object) throws StoreException {

		log.debug("deleteObject(): " + object);
	}

	@Override
	public Action[] listActions() throws StoreException {

		log.debug("listActions()");

		return null;
	}

	@Override
	public Action createAction(Character gcs, String transactionId, String request, String response) throws StoreException {

		log.debug("createAction(): " + gcs + " " + transactionId);
		log.debug(request);
		log.debug(response);

		return null;
	}

	@Override
	public Poll[] listPolls() throws StoreException {

		log.debug("listPolls()");

		return null;
	}

	@Override
	public Poll createPoll(Character gcs, String transactionId, String response) throws StoreException {

		log.debug("createPoll(): " + gcs + " " + transactionId);
		log.debug(response);

		return null;
	}
}