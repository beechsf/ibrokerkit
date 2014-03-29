package ibrokerkit.epptools4java;

import java.util.Properties;

public class EppTransactionIdGenerator {

	private Properties properties;

	private int currentTransactionNumber;
	private String lastTransactionId;

	public EppTransactionIdGenerator(Properties properties) {

		this.properties = properties;

		this.currentTransactionNumber = 0;
		this.lastTransactionId = null;
	}

	/**
	 * Generate and return a new client transaction ID, consisting of:
	 * - our EPP username
	 * - a sequential number
	 * - our thread ID
	 * - a timestamp
	 */
	public String generateTransactionId() {

		this.currentTransactionNumber++;

		StringBuffer buffer = new StringBuffer();
		buffer.append("id-");
		buffer.append(this.properties.getProperty("epp-username") + "-");
		buffer.append(Long.toString(Thread.currentThread().getId()) + "-");
		buffer.append(Integer.toString(this.currentTransactionNumber) + "-");
		buffer.append(Long.toString(System.currentTimeMillis()));

		this.lastTransactionId = buffer.toString();

		return this.lastTransactionId;
	}

	/**
	 * Returns the last client transaction ID the we generated.
	 */
	public String getLastTransactionId() {

		return this.lastTransactionId;
	}
}
