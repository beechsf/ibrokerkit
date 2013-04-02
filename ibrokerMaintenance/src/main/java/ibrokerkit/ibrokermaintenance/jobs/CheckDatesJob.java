package ibrokerkit.ibrokermaintenance.jobs;

import ibrokerkit.ibrokermaintenance.IbrokerMaintenance;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriConstants;
import ibrokerkit.iname4java.store.impl.grs.GrsXri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.neulevel.epp.xri.EppXriName;

public class CheckDatesJob implements Job {

	private static final Log log = LogFactory.getLog(CheckDatesJob.class.getName());

	private boolean fix;

	public CheckDatesJob(boolean fix) {

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

				String date = xri.getXriAttribute(XriConstants.ATTRIBUTE_KEY_DATE);
				String expirationDate = xri.getXriAttribute(XriConstants.ATTRIBUTE_KEY_EXPIRATIONDATE);

				EppXriName infoName = IbrokerMaintenance.eppTools.infoIname(xri.getFullName().charAt(0), xri.getFullName());
				if (infoName == null) { log.info("iname not found. SKIPPING!"); continue; }

				String realDate = Long.toString(infoName.getDateCreated().getTime().getTime());
				String realExpirationDate = Long.toString(infoName.getDateExpired().getTime().getTime());

				log.info("date: " + date + ", real date: " + realDate + (realDate.equals(date) ? "" : ", MISMATCH!"));
				log.info("expirationdate: " + expirationDate + ", real expirationdate: " + realExpirationDate + (realExpirationDate.equals(expirationDate) ? "" : ", MISMATCH!"));

				// fix?

				if (this.fix) {

					if (! realDate.equals(date)) {

						date = realDate;
						xri.setXriAttribute(XriConstants.ATTRIBUTE_KEY_DATE, date);

						log.info("FIXED date");
					}

					if (! realExpirationDate.equals(expirationDate)) {

						expirationDate = realExpirationDate;
						xri.setXriAttribute(XriConstants.ATTRIBUTE_KEY_EXPIRATIONDATE, expirationDate);

						log.info("FIXED expirationdate");
					}
				}
			} catch (Exception ex) {

				log.error("Error with " + xri.getFullName() + ": " + ex.getMessage(), ex);
			}
		}
	}
}
