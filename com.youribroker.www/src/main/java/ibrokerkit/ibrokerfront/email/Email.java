package ibrokerkit.ibrokerfront.email;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.wicket.Application;

import sun.net.smtp.SmtpClient;


public class Email {

	private SmtpClient smtp;
	private PrintStream stream;

	public Email(String subject, String to) throws IOException {

		Properties properties = ((IbrokerApplication) Application.get()).getProperties();

		String from = properties.getProperty("email-from");
		String server = properties.getProperty("email-server");

		this.smtp = new SmtpClient(server);
		this.smtp.from(from);
		this.smtp.to(to);

		this.stream = this.smtp.startMessage();

		this.stream.println("Subject: " + subject);
		this.stream.println("From: " + from);
		this.stream.println("To: " + to);
		this.stream.println();
	}

	public void println(String line) {

		this.stream.println(line);
	}

	public void println() {

		this.stream.println();
	}

	public void send() throws IOException {

		this.smtp.closeServer();
	}
	
	public PrintStream getPrintStream() {
		
		return(this.stream);
	}
}
