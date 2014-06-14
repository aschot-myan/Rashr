package com.fima.cardsui;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.fima.cardsui.objects.AbstractCard;
import com.fima.cardsui.objects.CardStack;

public class StackAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<AbstractCard> mStacks;
    private boolean mSwipeable;

    public StackAdapter(Context context, ArrayList<AbstractCard> stacks,
                        boolean swipable) {
        mContext = context;
        mStacks = stacks;
        mSwipeable = swipable;

    }

    @Override
    public int getCount() {
        return mStacks.size();
    }

    @Override
    public CardStack getItem(int position) {
        return (CardStack) mStacks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CardStack stack = getItem(position);
        stack.setAdapter(this);
        stack.setPosition(position);

        // the CardStack can decide whether to use convertView or not
        convertView = stack.getView(mContext, convertView, mSwipeable);
        return convertView;
    }

    public void setItems(ArrayList<AbstractCard> stacks) {
        mStacks = stacks;
        notifyDataSetChanged();
    }

    public void setSwipeable(boolean b) {
        mSwipeable = b;
    }

    public void setItems(CardStack cardStack, int position) {
        mStacks.set(position, cardStack);
    }

}