package com.fima.cardsui.objects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * A card that can be recycled when scrolled off screen.
 */
public abstract class RecyclableCard extends Card {

    public RecyclableCard() {
        super();
    }

    public RecyclableCard(String title, int image) {
        super(title, image);
    }

    public RecyclableCard(String titlePlay, String description, int imageRes,
                          String titleColor, Boolean hasOverflow, Boolean isClickable) {
        super(titlePlay, description, imageRes, titleColor, hasOverflow, isClickable);
    }

    public RecyclableCard(String title, String desc, int image) {
        super(title, desc, image);
    }

    public RecyclableCard(String titlePlay, String description, String color,
                          String titleColor, Boolean hasOverflow, Boolean isClickable) {
        super(titlePlay, description, color, titleColor, hasOverflow, isClickable);
    }

    public RecyclableCard(String title, String desc) {
        super(title, desc);
    }

    public RecyclableCard(String title) {
        super(title);
    }

    /**
     * Set the values of child views.  The view will not be null and
     * is guaranteed to have the layout of the root element equal to
     * the layout resource ID from getCardLayoutId().
     * @param convertView non-null view to modify
     */
    protected abstract void applyTo(View convertView);

    /**
     * Get the R.layout ID of the root element of the content of the card.
     * This value will be used to inflate the card and check whether an
     * old card's View can be recycled.
     * @return layout ID of the card content
     */
    protected abstract int getCardLayoutId();

    @Override
    public View getCardContent(Context context) {
        View view = LayoutInflater.from(context).inflate(getCardLayoutId(), null);
        applyTo(view);
        return view;
    }

    @Override
    public boolean convert(View convertCardView) {
        View view = convertCardView.findViewById(getCardLayoutId());
        if (view == null) {
            return false;
        }

        applyTo(view);
        return true;
    }

}