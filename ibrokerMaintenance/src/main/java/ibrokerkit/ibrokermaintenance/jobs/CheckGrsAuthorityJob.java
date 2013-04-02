package ibrokerkit.ibrokermaintenance.jobs;

import ibrokerkit.ibrokermaintenance.IbrokerMaintenance;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriConstants;
import ibrokerkit.iname4java.store.impl.grs.GrsXri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.neulevel.epp.xri.EppXriAuthority;
import com.neulevel.epp.xri.EppXriName;

public class CheckGrsAuthorityJob implements Job {

	private static final Log log = LogFactory.getLog(CheckGrsAuthorityJob.class.getName());

	private boolean fix;

	public CheckGrsAuthorityJob(boolean fix) {

		this.fix = fix;
	}

	@Override
	public void args(String[] args) {
		
	}

	@Override
	public void run() throws Exception {

		for (Xri xri : IbrokerMaintenance.xriStore.listXris()) {

			log.info("XRI: " + xri.getFullName() + " (" + xri.getClass().getSimpleName() + ")");

			try {

				if (! (xri instanceof GrsXri)) continue;

				String grsAuthorityId = xri.getAuthorityAttribute(XriConstants.ATTRIBUTE_GRS_AUTHORITYID);
				String grsPassword = xri.getAuthorityAttribute(XriConstants.ATTRIBUTE_GRS_AUTHORITYPASSWORD);

				EppXriName infoName = IbrokerMaintenance.eppTools.infoIname(xri.getFullName().charAt(0), xri.getFullName());
				if (infoName == null) { log.info("iname not found. SKIPPING!"); continue; }
				EppXriAuthority infoAuthority = IbrokerMaintenance.eppTools.infoAuthority(xri.getFullName().charAt(0), infoName.getAuthorityId(), false);
				if (infoAuthority == null) { log.info("authority not found. SKIPPING!"); continue; }

				String realGrsAuthorityId = infoAuthority.getAuthorityId();
				String realGrsPassword = infoAuthority.getAuthInfo().getValue();

				log.info("grs-authorityid: " + grsAuthorityId + ", real grs-authorityid: " + realGrsAuthorityId + (realGrsAuthorityId.equals(grsAuthorityId) ? "" : ", MISMATCH!"));
				log.info("grs-authoritypassword: " + grsPassword + ", real grs-authoritypassword: " + realGrsPassword + (realGrsPassword.equals(grsPassword) ? "" : ", MISMATCH!"));

				// fix?

				if (this.fix) {

					if (! realGrsAuthorityId.equals(grsAuthorityId)) {

						grsAuthorityId = realGrsAuthorityId;
						xri.setAuthorityAttribute(XriConstants.ATTRIBUTE_GRS_AUTHORITYID, grsAuthorityId);

						log.info("FIXED grs-authorityid");
					}

					if (! realGrsPassword.equals(grsPassword)) {

						grsPassword = realGrsPassword;
						xri.setAuthorityAttribute(XriConstants.ATTRIBUTE_GRS_AUTHORITYPASSWORD, grsPassword);

						log.info("FIXED grs-authoritypassword");
					}
				}
			} catch (Exception ex) {

				log.error("Error with " + xri.getFullName() + ": " + ex.getMessage(), ex);
			}
		}
	}
}
