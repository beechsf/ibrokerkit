package ibrokerkit.ibrokertask;

public class Options {

	private boolean doCheckExpiration = false;
	private boolean doPoll = false;
	private boolean actCheckExpiration = false;
	private boolean actPoll = false;

	public Options(boolean doCheckExpiration, boolean doPoll, boolean actCheckExpiration, boolean actPoll) {

		this.doCheckExpiration = doCheckExpiration;
		this.doPoll = doPoll;
		this.actCheckExpiration = actCheckExpiration;
		this.actPoll = actPoll;
	}

	public Options(String[] args) {

		for (String arg : args) {

			if ("-docheckexpiration".equals(arg)) this.doCheckExpiration = true;
			if ("-dopoll".equals(arg)) this.doPoll = true;
			if ("-actcheckexpiration".equals(arg)) this.actCheckExpiration = true;
			if ("-actpoll".equals(arg)) this.actPoll = true;
		}
	}

	public boolean isDoCheckExpiration() {

		return(this.doCheckExpiration);
	}

	public void setDoCheckExpiration(boolean doCheckExpiration) {

		this.doCheckExpiration = doCheckExpiration;
	}

	public boolean isDoPoll() {

		return(this.doPoll);
	}

	public void setDoPoll(boolean doPoll) {

		this.doPoll = doPoll;
	}

	public boolean isActCheckExpiration() {

		return(this.actCheckExpiration);
	}

	public void setActCheckExpiration(boolean actCheckExpiration) {

		this.actCheckExpiration = actCheckExpiration;
	}

	public boolean isActPoll() {

		return(this.actPoll);
	}

	public void setActPoll(boolean actPoll) {

		this.actPoll = actPoll;
	}
}
