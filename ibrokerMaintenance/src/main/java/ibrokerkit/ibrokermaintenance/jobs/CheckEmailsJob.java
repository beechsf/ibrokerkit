package ibrokerkit.ibrokermaintenance.jobs;

import ibrokerkit.ibrokermaintenance.IbrokerMaintenance;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.impl.grs.GrsXri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.neulevel.epp.xri.EppXriAuthority;
import com.neulevel.epp.xri.EppXriName;

public class CheckEmailsJob implements Job {

	private static final Log log = LogFactory.getLog(CheckEmailsJob.class.getName());

	private boolean fix;

	public CheckEmailsJob(boolean fix) {

		this.fix = fix;
	}

	@Override
	public void run() throws Exception {

		for (Xri xri : IbrokerMaintenance.xriStore.listXris()) {

			log.info("XRI: " + xri.getFullName() + " (" + xri.getClass().getSimpleName() + ")");

			try {

				if (! (xri instanceof GrsXri)) continue;

				User user = IbrokerMaintenance.ibrokerStore.findUser(xri.getUserIdentifier());
				String email = user.getEmail();

				EppXriName infoName = IbrokerMaintenance.eppTools.infoIname(xri.getFullName().charAt(0), xri.getFullName());
				if (infoName == null) { log.info("iname not found. SKIPPING!"); continue; }
				EppXriAuthority infoAuthority = IbrokerMaintenance.eppTools.infoAuthority(xri.getFullName().charAt(0), infoName.getAuthorityId(), false);
				if (infoAuthority == null) { log.info("authority not found. SKIPPING!"); continue; }

				String realEmail = infoAuthority.getSocialData().getPrimaryEmail();

				log.info("email: " + email + ", real email: " + realEmail + (realEmail.equals(email) ? "" : ", MISMATCH!"));

				// fix?

				if (this.fix) {

					if (! realEmail.equals(email)) {

						email = realEmail;
						user.setEmail(email);
						IbrokerMaintenance.ibrokerStore.updateObject(user);

						log.info("FIXED email");
					}
				}
			} catch (Exception ex) {

				log.error("Error with " + xri.getFullName() + ": " + ex.getMessage(), ex);
			}
		}
	}
}
