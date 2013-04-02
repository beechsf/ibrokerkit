package ibrokerkit.ibrokermaintenance.jobs;

import ibrokerkit.ibrokermaintenance.IbrokerMaintenance;

public class DeleteInameJob implements Job {

	private String iname;

	public DeleteInameJob() {

	}

	@Override
	public void args(String[] args) {

		if (args.length != 1) throw new RuntimeException("Missing I-Name as command line argument.");

		this.iname = args[0];
	}

	@Override
	public void run() throws Exception {

		IbrokerMaintenance.eppTools.deleteIname(this.iname.charAt(0), this.iname);
	}
}
