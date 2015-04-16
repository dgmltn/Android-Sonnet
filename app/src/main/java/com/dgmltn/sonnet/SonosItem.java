package com.dgmltn.sonnet;

/**
 * Created by doug on 3/29/15.
 */
public class SonosItem {
	String id;
	String title;
	String uri;
	String albumArtUri;
	String creator;
	String album;
	String parentId;
	boolean restricted;
	String upnpClass;

	public SonosItem() {
	}

	public String generateMetadata() {
		return
			"<DIDL-Lite xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:r=\"urn:schemas-rinconnetworks-com:metadata-1-0/\" xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\">"
				+ "<item id=\"" + id + "\" parentID=\"" + parentId + "\" restricted=\"" + (restricted ? "true"
				: "false") + "\">"
				+ "<dc:title>" + title + "</dc:title>"
				+ "<upnp:class>" + upnpClass + "</upnp:class>"
				+ "<desc id=\"cdudn\" nameSpace=\"urn:schemas-rinconnetworks-com:metadata-1-0/\">RINCON_AssociatedZPUDN</desc>"
				+ "</item>"
				+ "</DIDL-Lite>";
	}
}
