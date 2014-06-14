package com.fima.cardsui.views;

import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.R;
import com.fima.cardsui.objects.RecyclableCard;

public class MyCard extends RecyclableCard {
    String description = "";

    public MyCard(String title){
        super(title);
    }

    public MyCard(String title, String description){
        super(title);
        this.description = description;
    }

    @Override
    protected int getCardLayoutId() {
        return R.layout.card_ex;
    }

    @Override
    protected void applyTo(View convertView) {
        ((TextView) convertView.findViewById(R.id.title)).setText(title);
        if (!this.description.equals(""))
            ((TextView) convertView.findViewById(R.id.description)).setText(this.description);
    }

}