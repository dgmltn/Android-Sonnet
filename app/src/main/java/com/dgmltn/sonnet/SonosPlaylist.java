package com.dgmltn.sonnet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by doug on 3/27/15.
 */
public class SonosPlaylist {
	String id;
	String parentId;
	boolean restricted;
	String title;
	String url;
	transient List<SonosItem> items;

	public SonosPlaylist() {
	}

	public SonosPlaylist(String id, String title, String url) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.items = new ArrayList<>();
	}

	@Override
	public String toString() {
		return title;
	}
}
