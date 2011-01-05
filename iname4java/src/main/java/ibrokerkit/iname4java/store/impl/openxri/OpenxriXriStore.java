package ibrokerkit.iname4java.store.impl.openxri;

import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriConstants;
import ibrokerkit.iname4java.store.XriData;
import ibrokerkit.iname4java.store.XriStore;
import ibrokerkit.iname4java.store.XriStoreException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxri.GCSAuthority;
import org.openxri.exceptions.StoreException;
import org.openxri.store.Authority;
import org.openxri.store.Store;
import org.openxri.store.StoreAttributable;
import org.openxri.store.StoreBetterLookup;
import org.openxri.store.StoreStatistics;
import org.openxri.store.SubSegment;
import org.openxri.xml.XRD;

/**
 * The OpenxriXriStore is used for creating, retrieving and managing i-names in OpenXRI.
 */
public class OpenxriXriStore implements XriStore {

	private static Log log = LogFactory.getLog(OpenxriXriStore.class.getName());

	protected Store openxriStore;

	public OpenxriXriStore(Store openxriStore) {

		this.openxriStore = openxriStore;
	}

	public Store getStore() {

		return(this.openxriStore);
	}

	public boolean existsXri(Xri parentXri, String localName) throws XriStoreException {

		if (parentXri != null && ! (parentXri instanceof OpenxriXri)) throw new IllegalArgumentException("XRI " + parentXri.toString() + " is not from this store.");

		// check if subsegment exists already

		SubSegment subSegment;

		try {

			if (parentXri == null) {

				subSegment = this.openxriStore.findRootSubSegment(localName);
			} else {

				Authority parentAuthority = ((OpenxriXri) parentXri).getAuthority();
				subSegment = this.openxriStore.findSubSegment(parentAuthority, localName);
			}
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot check subsegment " + localName + ": " + ex.getMessage(), ex);
		}

		return(subSegment != null);
	}

	public Xri registerXri(Xri parentXri, String localName, XriData xriData, int years) throws XriStoreException {

		if (parentXri != null && ! (parentXri instanceof OpenxriXri)) throw new IllegalArgumentException("XRI " + parentXri.toString() + " is not valid.");
		if (localName == null) throw new NullPointerException("localName is null");
		if (xriData == null || ! xriData.isCompleteForRegister()) throw new IllegalArgumentException("Incomplete XRI data.");
		if (! (xriData instanceof OpenxriXriData)) throw new IllegalArgumentException("Invalid XRI data.");

		OpenxriXriData openxriXriData = ((OpenxriXriData) xriData);

		// register new subsegment

		SubSegment subSegment;
		Authority authority;

		try {

			Authority parentAuthority = parentXri == null ? null : ((OpenxriXri) parentXri).getAuthority();

			XRD xrd = openxriXriData.getXrd();
			if (xrd == null) xrd = new XRD();

			if (parentAuthority == null) {

				subSegment = this.openxriStore.createRootSubSegment(localName, xrd);
			} else {

				subSegment = this.openxriStore.registerSubsegment(parentAuthority, localName, xrd);
			}

			authority = this.openxriStore.getSubSegmentAuthority(subSegment);

			// set timestamp and user identifier

			String date = Long.toString(new Date().getTime());

			Map<String, String> subSegmentAttributes = ((StoreAttributable) this.openxriStore).getSubSegmentAttributes(subSegment);
			subSegmentAttributes.put(XriConstants.ATTRIBUTE_KEY_DATE, date);
			((StoreAttributable) this.openxriStore).setSubSegmentAttributes(subSegment, subSegmentAttributes);
			((StoreAttributable) this.openxriStore).setSubSegmentIndex(subSegment, openxriXriData.getUserIdentifier());

			Map<String, String> authorityAttributes = ((StoreAttributable) this.openxriStore).getAuthorityAttributes(authority);
			authorityAttributes.put(XriConstants.ATTRIBUTE_KEY_DATE, date);
			((StoreAttributable) this.openxriStore).setAuthorityAttributes(authority, authorityAttributes);
			((StoreAttributable) this.openxriStore).setAuthorityIndex(authority, openxriXriData.getUserIdentifier());
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot register subsegment " + localName + ": " + ex.getMessage(), ex);
		}

		// done

		return(new OpenxriXri(this.openxriStore, subSegment, authority));
	}

	public Xri registerXriSynonym(Xri parentXri, String localName, Xri xri, XriData xriData, int years) throws XriStoreException {

		if (parentXri != null && ! (parentXri instanceof OpenxriXri)) throw new IllegalArgumentException("XRI " + parentXri.toString() + " is not from this store.");
		if (xri != null && ! (xri instanceof OpenxriXri)) throw new IllegalArgumentException("XRI " + xri.toString() + " is not from this store.");
		if (xriData == null || ! xriData.isCompleteForRegister()) throw new IllegalArgumentException("Incomplete XRI data.");
		if (! (xriData instanceof OpenxriXriData)) throw new IllegalArgumentException("Invalid XRI data.");

		OpenxriXriData openxriXriData = ((OpenxriXriData) xriData);

		// register new subsegment

		SubSegment subSegment;
		Authority authority;

		try {

			Authority parentAuthority = parentXri == null ? null : ((OpenxriXri) parentXri).getAuthority();
			authority = xri == null ? null : ((OpenxriXri) xri).getAuthority();

			if (authority == null) throw new NullPointerException("authority is null.");

			subSegment = this.openxriStore.registerSubsegment(parentAuthority, localName, authority);

			// set timestamp and user identifier

			String date = Long.toString(new Date().getTime());

			Map<String, String> subSegmentAttributes = ((StoreAttributable) this.openxriStore).getSubSegmentAttributes(subSegment);
			subSegmentAttributes.put(XriConstants.ATTRIBUTE_KEY_DATE, date);
			((StoreAttributable) this.openxriStore).setSubSegmentAttributes(subSegment, subSegmentAttributes);
			((StoreAttributable) this.openxriStore).setSubSegmentIndex(subSegment, openxriXriData.getUserIdentifier());
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot register subsegment " + localName + ": " + ex.getMessage(), ex);
		}

		// done

		return(new OpenxriXri(this.openxriStore, subSegment, authority));
	}

	public Xri transferAuthorityInRequest(String localName, XriData xriData) throws XriStoreException {

		if (localName == null) throw new NullPointerException("localName is null");
		if (xriData == null || ! xriData.isCompleteForTransfer()) throw new IllegalArgumentException("Incomplete XRI data.");
		if (! (xriData instanceof OpenxriXriData)) throw new IllegalArgumentException("Invalid XRI data.");

		OpenxriXriData openxriXriData = ((OpenxriXriData) xriData);

		// register new subsegment

		SubSegment subSegment;
		Authority authority;

		try {

			subSegment = this.openxriStore.createRootSubSegment(localName, new XRD());

			authority = this.openxriStore.getSubSegmentAuthority(subSegment);

			// set timestamp and user identifier

			String date = Long.toString(new Date().getTime());

			Map<String, String> subSegmentAttributes = ((StoreAttributable) this.openxriStore).getSubSegmentAttributes(subSegment);
			subSegmentAttributes.put(XriConstants.ATTRIBUTE_KEY_DATE, date);
			((StoreAttributable) this.openxriStore).setSubSegmentAttributes(subSegment, subSegmentAttributes);
			((StoreAttributable) this.openxriStore).setSubSegmentIndex(subSegment, openxriXriData.getUserIdentifier());

			Map<String, String> authorityAttributes = ((StoreAttributable) this.openxriStore).getAuthorityAttributes(authority);
			authorityAttributes.put(XriConstants.ATTRIBUTE_KEY_DATE, date);
			((StoreAttributable) this.openxriStore).setAuthorityAttributes(authority, authorityAttributes);
			((StoreAttributable) this.openxriStore).setAuthorityIndex(authority, openxriXriData.getUserIdentifier());
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot register subsegment " + localName + ": " + ex.getMessage(), ex);
		}

		// done

		return(new OpenxriXri(this.openxriStore, subSegment, authority));
	}

	public void transferAuthorityInComplete(Xri xri) throws XriStoreException {

		if (xri == null) throw new NullPointerException();
	}

	public void transferAuthorityInCanceled(Xri xri) throws XriStoreException {

		if (xri == null) throw new NullPointerException();

		log.debug("Deleting all authority subsegments.");

		try {

			SubSegment[] subSegments = ((StoreBetterLookup) this.openxriStore).getAuthoritySubSegments(((OpenxriXri) xri).getAuthority());

			for (SubSegment subSegment : subSegments) this.openxriStore.releaseSubSegment(subSegment);
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot delete subsegments or authority: " + ex.getMessage(), ex);
		}
	}

	public void transferAuthorityOutApprove(Xri xri, String token) throws XriStoreException {

		if (xri == null) throw new NullPointerException();
	}

	public void transferAuthorityOutReject(Xri xri, String token) throws XriStoreException {

		if (xri == null) throw new NullPointerException();
	}

	public void transferAuthorityOutComplete(Xri xri) throws XriStoreException {

		if (xri == null) throw new NullPointerException();

		log.debug("Deleting all authority subsegments.");

		try {

			SubSegment[] subSegments = ((StoreBetterLookup) this.openxriStore).getAuthoritySubSegments(((OpenxriXri) xri).getAuthority());

			for (SubSegment subSegment : subSegments) this.openxriStore.releaseSubSegment(subSegment);
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot delete subsegments or authority: " + ex.getMessage(), ex);
		}
	}

	public void transferAuthorityOutCanceled(Xri xri) throws XriStoreException {

		if (xri == null) throw new NullPointerException();
	}

	public Xri transferXriInRequest(String localName, XriData xriData) throws XriStoreException {

		throw new XriStoreException("Transfer IN for individual i-names not currently supported (only for whole authority).");
	}

	public void transferXriInComplete(Xri xri) throws XriStoreException {

		if (xri == null) throw new NullPointerException();
	}

	public void transferXriInCanceled(Xri xri) throws XriStoreException {

		if (xri == null) throw new NullPointerException();

		log.debug("Deleting i-name subsegment.");

		try {

			SubSegment subSegment = ((OpenxriXri) xri).getSubSegment();

			this.openxriStore.releaseSubSegment(subSegment);
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot delete subsegment: " + ex.getMessage(), ex);
		}
	}

	public void transferXriOutApprove(Xri xri, String token) throws XriStoreException {

		if (xri == null) throw new NullPointerException();
	}

	public void transferXriOutReject(Xri xri, String token) throws XriStoreException {

		if (xri == null) throw new NullPointerException();
	}

	public void transferXriOutComplete(Xri xri) throws XriStoreException {

		if (xri == null) throw new NullPointerException();

		log.debug("Deleting i-name subsegment.");

		try {

			SubSegment subSegment = ((OpenxriXri) xri).getSubSegment();

			this.openxriStore.releaseSubSegment(subSegment);
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot delete subsegment: " + ex.getMessage(), ex);
		}
	}

	public void transferXriOutCanceled(Xri xri) throws XriStoreException {

		if (xri == null) throw new NullPointerException();
	}

	public Calendar renewXri(Xri xri, int years) throws XriStoreException {

		if (xri == null) throw new NullPointerException();

		((OpenxriXri) xri).expirationDate = null;

		return null;
	}

	public void deleteXri(Xri xri) throws XriStoreException {

		log.debug("Deleting subsegment.");

		try {

			SubSegment subSegment = ((OpenxriXri) xri).getSubSegment();

			this.openxriStore.releaseSubSegment(subSegment);
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot delete subsegment: " + ex.getMessage(), ex);
		}
	}

	public void deleteAuthority(Xri xri) throws XriStoreException {

		log.debug("Deleting all authority subsegments.");

		try {

			SubSegment[] subSegments = ((StoreBetterLookup) this.openxriStore).getAuthoritySubSegments(((OpenxriXri) xri).getAuthority());

			for (SubSegment subSegment : subSegments) this.openxriStore.releaseSubSegment(subSegment);
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot delete subsegments or authority: " + ex.getMessage(), ex);
		}
	}

	public List<Xri> listXris() throws XriStoreException {

		log.debug("Listing subsegments.");

		List<Xri> inames = new ArrayList<Xri> ();
		SubSegment[] subSegments;

		try {

			subSegments = ((StoreBetterLookup) this.openxriStore).listSubSegments();
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot list subsegments: " + ex.getMessage(), ex);
		}

		for (SubSegment subSegment : subSegments) {

			if (subSegment.getName().startsWith("@!") || subSegment.getName().startsWith("=!")) continue;
			inames.add(new OpenxriXri(this.openxriStore, subSegment));
		}

		return(inames);
	}

	public List<Xri> listRootXris() throws XriStoreException {

		log.debug("Listing root subsegments.");

		List<Xri> rootInames = new ArrayList<Xri> ();
		SubSegment[] subSegments;

		try {

			subSegments = this.openxriStore.listRootSubSegments();
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot list root subsegments: " + ex.getMessage(), ex);
		}

		for (SubSegment subSegment : subSegments) {

			if (subSegment.getName().startsWith("@!") || subSegment.getName().startsWith("=!")) continue;
			rootInames.add(new OpenxriXri(this.openxriStore, subSegment));
		}

		return(rootInames);
	}

	public List<Xri> listUserXris(String userIdentifier) throws XriStoreException {

		log.debug("Listing user subsegments: " + userIdentifier + ".");

		List<Xri> userInames = new ArrayList<Xri> ();
		SubSegment[] subSegments;

		try {

			subSegments = ((StoreAttributable) this.openxriStore).listSubSegmentsByIndex(userIdentifier);
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot list user subsegments: " + ex.getMessage(), ex);
		}

		for (SubSegment subSegment : subSegments) {

			if (subSegment.getName().startsWith("@!") || subSegment.getName().startsWith("=!")) continue;
			userInames.add(new OpenxriXri(this.openxriStore, subSegment));
		}

		return(userInames);
	}

	public Xri findXri(String xri) throws XriStoreException {

		Authority authority = null;
		SubSegment subSegment = null;

		try {

			authority = this.openxriStore.localLookup(new GCSAuthority(xri));
			if (authority == null) return(null);

			SubSegment[] subSegments = ((StoreBetterLookup) this.openxriStore).getAuthoritySubSegments(authority);
			for (int i=0; i<subSegments.length; i++) if (xri.toLowerCase().endsWith(subSegments[i].getName().toLowerCase())) subSegment = subSegments[i];
			if (subSegment == null) return(null);
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot find user subsegments: " + ex.getMessage(), ex);
		}

		return(new OpenxriXri(this.openxriStore, subSegment, authority));
	}

	public Xri findXriByGrsAuthorityId(String grsAuthorityId) throws XriStoreException {

		Authority authority;
		SubSegment subSegment;

		try {

			Authority[] authorities = ((StoreAttributable) this.openxriStore).listAuthoritiesByAttributeValue(XriConstants.ATTRIBUTE_GRS_AUTHORITYID, grsAuthorityId);
			if (authorities == null || authorities.length < 1) return(null);

			authority = authorities[0];

			SubSegment[] subSegments = ((StoreBetterLookup) this.openxriStore).getAuthoritySubSegments(authority);
			if (subSegments == null || subSegments.length < 1) return(null);

			subSegment = subSegments[0];
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot find in OpenXRI " + grsAuthorityId + ": " + ex.getMessage(), ex);
		}

		return(new OpenxriXri(this.openxriStore, subSegment, authority));
	}

	public String findUserIdentifier(String xri) throws XriStoreException {

		String userIdentifier;

		try {

			Authority authority = this.openxriStore.localLookup(new GCSAuthority(xri));
			if (authority == null) return(null);

			userIdentifier = ((StoreAttributable) this.openxriStore).getAuthorityIndex(authority);
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot list user subsegments: " + ex.getMessage(), ex);
		}

		return(userIdentifier);
	}

	public long getAuthorityCount() throws XriStoreException {

		log.debug("Counting i-name authorities.");

		Long authorityCount;

		try {

			authorityCount = ((StoreStatistics) this.openxriStore).getAuthorityCount();
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot count i-name authorities: " + ex.getMessage(), ex);
		}

		return(authorityCount == null ? -1 : authorityCount.longValue());
	}

	public long getXriCount() throws XriStoreException {

		log.debug("Counting i-names.");

		Long xriCount;

		try {

			xriCount = ((StoreStatistics) this.openxriStore).getSubSegmentCount();
		} catch (StoreException ex) {

			log.error(ex);
			throw new XriStoreException("Cannot count i-names: " + ex.getMessage(), ex);
		}

		return(xriCount == null ? -1 : xriCount.longValue());
	}
}
