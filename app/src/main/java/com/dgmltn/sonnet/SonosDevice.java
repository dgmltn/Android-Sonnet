package com.dgmltn.sonnet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.support.annotation.Nullable;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;


/**
 * Represents an interface with a specific Sonos system on the network.
 * If you have multiple Sonos systems in your house, you will have multiple
 * instances of this class.
 * <p/>
 * Created by doug on 3/23/15.
 * <p/>
 *
 * See: https://github.com/DjMomo/sonos/blob/master/sonos.class.php
 *
 */
public class SonosDevice implements Comparable {

	private static final String TAG = SonosDevice.class.getSimpleName();

	private URL mDescriptorUrl;

	private String mRoomName;
	private String mUuid;

	private int mPort = 1400;
	private transient final OkHttpClient mClient = new OkHttpClient();

	private SonosDevice() {
	}

	/**
	 * Creates and populates a SonosDevice based on its UPnPDevice descriptor. This uses
	 * networking and may return null.
	 *
	 * @param upnp
	 * @return
	 */
	@Nullable
	public static SonosDevice newInstance(UPnPDevice upnp) {
		// Perform a basic check first:
		if (!upnp.getServer().contains("Sonos")) {
			return null;
		}

		return newInstance(upnp.getLocation());
	}

	@Nullable
	public static SonosDevice newInstance(URL descriptor) {
		SonosDevice d = new SonosDevice();
		d.mDescriptorUrl = descriptor;
		d.mPort = descriptor.getPort();
		try {
			d.fillFromDescriptor();
		}
		catch (Exception e) {
			return null;
		}
		return d;
	}

	public static SonosDevice newInstance(String descriptor, String roomName, String uuid) {
		SonosDevice d = new SonosDevice();
		try {
			d.mDescriptorUrl = new URL(descriptor);
			d.mPort = d.mDescriptorUrl.getPort();
			d.mRoomName = roomName;
			d.mUuid = uuid;
		}
		catch (Exception e) {
			return null;
		}
		return d;
	}

	// Get more details about the sonos device
	private void fillFromDescriptor() throws Exception {
		Request request = new Request.Builder()
			.url(mDescriptorUrl)
			.build();

		Response response = mClient.newCall(request).execute();
		if (!response.isSuccessful()) {
			throw new IOException("Unexpected code " + response);
		}

		// TODO: revisit xml parsing
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(response.body().byteStream());
		XPath xPath = XPathFactory.newInstance().newXPath();

		mRoomName = xPath.compile("//roomName").evaluate(doc);
		String mdn = xPath.compile("//UDN").evaluate(doc);
		mUuid = mdn.toLowerCase().startsWith("uuid:") ? mdn.substring(5) : mdn;

		String manufacturer = xPath.compile("//manufacturer").evaluate(doc);
		if (!manufacturer.contains("Sonos")) {
			throw new IOException("Not a Sonos device: " + manufacturer);
		}

		Log.e(TAG, "SonosDevice.newInstance(\"" + mDescriptorUrl + "\", \"" + mRoomName + "\", \"" + mUuid + "\");");
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// Getters / Setters
	////////////////////////////////////////////////////////////////////////////////////////

	public String getRoomName() {
		return mRoomName;
	}

	public String getIPAddress() {
		return mDescriptorUrl.getHost();
	}

	@Override
	public String toString() {
		return getRoomName();
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// Meta / compound commands
	////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Get Sonos Playlists
	 */
	public Observable<SonosPlaylist> getPlaylists() {
		return browse("SQ:")
			.flatMap(new Func1<Response, Observable<String>>() {
				@Override
				public Observable<String> call(final Response response) {
					return parseResponseForResult(response);
				}
			})
			.flatMap(new Func1<String, Observable<SonosPlaylist>>() {
				@Override
				public Observable<SonosPlaylist> call(String s) {
					return parseResultForPlaylists(s);
				}
			});
	}

	/**
	 * Get items in a playlist
	 *
	 * @param playlist a list of SonosItem
	 * @return
	 */
	public Observable<SonosItem> getPlaylistItems(SonosPlaylist playlist) {
		return browse(playlist.id)
			.flatMap(new Func1<Response, Observable<String>>() {
				@Override
				public Observable<String> call(final Response response) {
					return parseResponseForResult(response);
				}
			})
			.flatMap(new Func1<String, Observable<SonosItem>>() {
				@Override
				public Observable<SonosItem> call(final String result) {
					return parseResultForItems(result);
				}
			});
	}

	/**
	 * Clears the queue and plays the item passed in. This won't work if the item is
	 * from a streaming service like Rdio or SoundCloud.
	 *
	 * @param item new track to play
	 */
	public Observable<Response> playSonosItemNow(final SonosItem item) {
		return becomeCoordinatorOfStandaloneGroup()
			.flatMap(new Func1<Response, Observable<Response>>() {
				@Override
				public Observable<Response> call(Response response) {
					return removeAllTracksFromQueue();
				}
			})
			.flatMap(new Func1<Response, Observable<Response>>() {
				@Override
				public Observable<Response> call(Response response) {
					return selectQueue();
				}
			})
			.flatMap(new Func1<Response, Observable<Response>>() {
				@Override
				public Observable<Response> call(Response response) {
					return addURIToQueue(item.uri, item.generateMetadata(), true);
				}
			})
			.flatMap(new Func1<Response, Observable<Response>>() {
				@Override
				public Observable<Response> call(Response response) {
					return play();
				}
			});
	}

	/**
	 * Loads the specified playlist and starts playing from the specified track number in that playlist.
	 * @param playlistUri
	 * @param trackIndex
	 * @return
	 */
	public Observable<Response> playPlaylistItemNow(final String playlistUri, final int trackIndex) {
		return becomeCoordinatorOfStandaloneGroup()
			.flatMap(new Func1<Response, Observable<Response>>() {
				@Override
				public Observable<Response> call(Response response) {
					return removeAllTracksFromQueue();
				}
			})
			.flatMap(new Func1<Response, Observable<Response>>() {
				@Override
				public Observable<Response> call(Response response) {
					return selectQueue();
				}
			})
			.flatMap(new Func1<Response, Observable<Response>>() {
				@Override
				public Observable<Response> call(Response response) {
					return addURIToQueue(playlistUri, "", true);
				}
			})
			.flatMap(new Func1<Response, Observable<Response>>() {
				@Override
				public Observable<Response> call(Response response) {
					return seekTrack(trackIndex);
				}
			})
			.flatMap(new Func1<Response, Observable<Response>>() {
				@Override
				public Observable<Response> call(Response response) {
					return play();
				}
			});
	}

	/**
	 * Get Sonos Favorites
	 */
	public Observable<String> getFavorites() {
		return browse("FV:2")
			.flatMap(new Func1<Response, Observable<String>>() {
				@Override
				public Observable<String> call(final Response response) {
					return parseResponseForResult(response);
				}
			});
	}

	public Observable<Response> playStream(String uri) {
		return setAVTransportURI(uri, "");
	}

	/**
	 * Sets this device to join up as a slave of the master device.
	 *
	 * @param master
	 * @return
	 */
	public Observable<Response> join(SonosDevice master) {
		String queueURI = "x-rincon:" + master.mUuid;
		return playStream(queueURI);
	}

	/**
	 * Sets this device's queued playlist (as opposed to a stream) as the music source.
	 *
	 * @return
	 */
	public Observable<Response> selectQueue() {
		String queueURI = "x-rincon-queue:" + mUuid + "#0";
		return playStream(queueURI);
	}

	public Observable<Response> seekTrack(int trackIndex) {
		String unit = "TRACK_NR";
		return seek(unit, Integer.toString(trackIndex));
	}

	public Observable<Response> seekTime(String time) {
		String unit = "REL_TIME";
		return seek(unit, time);
	}

	/**
	 * Returns this device's current queue/playlist
	 */
	public Observable<Response> getQueue() {
		return browse("Q:0");
	}

	public Observable<String> getVolumeString() {
		return getVolume()
			.flatMap(new Func1<Response, Observable<String>>() {
				@Override
				public Observable<String> call(Response response) {
					return parseResponseFor(response, "CurrentVolume");
				}
			});
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// Directly mapped upnp / sonos device commands
	////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns information about the currently playing track.
	 * @return
	 */
	public Observable<Response> getPositionInfo() {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "GetPositionInfo";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID><Channel>Master</Channel>";
		return upnp(url, service, action, args);
	}

	/**
	 * Plays/Resumes the current queue or stream for this device.
	 */
	public Observable<Response> play() {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "Play";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID><Speed>1</Speed>";
		return upnp(url, service, action, args);
	}

	private Observable<Response> setAVTransportURI(String uri, String metadata) {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "SetAVTransportURI";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID>"
			+ "<CurrentURI>" + uri + "</CurrentURI>"
			+ "<CurrentURIMetaData>" + metadata + "</CurrentURIMetaData>";
		return upnp(url, service, action, args);
	}

	/**
	 * pause
	 */
	public Observable<Response> pause() {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "Pause";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID>";
		return upnp(url, service, action, args);
	}

	/**
	 * Jumps to the current location in the current playlist.
	 * See also: seekTrack, seekTime
	 */
	public Observable<Response> seek(String unit, String target) {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "Seek";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID>"
			+ "<Unit>" + unit + "</Unit>"
			+ "<Target>" + target + "</Target>";
		return upnp(url, service, action, args);
	}

	/**
	 * next
	 */
	public Observable<Response> next() {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "Next";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID>";
		return upnp(url, service, action, args);
	}

	/**
	 * pause
	 */
	public Observable<Response> previous() {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "Previous";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID>";
		return upnp(url, service, action, args);
	}

	public Observable<Response> getVolume() {
		String url = "/MediaRenderer/RenderingControl/Control";
		String action = "GetVolume";
		String service = "urn:schemas-upnp-org:service:RenderingControl:1";
		String args = "<InstanceID>0</InstanceID><Channel>Master</Channel>";
		return upnp(url, service, action, args);
	}

	public Observable<Response> setVolume(String volume) {
		//TODO
		String url = "/MediaRenderer/RenderingControl/Control";
		String action = "SetVolume";
		String service = "urn:schemas-upnp-org:service:RenderingControl:1";
		String args = "<InstanceID>0</InstanceID><Channel>Master</Channel><DesiredVolume>" + volume + "</DesiredVolume>";
		return upnp(url, service, action, args);
	}

	/**
	 * Get Transport Info : get status about player
	 */
	public Observable<Response> getTransportInfo() {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "GetTransportInfo";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID>";
		return upnp(url, service, action, args);
	}

	/**
	 * Get Media Info : get informations about media
	 */
	public Observable<Response> getMediaInfo() {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "GetMediaInfo";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID>";
		return upnp(url, service, action, args);
	}

	/**
	 * Returns a playlist
	 * file:///jffs/settings/savedqueues.rsq#10
	 *
	 * @param objectID
	 * @return
	 */
	public Observable<Response> browse(String objectID) {
		String url = "/MediaServer/ContentDirectory/Control";
		String action = "Browse";
		String service = "urn:schemas-upnp-org:service:ContentDirectory:1";
		String args = "<ObjectID>" + objectID + "</ObjectID>"
			+ "<BrowseFlag>BrowseDirectChildren</BrowseFlag>"
			+ "<Filter></Filter>"
			+ "<StartingIndex>0</StartingIndex>"
			+ "<RequestedCount>100</RequestedCount>"
			+ "<SortCriteria></SortCriteria>";
		return upnp(url, service, action, args);
	}

	/**
	 * Remove a particular track from the queue.
	 */
	public Observable<Response> removeTrackFromQueue(int trackNumber) {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "RemoveTrackFromQueue";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID><ObjectID>Q:0/" + trackNumber + "</ObjectID>";
		return upnp(url, service, action, args);
	}

	/**
	 * Clear Queue
	 */
	public Observable<Response> removeAllTracksFromQueue() {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "RemoveAllTracksFromQueue";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID>";
		return upnp(url, service, action, args);
	}

	/**
	 * Add URI to Queue
	 *
	 * @param uri  track/radio URI
	 * @param next added next (=1) or end queue (=0)
	 */
	public Observable<Response> addURIToQueue(String uri, String metadata, boolean next) {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "AddURIToQueue";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";

		String escapedUri = Utils.xmlEnocode(uri);
		String escapedMetadata = Utils.xmlEnocode(metadata);

		String args = "<InstanceID>0</InstanceID>"
			+ "<EnqueuedURI>" + escapedUri + "</EnqueuedURI>"
			+ "<EnqueuedURIMetaData>" + escapedMetadata + "</EnqueuedURIMetaData>"
			+ "<DesiredFirstTrackNumberEnqueued>0</DesiredFirstTrackNumberEnqueued>"
			+ "<EnqueueAsNext>" + (next ? "1" : "0") + "</EnqueueAsNext>";

		return upnp(url, service, action, args);
	}

	/**
	 * Become Coordinator of Standalone Group (ungroup this device)
	 */
	public Observable<Response> becomeCoordinatorOfStandaloneGroup() {
		String url = "/MediaRenderer/AVTransport/Control";
		String action = "BecomeCoordinatorOfStandaloneGroup";
		String service = "urn:schemas-upnp-org:service:AVTransport:1";
		String args = "<InstanceID>0</InstanceID>";
		return upnp(url, service, action, args);
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// Response parsing / mapping
	////////////////////////////////////////////////////////////////////////////////////////

	private Observable<SonosPlaylist> parseResultForPlaylists(final String result) {
		return Observable.create(new Observable.OnSubscribe<SonosPlaylist>() {
			@Override
			public void call(Subscriber<? super SonosPlaylist> subscriber) {
				try {
					// TODO: revisit xml parsing
					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					factory.setNamespaceAware(true);
					XmlPullParser xpp = factory.newPullParser();
					xpp.setInput(new StringReader(result));

					int eventType = xpp.getEventType();
					SonosPlaylist playlist = null;
					String text = null;
					while (eventType != XmlPullParser.END_DOCUMENT) {
						switch (eventType) {
						case XmlPullParser.START_TAG:
							switch (xpp.getName()) {
							case "container":
								playlist = new SonosPlaylist();
								for (int i = 0; i < xpp.getAttributeCount(); i++) {
									switch (xpp.getAttributeName(i)) {
									case "id":
										playlist.id = xpp.getAttributeValue(i);
										break;
									case "parentID":
										playlist.parentId = xpp.getAttributeValue(i);
										break;
									case "restricted":
										playlist.restricted = xpp.getAttributeValue(i) == "true";
										break;
									}
								}
								break;
							}
							break;
						case XmlPullParser.END_TAG:
							if (playlist != null) {
								switch (xpp.getName()) {
								case "title":
									playlist.title = text;
									break;
								case "res":
									playlist.url = text;
									break;
								case "container":
									subscriber.onNext(playlist);
									break;
								}
								text = null;
							}
							break;
						case XmlPullParser.TEXT:
							text = xpp.getText();
							break;
						}
						eventType = xpp.next();
					}

					subscriber.onCompleted();
				}
				catch (Exception e) {
					e.printStackTrace();
					subscriber.onError(e);
				}
			}
		});
	}

	private Observable<SonosItem> parseResultForItems(final String result) {
		return Observable.create(new Observable.OnSubscribe<SonosItem>() {
			@Override
			public void call(Subscriber<? super SonosItem> subscriber) {
				try {
					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					factory.setNamespaceAware(true);
					XmlPullParser xpp = factory.newPullParser();
					xpp.setInput(new StringReader(result));

					int eventType = xpp.getEventType();
					SonosItem item = null;
					String text = null;
					while (eventType != XmlPullParser.END_DOCUMENT) {
						switch (eventType) {
						case XmlPullParser.START_TAG:
							switch (xpp.getName()) {
							case "item":
								item = new SonosItem();
								for (int i = 0; i < xpp.getAttributeCount(); i++) {
									switch (xpp.getAttributeName(i)) {
									case "id":
										item.id = xpp.getAttributeValue(i);
										break;
									case "parentID":
										item.parentId = xpp.getAttributeValue(i);
										break;
									case "restricted":
										item.restricted = xpp.getAttributeValue(i) == "true";
										break;
									}
								}
								break;
							}
							break;
						case XmlPullParser.END_TAG:
							if (item != null) {
								switch (xpp.getName()) {
								case "title":
									item.title = text;
									break;
								case "res":
									item.uri = text;
									break;
								case "albumArtURI":
									if (text.startsWith("/")) {
										text = mDescriptorUrl.getProtocol() + "://"
											+ mDescriptorUrl.getHost()
											+ ":" + mDescriptorUrl.getPort()
											+ text;
									}
									item.albumArtUri = text;
									break;
								case "class":
									item.upnpClass = text;
									break;
								case "creator":
									item.creator = text;
									break;
								case "album":
									item.album = text;
									break;
								case "item":
									subscriber.onNext(item);
									break;
								}
								text = null;
							}
							break;
						case XmlPullParser.TEXT:
							text = xpp.getText();
							break;
						}
						eventType = xpp.next();
					}

					subscriber.onCompleted();
				}
				catch (Exception e) {
					e.printStackTrace();
					subscriber.onError(e);
				}
			}
		});
	}

	/**
	 * Looks through an xml Response for the <Result>...</Result> inside it, and returns that as a String.
	 *
	 * @param response
	 * @return
	 */
	public Observable<String> parseResponseForResult(final Response response) {
		return this.parseResponseFor(response, "Result");
	}

	/**
	 * Looks through an xml Response for the <mytag>...</mytag> inside it, and returns that as a String.
	 *
	 * @param response
	 * @return
	 */
	public Observable<String> parseResponseFor(final Response response, final String tag) {
		return Observable.create(new Observable.OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				try {
					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					XmlPullParser xpp = factory.newPullParser();
					xpp.setInput(new InputStreamReader(response.body().byteStream()));

					int eventType = xpp.getEventType();
					String text = null;
					while (eventType != XmlPullParser.END_DOCUMENT) {
						switch (eventType) {
						case XmlPullParser.END_TAG:
							if (tag.equals(xpp.getName())) {
								Log.e(TAG, tag + " = " + text);
								subscriber.onNext(text);
							}
							text = null;
							break;
						case XmlPullParser.TEXT:
							text = xpp.getText();
							break;
						}
						eventType = xpp.next();
					}

					subscriber.onCompleted();
				}
				catch (Exception e) {
					e.printStackTrace();
					subscriber.onError(e);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// Low Level SOAP UPnP request
	////////////////////////////////////////////////////////////////////////////////////////

	private Observable<Response> upnp(String url, String service, String action, String args) {
		String body =
			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
				+ "<s:Body>"
				+ "<u:" + action + " xmlns:u=\"" + service + "\">"
				+ args
				+ "</u:" + action + ">"
				+ "</s:Body>"
				+ "</s:Envelope>";

		String postUrl = "http://" + getIPAddress() + ":" + mPort + url;

		Request request = new Request.Builder()
			.url(postUrl)
			.addHeader("Content-Type:", "text/xml")
			.addHeader("SOAPAction:", service + "#" + action)
			.post(RequestBody.create(MediaType.parse("text/xml"), body.toString()))
			.build();

		Log.e(TAG, "DOUG: request = " + body);

		return request(mClient, request);
	}

	// https://gist.github.com/paulcbetts/2274581f24ded7502011
	public static Observable<Response> request(final OkHttpClient client, final Request request) {
		return Observable.create(new Observable.OnSubscribe<Response>() {
			@Override
			public void call(final Subscriber<? super Response> subj) {
				final Call call = client.newCall(request);

				subj.add(Subscriptions.create(new Action0() {
					@Override
					public void call() {
						call.cancel();
					}
				}));

				call.enqueue(new Callback() {
					@Override
					public void onFailure(Request request, IOException e) {
						subj.onError(e);
					}

					@Override
					public void onResponse(Response response) throws IOException {
						Throwable error = getFailureExceptionOnBadStatus(response);
						if (error != null) {
							subj.onError(error);
							return;
						}

						subj.onNext(response);
						subj.onCompleted();
					}

				});

			}
		});
	}

	private static Throwable getFailureExceptionOnBadStatus(Response resp) {
		if (resp.code() < 399) {
			return null;
		}
		return new Exception(resp.toString());
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// Basic object stuff
	////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SonosDevice)) {
			return false;
		}
		SonosDevice that = (SonosDevice) o;
		return this.mUuid.equals(that.mUuid);
	}

	@Override
	public int hashCode() {
		return this.mUuid.hashCode();
	}

	@Override
	public int compareTo(Object another) {
		return this.mRoomName.compareToIgnoreCase(((SonosDevice)another).mRoomName);
	}
}
