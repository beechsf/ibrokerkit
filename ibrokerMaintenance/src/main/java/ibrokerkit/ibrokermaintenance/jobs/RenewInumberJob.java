package ibrokerkit.ibrokermaintenance.jobs;

import ibrokerkit.ibrokermaintenance.IbrokerMaintenance;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neulevel.epp.xri.EppXriNumber;

public class RenewInumberJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(RenewInumberJob.class.getName());

	private String inumber;

	public RenewInumberJob() {

	}

	@Override
	public void args(String[] args) {

		if (args.length != 1) throw new RuntimeException("Missing I-Number as command line argument.");

		this.inumber = args[0];
	}

	@Override
	public void run() throws Exception {

		EppXriNumber eppXriNumber = IbrokerMaintenance.eppTools.infoInumber(this.inumber.charAt(0), this.inumber);
		Calendar newExpDate = IbrokerMaintenance.eppTools.renewInumber(this.inumber.charAt(0), this.inumber, eppXriNumber.getDateExpired(), 1);

		log.info("New expiration date: " + newExpDate.getTime().toString());
	}
}
