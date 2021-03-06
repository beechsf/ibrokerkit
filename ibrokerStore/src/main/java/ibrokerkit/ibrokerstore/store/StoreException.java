package ibrokerkit.ibrokerstore.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Exception thrown by various methods accessing the Store.
 */
public class StoreException extends Exception {

	private static final long serialVersionUID = 6472000796830169833L;

	private static Logger log = LoggerFactory.getLogger(StoreException.class);

	public StoreException() {
		
		super();

		log.error(null);
	}

	public StoreException(String message) {
		
		super(message);

		log.error(message);
	}

	public StoreException(String message, Throwable t) {
		
		super(message, t);

		log.error(message, t);
	}

	public StoreException(Throwable t) {
		
		super(t);

		log.error(t.getMessage(), t);
	}
}
