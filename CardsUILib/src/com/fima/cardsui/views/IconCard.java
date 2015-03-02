package com.fima.cardsui.views;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fima.cardsui.R;
import com.fima.cardsui.objects.CardColorScheme;
import com.fima.cardsui.objects.RecyclableCard;

public class IconCard extends RecyclableCard {
    String description = "";
    CardColorScheme mScheme;

	public IconCard(String title, int image){
		super(title, image);
	}

    public IconCard(String title, int image, CardColorScheme scheme){
        super(title, image);
        mScheme = scheme;
    }

    public IconCard(String title, int image, String description){
        super(title, image);
        this.description = description;
    }

	public IconCard(String title, int image, String description, CardColorScheme scheme){
		super(title, image);
		this.description = description;
		mScheme = scheme;
	}


	@Override
	protected int getCardLayoutId() {
		return R.layout.card_picture;
	}

	@Override
	protected void applyTo(View convertView) {
		((TextView) convertView.findViewById(R.id.title)).setText(title);
		if (mScheme != null) {
			((TextView) convertView.findViewById(R.id.title)).setTextColor(mScheme.getCardTextColor());
		}
		((ImageView) convertView.findViewById(R.id.imageView1)).setImageResource(image);
        if (!this.description.equals("")) {
	        ((TextView) convertView.findViewById(R.id.description)).setText(description);
	        if (mScheme != null)
	            ((TextView) convertView.findViewById(R.id.description)).setTextColor(mScheme.getCardTextColor());
        }
		if (mScheme != null)
			convertView.setBackgroundColor(mScheme.getCardBackgroundColor());
	}

}
