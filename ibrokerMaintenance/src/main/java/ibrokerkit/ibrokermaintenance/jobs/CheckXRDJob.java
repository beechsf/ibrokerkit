package ibrokerkit.ibrokermaintenance.jobs;

import ibrokerkit.ibrokermaintenance.IbrokerMaintenance;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.impl.grs.GrsXri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxri.xml.CanonicalID;

import com.neulevel.epp.xri.EppXriAuthority;
import com.neulevel.epp.xri.EppXriName;
import com.neulevel.epp.xri.EppXriNumberAttribute;

public class CheckXRDJob implements Job {

	private static final Log log = LogFactory.getLog(CheckXRDJob.class.getName());

	private boolean fix;

	public CheckXRDJob(boolean fix) {

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

				String canonicalID = xri.getCanonicalID() == null ? null : xri.getCanonicalID().getValue();

				EppXriName infoName = IbrokerMaintenance.eppTools.infoIname(xri.getFullName().charAt(0), xri.getFullName());
				if (infoName == null) { log.info("iname not found. SKIPPING!"); continue; }
				EppXriAuthority infoAuthority = IbrokerMaintenance.eppTools.infoAuthority(xri.getFullName().charAt(0), infoName.getAuthorityId(), true);
				if (infoAuthority == null) { log.info("authority not found. SKIPPING!"); continue; }

				String realCanonicalID = ((EppXriNumberAttribute) infoAuthority.getINumber().get(0)).getINumber();

				log.info("CanonicalID: " + canonicalID + ", real CanonicalID: " + realCanonicalID + (realCanonicalID.equals(canonicalID) ? "" : ", MISMATCH!"));

				// fix?

				if (this.fix) {

					if (! realCanonicalID.equals(canonicalID)) {

						canonicalID = realCanonicalID;
						xri.setCanonicalID(new CanonicalID(canonicalID));

						log.info("FIXED CanonicalID");
					}
				}
			} catch (Exception ex) {

				log.error("Error with " + xri.getFullName() + ": " + ex.getMessage(), ex);
			}
		}
	}
}
