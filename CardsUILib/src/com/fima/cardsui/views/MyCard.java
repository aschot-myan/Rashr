package com.fima.cardsui.views;

import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.R;
import com.fima.cardsui.objects.RecyclableCard;

public class MyCard extends RecyclableCard {
    String description = "";
	int backgroundColor = 0;
	int fontColor = 0;

    public MyCard(String title){
        super(title);
    }

    public MyCard(String title, String description){
        super(title);
        this.description = description;
    }

	public MyCard(String title, String description, int backgroundColor){
		super(title);
		this.description = description;
		this.backgroundColor = backgroundColor;
	}
	public MyCard(String title, String description, int backgroundColor, int fontColor){
		super(title);
		this.description = description;
		this.backgroundColor = backgroundColor;
		this.fontColor = fontColor;
	}

    @Override
    protected int getCardLayoutId() {
        return R.layout.card_ex;
    }

    @Override
    protected void applyTo(View convertView) {
        ((TextView) convertView.findViewById(R.id.title)).setText(title);
	    if (fontColor != 0) {
		    ((TextView) convertView.findViewById(R.id.title)).setTextColor(fontColor);
	    }
        if (!description.equals("")) {
	        ((TextView) convertView.findViewById(R.id.description)).setText(description);
	        if (fontColor != 0)
		        ((TextView) convertView.findViewById(R.id.description)).setTextColor(fontColor);
        }

	    if (backgroundColor != 0)
		    convertView.setBackgroundColor(backgroundColor);
    }

}