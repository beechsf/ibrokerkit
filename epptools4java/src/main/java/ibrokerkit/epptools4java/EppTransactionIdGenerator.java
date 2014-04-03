package ibrokerkit.epptools4java;

import java.util.Properties;

public class EppTransactionIdGenerator {

	private Properties properties;

	private int currentTransactionNumber;

	public EppTransactionIdGenerator(Properties properties) {

		this.properties = properties;

		this.currentTransactionNumber = 0;
	}

	/**
	 * Generate and return a new client transaction ID, consisting of:
	 * - our EPP username
	 * - a sequential number
	 * - our thread ID
	 * - a timestamp
	 */
	public synchronized String generateTransactionId() {

		this.currentTransactionNumber++;

		StringBuffer buffer = new StringBuffer();
		buffer.append("id-");
		buffer.append(this.properties.getProperty("epp-username") + "-");
		buffer.append(Long.toString(Thread.currentThread().getId()) + "-");
		buffer.append(Integer.toString(this.currentTransactionNumber) + "-");
		buffer.append(Long.toString(System.currentTimeMillis()));

		return buffer.toString();
	}
}
