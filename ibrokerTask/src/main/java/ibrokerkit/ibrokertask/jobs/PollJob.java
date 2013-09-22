
package ibrokerkit.ibrokertask.jobs;
import ibrokerkit.ibrokertask.IbrokerTask;
import ibrokerkit.iname4java.store.Xri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neulevel.epp.core.response.EppResponseData;
import com.neulevel.epp.xri.response.EppResponseDataTransferXriAuthority;
import com.neulevel.epp.xri.response.EppResponseDataTransferXriName;

public class PollJob {

	private static final Logger log = LoggerFactory.getLogger(PollJob.class.getName());

	public PollJob() {

	}

	public static void run(boolean act) throws Exception {

		log.info("Polling.");

		// check for transfer requests

		EppResponseData eppResponseData;

		eppResponseData = IbrokerTask.eppTools.poll('=', true);
		while (eppResponseData != null)  {

			process(eppResponseData, act);
			eppResponseData = IbrokerTask.eppTools.poll('=', true);
		}

		eppResponseData = IbrokerTask.eppTools.poll('@', true);
		while (eppResponseData != null)  {

			process(eppResponseData, act);
			eppResponseData = IbrokerTask.eppTools.poll('@', true);
		}

		log.info("Done polling.");
	}

	private static void process(EppResponseData eppResponseData, boolean act) throws Exception {

		log.info("Processing response data: " + eppResponseData.getClass().getName());

		if (eppResponseData instanceof EppResponseDataTransferXriAuthority) {

			EppResponseDataTransferXriAuthority eppResponseDataTransferXriAuthority = (EppResponseDataTransferXriAuthority) eppResponseData;

			String transferStatus = eppResponseDataTransferXriAuthority.getTransferStatus();
			String grsAuthorityId = eppResponseDataTransferXriAuthority.getAuthorityId();
			String requestClientId = eppResponseDataTransferXriAuthority.getRequestClientId(); 
			String actionClientId = eppResponseDataTransferXriAuthority.getActionClientId();
			boolean isTransferIn = IbrokerTask.eppTools.isOwnClientId(requestClientId);

			log.info("  --> Transfer Status: " + transferStatus);
			log.info("  --> GRS Authority ID: " + grsAuthorityId);
			log.info("  --> Request Client ID: " + requestClientId);
			log.info("  --> Action Client ID: " + actionClientId);
			log.info("  --> Is Transfer In: " + isTransferIn);

			// perform actions

			if (act) {

				// pending

				if (transferStatus.equals(EppResponseDataTransferXriAuthority.STATUS_PENDING)) {

				}

				// clientApproved / serverApproved

				if (transferStatus.equals(EppResponseDataTransferXriAuthority.STATUS_CLIENT_APPROVED) || transferStatus.equals(EppResponseDataTransferXriAuthority.STATUS_SERVER_APPROVED)) {

					Xri xri = IbrokerTask.xriStore.findXriByGrsAuthorityId(grsAuthorityId);

					if (isTransferIn) {

						IbrokerTask.xriStore.transferAuthorityInComplete(xri);
					} else {

						IbrokerTask.xriStore.transferAuthorityOutComplete(xri);
					}
				}

				// clientRejected / clientCancelled / serverCancelled

				if (transferStatus.equals(EppResponseDataTransferXriAuthority.STATUS_CLIENT_REJECTED) || transferStatus.equals(EppResponseDataTransferXriAuthority.STATUS_CLIENT_CANCELLED) || transferStatus.equals(EppResponseDataTransferXriAuthority.STATUS_SERVER_CANCELLED)) {

					Xri xri = IbrokerTask.xriStore.findXriByGrsAuthorityId(grsAuthorityId);

					if (isTransferIn) {

						IbrokerTask.xriStore.transferAuthorityInCanceled(xri);
					} else {

						IbrokerTask.xriStore.transferAuthorityOutCanceled(xri);
					}
				}
			}
		}

		if (eppResponseData instanceof EppResponseDataTransferXriName) {

			EppResponseDataTransferXriName eppResponseDataTransferXriName = (EppResponseDataTransferXriName) eppResponseData;

			String transferStatus = eppResponseDataTransferXriName.getTransferStatus();
			String iname = eppResponseDataTransferXriName.getIName();
			String requestClientId = eppResponseDataTransferXriName.getRequestClientId(); 
			String actionClientId = eppResponseDataTransferXriName.getActionClientId();
			boolean isTransferIn = IbrokerTask.eppTools.isOwnClientId(requestClientId);

			log.info("  --> Transfer Status: " + transferStatus);
			log.info("  --> I-Name: " + iname);
			log.info("  --> Request Client ID: " + requestClientId);
			log.info("  --> Action Client ID: " + actionClientId);
			log.info("  --> Is Transfer In: " + isTransferIn);

			// perform actions

			if (act) {

				// pending

				if (transferStatus.equals(EppResponseDataTransferXriName.STATUS_PENDING)) {

				}

				// clientApproved / serverApproved

				if (transferStatus.equals(EppResponseDataTransferXriName.STATUS_CLIENT_APPROVED) || transferStatus.equals(EppResponseDataTransferXriName.STATUS_SERVER_APPROVED)) {

					Xri xri = IbrokerTask.xriStore.findXriByGrsAuthorityId(iname);

					if (isTransferIn) {

						IbrokerTask.xriStore.transferXriInComplete(xri);
					} else {

						IbrokerTask.xriStore.transferXriOutComplete(xri);
					}
				}

				// clientRejected / clientCancelled / serverCancelled

				if (transferStatus.equals(EppResponseDataTransferXriName.STATUS_CLIENT_REJECTED) || transferStatus.equals(EppResponseDataTransferXriName.STATUS_CLIENT_CANCELLED) || transferStatus.equals(EppResponseDataTransferXriName.STATUS_SERVER_CANCELLED)) {

					Xri xri = IbrokerTask.xriStore.findXriByGrsAuthorityId(iname);

					if (isTransferIn) {

						IbrokerTask.xriStore.transferXriInCanceled(xri);
					} else {

						IbrokerTask.xriStore.transferXriOutCanceled(xri);
					}
				}
			}
		}
	}
}
