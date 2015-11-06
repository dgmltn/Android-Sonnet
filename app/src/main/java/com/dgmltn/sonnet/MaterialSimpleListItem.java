package com.dgmltn.sonnet;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import com.afollestad.materialdialogs.util.DialogUtils;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MaterialSimpleListItem<T> {

	private final Builder<T> mBuilder;

	private MaterialSimpleListItem(Builder builder) {
		mBuilder = builder;
	}

	public Drawable getIcon() {
		return mBuilder.mIcon;
	}

	public T getContent() {
		return mBuilder.mContent;
	}

	public int getIconPadding() {
		return mBuilder.mIconPadding;
	}

	@ColorInt
	public int getBackgroundColor() {
		return mBuilder.mBackgroundColor;
	}

	public static class Builder<T> {

		private final Context mContext;
		protected Drawable mIcon;
		protected T mContent;
		protected int mIconPadding;
		protected int mBackgroundColor;

		public Builder(Context context) {
			mContext = context;
			mBackgroundColor = Color.parseColor("#BCBCBC");
		}

		public Builder<T> icon(Drawable icon) {
			this.mIcon = icon;
			return this;
		}

		public Builder<T> icon(@DrawableRes int iconRes) {
			return icon(ContextCompat.getDrawable(mContext, iconRes));
		}

		public Builder<T> iconPadding(@IntRange(from = 0, to = Integer.MAX_VALUE) int padding) {
			this.mIconPadding = padding;
			return this;
		}

		public Builder<T> iconPaddingDp(@IntRange(from = 0, to = Integer.MAX_VALUE) int paddingDp) {
			this.mIconPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingDp,
				mContext.getResources().getDisplayMetrics());
			return this;
		}

		public Builder<T> iconPaddingRes(@DimenRes int paddingRes) {
			return iconPadding(mContext.getResources().getDimensionPixelSize(paddingRes));
		}

		public Builder<T> content(T content) {
			this.mContent = content;
			return this;
		}

		public Builder<T> backgroundColor(@ColorInt int color) {
			this.mBackgroundColor = color;
			return this;
		}

		public Builder<T> backgroundColorRes(@ColorRes int colorRes) {
			return backgroundColor(DialogUtils.getColor(mContext, colorRes));
		}

		public Builder<T> backgroundColorAttr(@AttrRes int colorAttr) {
			return backgroundColor(DialogUtils.resolveColor(mContext, colorAttr));
		}

		public MaterialSimpleListItem<T> build() {
			return new MaterialSimpleListItem<>(this);
		}
	}

	@Override
	public String toString() {
		if (getContent() != null) {
			return getContent().toString();
		}
		else {
			return "(no content)";
		}
	}
}
