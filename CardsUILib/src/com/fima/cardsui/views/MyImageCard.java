package com.fima.cardsui.views;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fima.cardsui.R;
import com.fima.cardsui.objects.RecyclableCard;

public class MyImageCard extends RecyclableCard {
    String description = "";

	public MyImageCard(String title, int image){
		super(title, image);
	}

    public MyImageCard(String title, int image, String description){
        super(title, image);
        this.description = description;
    }

	@Override
	protected int getCardLayoutId() {
		return R.layout.card_picture;
	}

	@Override
	protected void applyTo(View convertView) {
		((TextView) convertView.findViewById(R.id.title)).setText(title);
		((ImageView) convertView.findViewById(R.id.imageView1)).setImageResource(image);
        if (!this.description.equals(""))
            ((TextView) convertView.findViewById(R.id.description)).setText(this.description);
	}

}
