package ibrokerkit.ibrokermaintenance.jobs;

import ibrokerkit.epptools4java.EppTools;
import ibrokerkit.ibrokermaintenance.IbrokerMaintenance;
import ibrokerkit.iname4java.store.impl.grs.GrsXriData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neulevel.epp.xri.EppXriSocialData;
import com.neulevel.epp.xri.response.EppResponseDataCreateXriAuthority;
import com.neulevel.epp.xri.response.EppResponseDataCreateXriNumber;

public class CreateInumberJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(CreateInumberJob.class.getName());

	public CreateInumberJob() {

	}

	@Override
	public void args(String[] args) {
		
	}

	@Override
	public void run() throws Exception {

		GrsXriData grsXriData = new GrsXriData();
		grsXriData.setTrusteeEscrowAgent("@");
		grsXriData.setTrusteeContactAgent("@!(!!1003!1)");
		grsXriData.setName("Markus");
		grsXriData.setPrimaryEmail("markus.sabadello@gmail.com");
		grsXriData.setCountryCode("AT");
		grsXriData.setPostalCode("1020");
		grsXriData.setCity("Vienna");
		grsXriData.setStreet(new String[] { "Untere Augartenstrasse 34/24" });

		String authorityId = "5948.3594.5872";

		// build the GRS data

		char gcs = '=';
		String grsAuthorityId = IbrokerMaintenance.eppTools.makeGrsAuthorityId(gcs, authorityId);
		String grsAuthorityPassword = EppTools.makeGrsAuthorityPassword();
		EppXriSocialData eppXriSocialData = EppTools.makeEppXriSocialData(grsXriData.getStreet(), grsXriData.getCity(), grsXriData.getState(), grsXriData.getPostalCode(), grsXriData.getCountryCode(), grsXriData.getName(), grsXriData.getOrganization(), grsXriData.getPrimaryVoice(), grsXriData.getSecondaryVoice(), grsXriData.getFax(), grsXriData.getPrimaryEmail(), grsXriData.getSecondaryEmail(), grsXriData.getPager());
		String trusteeEscrowAgent = grsXriData.getTrusteeEscrowAgent();
		String trusteeContactAgent = grsXriData.getTrusteeContactAgent();

		// create authority and i-number and i-name

		EppResponseDataCreateXriAuthority eppResponseDataCreateXriAuthority; 
		EppResponseDataCreateXriNumber eppResponseDataCreateXriNumber;

		eppResponseDataCreateXriAuthority = IbrokerMaintenance.eppTools.createAuthority(gcs, grsAuthorityId, grsAuthorityPassword, eppXriSocialData, trusteeEscrowAgent, trusteeContactAgent);
		eppResponseDataCreateXriNumber = IbrokerMaintenance.eppTools.createInumber(gcs, grsAuthorityId, grsAuthorityId, 1);

		log.info("I-Number: " + eppResponseDataCreateXriNumber.getINumber());
		log.debug("Authority Response Data: " + eppResponseDataCreateXriAuthority);
		log.debug("Number Response Data: " + eppResponseDataCreateXriNumber);
	}
}
