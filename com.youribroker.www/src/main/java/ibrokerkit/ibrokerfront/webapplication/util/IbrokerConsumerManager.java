package ibrokerkit.ibrokerfront.webapplication.util;

import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;


public class IbrokerConsumerManager extends ConsumerManager {

	public IbrokerConsumerManager() throws ConsumerException {
		
		super();

		this.setAssociations(new InMemoryConsumerAssociationStore());
		this.setNonceVerifier(new InMemoryNonceVerifier(300));
	}
}
