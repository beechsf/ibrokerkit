package ibrokerkit.ibrokermaintenance.jobs;

import ibrokerkit.epptools4java.EppTools;
import ibrokerkit.ibrokermaintenance.IbrokerMaintenance;
import ibrokerkit.iname4java.store.impl.grs.GrsXriData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.neulevel.epp.xri.EppXriSocialData;
import com.neulevel.epp.xri.response.EppResponseDataCreateXriAuthority;
import com.neulevel.epp.xri.response.EppResponseDataCreateXriName;
import com.neulevel.epp.xri.response.EppResponseDataCreateXriNumber;

public class CreateInumberJob implements Job {

	private static final Log log = LogFactory.getLog(CreateInumberJob.class.getName());

	public CreateInumberJob() {

	}

	@Override
	public void run() throws Exception {

		GrsXriData grsXriData = new GrsXriData();
		grsXriData.setTrusteeEscrowAgent("@");
		grsXriData.setTrusteeContactAgent("@!(!!1003!1) ");
		grsXriData.setName("Markus");
		grsXriData.setPrimaryEmail("markus.sabadello@gmail.com");
		grsXriData.setCountryCode("AT");
		grsXriData.setPostalCode("1020");
		grsXriData.setCity("Vienna");
		grsXriData.setStreet(new String[] { "Untere Augartenstrasse 34/24" });

		String authorityId = "44783";

		// build the GRS data

		char gcs = '=';
		String grsAuthorityId = IbrokerMaintenance.eppTools.makeGrsAuthorityId(gcs, authorityId);
		String grsAuthorityPassword = EppTools.makeGrsAuthorityPassword();
		EppXriSocialData eppXriSocialData = EppTools.makeEppXriSocialData(grsXriData.getStreet(), grsXriData.getCity(), grsXriData.getState(), grsXriData.getPostalCode(), grsXriData.getCountryCode(), grsXriData.getName(), grsXriData.getOrganization(), grsXriData.getPrimaryVoice(), grsXriData.getSecondaryVoice(), grsXriData.getFax(), grsXriData.getPrimaryEmail(), grsXriData.getSecondaryEmail(), grsXriData.getPager());
		String trusteeEscrowAgent = grsXriData.getTrusteeEscrowAgent();
		String trusteeContactAgent = grsXriData.getTrusteeContactAgent();

		// create authority and i-number i-name

		EppResponseDataCreateXriAuthority eppResponseDataCreateXriAuthority; 
		EppResponseDataCreateXriNumber eppResponseDataCreateXriNumber;
		EppResponseDataCreateXriName eppResponseDataCreateXriName;

		eppResponseDataCreateXriAuthority = IbrokerMaintenance.eppTools.createAuthority(gcs, grsAuthorityId, grsAuthorityPassword, eppXriSocialData, trusteeEscrowAgent, trusteeContactAgent);
		eppResponseDataCreateXriNumber = IbrokerMaintenance.eppTools.createInumber(gcs, grsAuthorityId, grsAuthorityId, 1);

		log.info("I-Number: " + eppResponseDataCreateXriNumber.getINumber());
	}
}
