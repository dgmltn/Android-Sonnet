package com.dgmltn.sonnet;

import java.util.ArrayList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * Created by doug on 3/29/15.
 */
public class SonosItemAdapter extends RecyclerView.Adapter<SonosItemAdapter.ViewHolder> {

	public interface ItemClickListener {
		public void onClick(SonosItem item, int position);
	}

	private LayoutInflater inflater;
	private Picasso picasso;
	private ArrayList<SonosItem> mItems;
	private ItemClickListener mListener;

	public SonosItemAdapter(Context context) {
		super();
		inflater = LayoutInflater.from(context);
		picasso = Picasso.with(context);
		picasso.setIndicatorsEnabled(true);
		mItems = new ArrayList<>();
	}

	public void setItemClickListener(ItemClickListener listener) {
		mListener = listener;
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	public SonosItem getItem(int position) {
		return mItems.get(position);
	}

	public void clear() {
		int count = mItems.size();
		mItems.clear();
		notifyItemRangeRemoved(0, count);
	}

	public void add(SonosItem item) {
		mItems.add(item);
		notifyItemInserted(mItems.size());
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
		return new ViewHolder(inflater.inflate(R.layout.grid_item_album_art, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		SonosItem item = getItem(position);
		if (holder.title != null) {
			holder.title.setText(item.title);
		}
		if (holder.art != null) {
			picasso.load(item.albumArtUri)
				.placeholder(R.drawable.album_placeholder)
				.into(holder.art);
		}
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		@InjectView(R.id.art)
		ImageView art;

		@InjectView(R.id.title)
		@Optional
		TextView title;

		public ViewHolder(View view) {
			super(view);
			ButterKnife.inject(this, view);
		}

		@OnClick(R.id.root)
		public void click(View view) {
			int position = getAdapterPosition();
			if (mListener != null) {
				mListener.onClick(mItems.get(position), position);
			}
		}
	}
}
