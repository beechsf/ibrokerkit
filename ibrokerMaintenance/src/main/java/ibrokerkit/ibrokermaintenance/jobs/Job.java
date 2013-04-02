package ibrokerkit.ibrokermaintenance.jobs;

public interface Job {

	public void args(String[] args);
	public void run() throws Exception;
}
