package com.fima.cardsui.views;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fima.cardsui.R;
import com.fima.cardsui.objects.RecyclableCard;

public class MyImageCard extends RecyclableCard {
    String description = "";
	int backgroundColor = 0;
	int fontColor = 0;

	public MyImageCard(String title, int image){
		super(title, image);
	}

    public MyImageCard(String title, int image, String description){
        super(title, image);
        this.description = description;
    }

	public MyImageCard(String title, int image, String description, int backgroundColor){
		super(title, image);
		this.description = description;
		this.backgroundColor = backgroundColor;
	}

	public MyImageCard(String title, int image, String description, int backgroundColor,
	                   int fontColor){
		super(title, image);
		this.description = description;
		this.backgroundColor = backgroundColor;
		this.fontColor = fontColor;
	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.card_picture;
	}

	@Override
	protected void applyTo(View convertView) {
		((TextView) convertView.findViewById(R.id.title)).setText(title);
		if (fontColor != 0) {
			((TextView) convertView.findViewById(R.id.title)).setTextColor(fontColor);
		}
		((ImageView) convertView.findViewById(R.id.imageView1)).setImageResource(image);
        if (!this.description.equals("")) {
	        ((TextView) convertView.findViewById(R.id.description)).setText(this.description);
	        if (fontColor != 0)
	            ((TextView) convertView.findViewById(R.id.description)).setTextColor(fontColor);
        }
		if (backgroundColor != 0)
			convertView.setBackgroundColor(backgroundColor);
	}

}
