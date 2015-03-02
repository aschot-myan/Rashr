package com.fima.cardsui.views;

import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.R;
import com.fima.cardsui.objects.CardColorScheme;
import com.fima.cardsui.objects.RecyclableCard;

public class SimpleCard extends RecyclableCard {
    String description = "";
    CardColorScheme mScheme = null;

    public SimpleCard(String title){
        super(title);
    }

    public SimpleCard(String title, CardColorScheme scheme){
        super(title);
        mScheme = scheme;
    }

    public SimpleCard(String title, String description){
        super(title);
        this.description = description;
    }

	public SimpleCard(String title, String description, CardColorScheme scheme){
		super(title);
		this.description = description;
        mScheme = scheme;
	}

    @Override
    protected int getCardLayoutId() {
        return R.layout.card_ex;
    }

    @Override
    protected void applyTo(View convertView) {
        ((TextView) convertView.findViewById(R.id.title)).setText(title);
	    if (mScheme != null) {
		    ((TextView) convertView.findViewById(R.id.title)).setTextColor(mScheme.getCardTextColor());
	    }
        if (!description.equals("")) {
	        ((TextView) convertView.findViewById(R.id.description)).setText(description);
	        if (mScheme != null)
		        ((TextView) convertView.findViewById(R.id.description)).setTextColor(mScheme.getCardTextColor());
        }

	    if (mScheme != null)
		    convertView.setBackgroundColor(mScheme.getCardBackgroundColor());
    }

}