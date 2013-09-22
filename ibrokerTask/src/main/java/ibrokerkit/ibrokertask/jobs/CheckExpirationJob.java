package ibrokerkit.ibrokertask.jobs;

import ibrokerkit.ibrokerstore.store.User;
import ibrokerkit.ibrokertask.Email;
import ibrokerkit.ibrokertask.IbrokerTask;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.impl.grs.GrsXri;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neulevel.epp.xri.EppXriName;
import com.neulevel.epp.xri.EppXriNumber;

public class CheckExpirationJob {

	public static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

	private static final Logger log = LoggerFactory.getLogger(CheckExpirationJob.class.getName());

	public CheckExpirationJob() {

	}

	public static void run(boolean act) throws Exception {

		log.info("Checking expiration dates.");

		// check for expiring i-names

		List<Xri> xris = IbrokerTask.xriStore.listXris();

		for (Xri xri : xris) {

			try {

				if (! (xri instanceof GrsXri)) continue;
				if (xri.getFullName().charAt(1) == '!') continue;

				// read i-name and i-number expiration

				String iname = xri.getLocalName();

				log.info("Attempting info on i-name " + iname);
				EppXriName eppXriName = IbrokerTask.eppTools.infoIname(iname.charAt(0), iname);
				if (eppXriName == null) throw new RuntimeException("I-Name not found");
				Calendar dateExpiredName = eppXriName.getDateExpired();
				long daysName = (dateExpiredName.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / MILLIS_PER_DAY;
				log.info(eppXriName.getIName() + " expires in " + daysName + " days.");

				String inumber = xri.getCanonicalID().getValue();

				log.info("Attempting info on i-number " + inumber);
				EppXriNumber eppXriNumber = IbrokerTask.eppTools.infoInumber(inumber.charAt(0), inumber);
				if (eppXriNumber == null) throw new RuntimeException("I-Number not found");
				Calendar dateExpiredNumber = eppXriNumber.getDateExpired();
				long daysNumber = (dateExpiredNumber.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / MILLIS_PER_DAY;
				log.info(eppXriNumber.getINumber() + " expires in " + daysNumber + " days.");

				// perform actions

				if (act) {

					// find out if i-names are expiring
					// - normal reminder if expiring soon
					// - expire reminder if past expiration date
					// - delete+notification if 30 days past expiration date

					boolean remind = false;
					boolean expire = false;
					boolean delete = false;

					String[] remindDaysStr = IbrokerTask.properties.getProperty("remind-days").split(" ");
					long[] remindDays = new long[remindDaysStr.length];
					for (int i=0; i<remindDays.length; i++) remindDays[i] = Long.parseLong(remindDaysStr[i]);

					String[] expireDaysStr = IbrokerTask.properties.getProperty("expire-days").split(" ");
					long[] expireDays = new long[expireDaysStr.length];
					for (int i=0; i<expireDays.length; i++) expireDays[i] = Long.parseLong(expireDaysStr[i]);

					for (int i=0; i<remindDays.length; i++) if (daysName == remindDays[i] || daysNumber == remindDays[i]) remind = true;
					for (int i=0; i<expireDays.length; i++) if (daysName == expireDays[i] || daysNumber == expireDays[i]) expire = true;
					if (daysName < -30 || daysNumber < -30) delete = true;

					// send e-mail

					if (remind) {

						String userIdentifier = IbrokerTask.xriStore.findUserIdentifier(iname);
						User user = IbrokerTask.ibrokerStore.findUser(userIdentifier);
						String to = user.getEmail();

						log.info("----------------------------");
						log.info("REMIND: " + iname + " in " + daysName + " days! Sending e-mail to " + to);
						log.info("----------------------------");

						Email email = new Email(
								IbrokerTask.properties.getProperty("email-subject-remind"), 
								IbrokerTask.properties.getProperty("email-from"), 
								to,
								IbrokerTask.properties.getProperty("email-server"));
						StringWriter writer = new StringWriter();
						StringBuffer buffer;

						VelocityEngine velocity = new VelocityEngine();
						velocity.init();
						VelocityContext context = new VelocityContext(IbrokerTask.properties);
						context.put("iname", iname);
						context.put("inumber", inumber);
						context.put("inamedays", daysName);
						context.put("inumberdays", daysNumber);
						Template template = velocity.getTemplate("email-remind.vm");
						template.merge(context, writer);
						buffer = writer.getBuffer();
						email.println(buffer.toString());
						email.send();
					}

					// send e-mail

					if (expire) {

						String userIdentifier = IbrokerTask.xriStore.findUserIdentifier(iname);
						User user = IbrokerTask.ibrokerStore.findUser(userIdentifier);
						String to = user.getEmail();

						log.info("----------------------------");
						log.info("EXPIRE: " + iname + " in " + daysName + " days! Sending e-mail to " + to);
						log.info("----------------------------");

						Email email = new Email(
								IbrokerTask.properties.getProperty("email-subject-expire"), 
								IbrokerTask.properties.getProperty("email-from"), 
								to,
								IbrokerTask.properties.getProperty("email-server"));
						StringWriter writer = new StringWriter();
						StringBuffer buffer;

						VelocityEngine velocity = new VelocityEngine();
						velocity.init();
						VelocityContext context = new VelocityContext(IbrokerTask.properties);
						context.put("iname", iname);
						context.put("inumber", inumber);
						context.put("inamedays", daysName);
						context.put("inumberdays", daysNumber);
						Template template = velocity.getTemplate("email-expire.vm");
						template.merge(context, writer);
						buffer = writer.getBuffer();
						email.println(buffer.toString());
						email.send();
					}

					// delete those that have expired

					if (delete) {

						String userIdentifier = IbrokerTask.xriStore.findUserIdentifier(iname);
						User user = IbrokerTask.ibrokerStore.findUser(userIdentifier);
						String to = user.getEmail();

						if (daysName < -30) IbrokerTask.xriStore.deleteXri(xri);
						if (daysNumber < -30) IbrokerTask.xriStore.deleteAuthority(xri);

						log.info("----------------------------");
						log.info("DELETE: " + iname + " in " + daysName + " days! Sending e-mail to " + to);
						log.info("----------------------------");

						Email email = new Email(
								IbrokerTask.properties.getProperty("email-subject-delete"), 
								IbrokerTask.properties.getProperty("email-from"), 
								to,
								IbrokerTask.properties.getProperty("email-server"));
						StringWriter writer = new StringWriter();
						StringBuffer buffer;

						VelocityEngine velocity = new VelocityEngine();
						velocity.init();
						VelocityContext context = new VelocityContext(IbrokerTask.properties);
						context.put("iname", iname);
						context.put("inumber", inumber);
						context.put("inamedays", daysName);
						context.put("inumberdays", daysNumber);
						Template template = velocity.getTemplate("email-delete.vm");
						template.merge(context, writer);
						buffer = writer.getBuffer();
						email.println(buffer.toString());
						email.send();
					}
				}
			} catch (Exception ex) {

				log.error("Exception while checking i-name: " + xri.getFullName(), ex);
				continue;
			}
		}

		log.info("Done checking expiration dates.");
	}
}
