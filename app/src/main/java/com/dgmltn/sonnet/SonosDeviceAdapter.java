package com.dgmltn.sonnet;

import android.content.Context;
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
public class SonosDeviceAdapter extends ArrayAdapter<SonosDevice> {
	LayoutInflater inflater;

	public SonosDeviceAdapter(Context context) {
		super(context, R.layout.row_sonos_item);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void add(SonosDevice object) {
		for (int i = 0; i < getCount(); i++) {
			if (object.compareTo(getItem(i)) < 0) {
				super.insert(object, i);
				return;
			}
		}
		super.add(object);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;
		if (view != null) {
			holder = (ViewHolder) view.getTag();
		}
		else {
			view = inflater.inflate(R.layout.row_sonos_device, parent, false);
			holder = new ViewHolder(view);
			view.setTag(holder);
		}

		SonosDevice device = getItem(position);
		if (holder.title != null) {
			holder.title.setText(device.getRoomName());
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
