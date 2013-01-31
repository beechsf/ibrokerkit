package ibrokerkit.ibrokermaintenance.jobs;

import ibrokerkit.ibrokermaintenance.IbrokerMaintenance;
import ibrokerkit.iname4java.store.Xri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CheckGrsAuthorityIdJob implements Job {

	private static final Log log = LogFactory.getLog(CheckGrsAuthorityIdJob.class.getName());

	@Override
	public void run() throws Exception {

		for (Xri xri : IbrokerMaintenance.xriStore.listXris()) {

			log.info("XRI: " + xri.getFullName());

			String grsAuthorityId = xri.getAuthorityAttribute("grs-authorityid");

			if (grsAuthorityId == null) log.warn("No grs-authorityid: " + xri.getFullName());
		}
	}
}
