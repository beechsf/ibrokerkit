package ibrokerkit.ibrokermaintenance.jobs;

import ibrokerkit.ibrokermaintenance.IbrokerMaintenance;
import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.impl.grs.GrsXri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.neulevel.epp.xri.EppXriAuthority;
import com.neulevel.epp.xri.EppXriName;
import com.neulevel.epp.xri.EppXriSocialData;

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

					if (email == null || email.isEmpty()) {

						email = realEmail;
						user.setEmail(email);
						IbrokerMaintenance.ibrokerStore.updateObject(user);

						log.info("FIXED email");
					} else if (! realEmail.equals(email)) {

						realEmail = email;
						EppXriSocialData socialData = infoAuthority.getSocialData();
						socialData.setPrimaryEmail(realEmail);
						socialData.setSecondaryEmail(null);
						IbrokerMaintenance.eppTools.setSocialData(xri.getFullName().charAt(0), infoAuthority.getAuthorityId(), infoAuthority.getAuthInfo().getValue(), socialData);

						/*infoAuthority.getSocialData() == null || infoAuthority.getSocialData().getPostalInfo() == null || infoAuthority.getSocialData().getPostalInfo().getAddress() == null ? null : infoAuthority.getSocialData().getPostalInfo().getAddress().getStreet(), 
						infoAuthority.getSocialData() == null || infoAuthority.getSocialData().getPostalInfo() == null || infoAuthority.getSocialData().getPostalInfo().getAddress() == null ? null : infoAuthority.getSocialData().getPostalInfo().getAddress().getCity(), 
						infoAuthority.getSocialData() == null || infoAuthority.getSocialData().getPostalInfo() == null || infoAuthority.getSocialData().getPostalInfo().getAddress() == null ? null : infoAuthority.getSocialData().getPostalInfo().getAddress().getState(), 
						infoAuthority.getSocialData() == null || infoAuthority.getSocialData().getPostalInfo() == null || infoAuthority.getSocialData().getPostalInfo().getAddress() == null ? null : infoAuthority.getSocialData().getPostalInfo().getAddress().getPostalCode(), 
						infoAuthority.getSocialData() == null || infoAuthority.getSocialData().getPostalInfo() == null || infoAuthority.getSocialData().getPostalInfo().getAddress() == null ? "XX" : infoAuthority.getSocialData().getPostalInfo().getAddress().getCountryCode(),
						infoAuthority.getSocialData() == null || infoAuthority.getSocialData().getPostalInfo() == null ? null : infoAuthority.getSocialData().getPostalInfo().getName(),
						infoAuthority.getSocialData() == null || infoAuthority.getSocialData().getPostalInfo() == null ? null : infoAuthority.getSocialData().getPostalInfo().getOrganization(),
						infoAuthority.getSocialData() == null || infoAuthority.getSocialData().getPrimaryVoice() == null ? "+1.0000000" : infoAuthority.getSocialData().getPrimaryVoice().getNumber(),
						infoAuthority.getSocialData() == null || infoAuthority.getSocialData().getSecondaryVoice() == null ? "+1.0000000" : infoAuthority.getSocialData().getSecondaryVoice().getNumber(),
						infoAuthority.getSocialData() == null || infoAuthority.getSocialData().getFax() == null ? "+1.0000000" : infoAuthority.getSocialData().getFax().getNumber(),
						realEmail,
						infoAuthority.getSocialData() == null ? "noemail@noemail.com" : infoAuthority.getSocialData().getSecondaryEmail(),
						infoAuthority.getSocialData() == null || infoAuthority.getSocialData().getPager() == null ? "+1.0000000" : infoAuthority.getSocialData().getPager().getNumber());*/

						log.info("FIXED email");
					}
				}
			} catch (Exception ex) {

				log.error("Error with " + xri.getFullName() + ": " + ex.getMessage(), ex);
			}
		}
	}
}
