package ibrokerkit.xritools4java;

import org.openxri.resolve.Resolver;

public abstract class Job {

	protected StringBuffer result;
	protected StringBuffer oneLineResult;
	protected StringBuffer stats;

	public abstract String getJobName();
	public abstract void execute(Resolver resolver) throws Exception;

	public String getResult(boolean html) {

		if (html) {

			return(this.result.toString());
		} else {

			return(stripHtml(this.result.toString()));
		}
	}

	public String getOneLineResult() {

		return(this.oneLineResult.toString());
	}

	public String getStats(boolean html) {

		if (html) {

			return(this.stats.toString());
		} else {

			return(stripHtml(this.stats.toString()));
		}
	}

	public boolean hasResult() {

		return(this.result != null && this.result.length() > 0);
	}

	public boolean hasStats() {

		return(this.stats != null && this.stats.length() > 0);
	}

	private String stripHtml(String html) {

		return(html
				.replaceAll("<br>", "\n")
				.replaceAll("<[^>]+>", "")
				.replaceAll("&lt;", "<")
				.replaceAll("&nbsp;", " ")
				.replaceAll("&gt;", ">"));
	}
}
