package com.dgmltn.sonnet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by doug on 3/29/15.
 */
public class SonosItemAdapter extends ArrayAdapter<SonosItem> {
	LayoutInflater inflater;
	Picasso picasso;

	public SonosItemAdapter(Context context) {
		super(context, R.layout.row_sonos_item);
		inflater = LayoutInflater.from(context);
		picasso = Picasso.with(context);
		picasso.setIndicatorsEnabled(true);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;
		if (view != null) {
			holder = (ViewHolder) view.getTag();
		}
		else {
			view = inflater.inflate(R.layout.row_sonos_item, parent, false);
			holder = new ViewHolder(view);
			view.setTag(holder);
		}

		SonosItem item = getItem(position);
		if (holder.title != null) {
			holder.title.setText(item.title);
		}
		if (holder.art != null) {
			picasso.load(item.albumArtUri)
				.placeholder(R.drawable.album_placeholder)
				.into(holder.art);
		}

		return view;
	}

	static class ViewHolder {
		@InjectView(R.id.art)
		ImageView art;

		@InjectView(R.id.title)
		@Optional
		TextView title;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}
}
