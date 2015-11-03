package com.dgmltn.sonnet;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

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
		public boolean animateDisappearance(ViewHolder viewHolder,
			ItemHolderInfo preLayoutInfo,
			ItemHolderInfo postLayoutInfo) {
			return false;
		}

		@Override
		public boolean animateAppearance(ViewHolder viewHolder,
			ItemHolderInfo preLayoutInfo,
			ItemHolderInfo postLayoutInfo) {
			return false;
		}

		@Override
		public boolean animatePersistence(ViewHolder viewHolder,
			ItemHolderInfo preLayoutInfo,
			ItemHolderInfo postLayoutInfo) {
			return false;
		}

		@Override
		public boolean animateChange(ViewHolder oldHolder, final ViewHolder newHolder,
			ItemHolderInfo preLayoutInfo,
			ItemHolderInfo postLayoutInfo) {
			return false;
		}

		@Override
		public void runPendingAnimations() {
		}

		@Override
		public void onAnimationFinished(ViewHolder viewHolder) {
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
