package ibrokerkit.ibrokermaintenance.jobs;

import ibrokerkit.ibrokermaintenance.IbrokerMaintenance;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iservicestore.store.Authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckAuthenticationPasswordsJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(CheckAuthenticationPasswordsJob.class.getName());

	private boolean fix;

	public CheckAuthenticationPasswordsJob(boolean fix) {

		this.fix = fix;
	}

	@Override
	public void args(String[] args) {

	}

	@Override
	public void run() throws Exception {

		for (User user : IbrokerMaintenance.ibrokerStore.listUsers()) {

			log.info("User: " + user.getIdentifier() + " (" + user.getEmail() + ")");

			try {

				for (Authentication authentication : IbrokerMaintenance.iserviceStore.listAuthenticationsByIndex(user.getIdentifier())) {

					log.info("  Authentication: " + authentication.getId() + " (" + authentication.getQxri() + ")");

					String userPass = user.getPass();
					String authenticationPass = authentication.getPass();

					log.info("  authenticationPass: " + authenticationPass + ", userPass: " + userPass + (userPass.equals(authenticationPass) ? "" : ", MISMATCH!"));

					// fix?

					if (this.fix) {

						if (! userPass.equals(authenticationPass)) {

							authenticationPass = userPass;
							authentication.setPass(authenticationPass);
							IbrokerMaintenance.iserviceStore.updateObject(authentication);

							log.info("FIXED authenticationPass");
						}
					}
				}
			} catch (Exception ex) {

				log.error("Error with " + user.getIdentifier() + ": " + ex.getMessage(), ex);
			}
		}
	}
}
