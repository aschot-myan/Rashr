package com.fima.cardsui.views;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fima.cardsui.R;
import com.fima.cardsui.objects.RecyclableCard;

public class MyPlayCard extends RecyclableCard {

	int backgroundColor = 0;

	public MyPlayCard(String titlePlay, String description, String color,
			String titleColor, Boolean hasOverflow, Boolean isClickable) {
		super(titlePlay, description, color, titleColor, hasOverflow,
				isClickable);
	}

	public MyPlayCard(String titlePlay, String description, String color,
	                  String titleColor, Boolean hasOverflow, Boolean isClickable,
	                  int backgroundColor) {
		super(titlePlay, description, color, titleColor, hasOverflow,
				isClickable);
		this.backgroundColor = backgroundColor;

	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.card_play;
	}

	@Override
	protected void applyTo(View convertView) {
		((TextView) convertView.findViewById(R.id.title)).setText(titlePlay);
		((TextView) convertView.findViewById(R.id.title)).setTextColor(Color
				.parseColor(titleColor));
		((TextView) convertView.findViewById(R.id.description))
				.setText(description);
		convertView.findViewById(R.id.stripe).setBackgroundColor(Color.parseColor(color));

		if (isClickable)
			convertView.findViewById(R.id.contentLayout)
					.setBackgroundResource(R.drawable.selectable_background_cardbank);

		if (hasOverflow)
			convertView.findViewById(R.id.overflow).setVisibility(View.VISIBLE);
		else
			convertView.findViewById(R.id.overflow).setVisibility(View.GONE);

		if (backgroundColor != 0)
			convertView.setBackgroundColor(backgroundColor);
	}
}
