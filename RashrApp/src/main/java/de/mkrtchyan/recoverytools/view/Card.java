package de.mkrtchyan.recoverytools.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

import de.mkrtchyan.recoverytools.R;

public class Card extends CardView {
    private AppCompatTextView mTitle;
    private AppCompatTextView mDescription;
    private AppCompatImageView mIcon;
    private String mData;

    public Card(Context context) {
        super(context);
        init(null, 0);
    }

    public Card(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public Card(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.card_layout, this);
        // Load attributes
        mTitle = findViewById(R.id.title);
        mDescription = findViewById(R.id.description);
        mIcon = findViewById(R.id.card_image);
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Card, defStyle, 0);

        setTitle(a.getString(R.styleable.Card_title));
        setDescription(a.getString(R.styleable.Card_description));
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.

        if (a.hasValue(R.styleable.Card_drawable)) {
            setIcon(a.getDrawable(R.styleable.Card_drawable));
        }
        if (a.hasValue(R.styleable.Card_data)) {
            setData(a.getString(R.styleable.Card_data));
        }

        if (a.hasValue(R.styleable.Card_titleColor)) {
            setTitleColor(a.getInteger(R.styleable.Card_titleColor, 0));
        }

        if (a.hasValue(R.styleable.Card_descriptionColor)) {
            setDescriptionColor(a.getInteger(R.styleable.Card_descriptionColor, 0));
        }

        if (a.hasValue(R.styleable.Card_backgroundColor)) {
            setBackgroundColor(a.getInteger(R.styleable.Card_backgroundColor, 0));
        }

        if (a.hasValue(R.styleable.Card_cardColor)) {
            setCardBackgroundColor(a.getInteger(R.styleable.Card_cardColor, 0));
        }

        a.recycle();
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setDescription(String description) {
        mDescription.setText(description);
    }

    public void setIcon(Drawable drawable) {
        mIcon.setImageDrawable(drawable);
    }

    public void setData(String data) {
        mData = data;
    }

    public String getData() {
        return mData;
    }

    public void setTitleColor(int fontColor) {
        mTitle.setTextColor(fontColor);
    }
    public void setDescriptionColor(int fontColor) {
        mDescription.setTextColor(fontColor);
    }
}
