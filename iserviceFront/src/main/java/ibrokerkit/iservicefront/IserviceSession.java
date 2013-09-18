package ibrokerkit.iservicefront;

public interface IserviceSession {

	public String getUserIdentifier();
	public void loginUser(String userIdentifier);
	public void logoutUser();
	public boolean isLoggedIn();
}
