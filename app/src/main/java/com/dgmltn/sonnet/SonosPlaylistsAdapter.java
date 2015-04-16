package com.dgmltn.sonnet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by doug on 3/29/15.
 */
public class SonosPlaylistsAdapter extends ArrayAdapter<SonosPlaylist> {
	LayoutInflater inflater;

	public SonosPlaylistsAdapter(Context context) {
		super(context, R.layout.row_sonos_item);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void add(SonosPlaylist object) {
		//TODO: insert sorted
		super.add(object);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;
		if (view != null) {
			holder = (ViewHolder) view.getTag();
		}
		else {
			view = inflater.inflate(R.layout.row_sonos_playlist, parent, false);
			holder = new ViewHolder(view);
			view.setTag(holder);
		}

		SonosPlaylist playlist = getItem(position);
		if (holder.title != null) {
			holder.title.setText(playlist.title);
		}

		return view;
	}

	static class ViewHolder {
		@InjectView(R.id.title)
		TextView title;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}
}
