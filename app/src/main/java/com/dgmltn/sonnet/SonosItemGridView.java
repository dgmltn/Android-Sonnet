package com.dgmltn.sonnet;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by doug on 4/16/15.
 */
public class SonosItemGridView extends RecyclerView {

	private SonosItemAdapter mAdapter;

	private Subscription mSubscription;

	public SonosItemGridView(Context context) {
		super(context);
		init(context);
	}

	public SonosItemGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SonosItemGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		setHasFixedSize(true);
		GridLayoutManager glm = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
		setLayoutManager(glm);
		setItemAnimator(new ExplodeItemAnimator());

		mAdapter = new SonosItemAdapter(context);
		setAdapter(mAdapter);
	}

	public void setItemClickListener(SonosItemAdapter.ItemClickListener listener) {
		mAdapter.setItemClickListener(listener);
	}

	public void populatePlaylist(SonosDevice device, SonosPlaylist playlist) {
		if (mSubscription != null && !mSubscription.isUnsubscribed()) {
			mSubscription.unsubscribe();
		}
		mAdapter.clear();
		mSubscription = device
			.getPlaylistItems(playlist)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Action1<SonosItem>() {
				@Override
				public void call(SonosItem item) {
					mAdapter.add(item);
				}
			});
	}

	private static class ExplodeItemAnimator extends RecyclerView.ItemAnimator {
		@Override
		public void runPendingAnimations() {

		}

		@Override
		public boolean animateRemove(RecyclerView.ViewHolder viewHolder) {
			return false;
		}

		@Override
		public boolean animateAdd(RecyclerView.ViewHolder viewHolder) {
			return false;
		}

		@Override
		public boolean animateMove(RecyclerView.ViewHolder viewHolder,
			int i, int i2, int i3, int i4) {
			return false;
		}

		@Override
		public boolean animateChange(final RecyclerView.ViewHolder viewHolder,
			RecyclerView.ViewHolder viewHolder2,
			int fromLeft, int fromTop, int toLeft, int toTop) {
			viewHolder.itemView.animate().scaleX(3f).scaleY(3f).alpha(0f).withEndAction(new Runnable() {
				@Override
				public void run() {
					dispatchChangeFinished(viewHolder, true);
				}
			}).start();
			return false;
		}

		@Override
		public void onChangeFinished(RecyclerView.ViewHolder viewHolder, boolean oldItem) {
			viewHolder.itemView.setVisibility(View.GONE);
			viewHolder.itemView.setAlpha(1f);
			viewHolder.itemView.setScaleX(1f);
			viewHolder.itemView.setScaleY(1f);
		}

		@Override
		public void endAnimation(RecyclerView.ViewHolder viewHolder) {
		}

		@Override
		public void endAnimations() {

		}

		@Override
		public boolean isRunning() {
			return false;
		}
	}
}
