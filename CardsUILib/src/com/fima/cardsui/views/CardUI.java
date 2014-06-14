package com.fima.cardsui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.fima.cardsui.R;
import com.fima.cardsui.StackAdapter;
import com.fima.cardsui.objects.AbstractCard;
import com.fima.cardsui.objects.Card;
import com.fima.cardsui.objects.CardStack;

import java.util.ArrayList;

public class CardUI extends FrameLayout {

    /**
     * Constants
     */

    private static final int STATE_ONSCREEN = 0;
    private static final int STATE_OFFSCREEN = 1;
    private static final int STATE_RETURNING = 2;
    protected int renderedCardsStacks = 0;
    protected int mScrollY;
    /**
     * *****************************
     * Fields
     * <p/>
     * ******************************
     */

    private ArrayList<AbstractCard> mStacks;
    private Context mContext;
    private ViewGroup mQuickReturnView;
    /**
     * The table layout to be used for multiple columns
     */
    private TableLayout mTableLayout;
    /**
     * The number of columns, 1 by default
     */
    private int mColumnNumber = 1;
    private View mPlaceholderView;
    private QuickReturnListView mListView;
    private int mMinRawY = 0;
    private int mState = STATE_ONSCREEN;
    private int mQuickReturnHeight;
    private int mCachedVerticalScrollRange;
    private boolean mSwipeable = false;
    private OnRenderedListener onRenderedListener;
    private StackAdapter mAdapter;
    private View mHeader;

    /**
     * Constructor
     */
    public CardUI(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //read the number of columns from the attributes
        mColumnNumber = attrs.getAttributeIntValue(null, "columnCount", 1);
        initData(context);
    }

    /**
     * Constructor
     */
    public CardUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        //read the number of columns from the attributes
        mColumnNumber = attrs.getAttributeIntValue(null, "columnCount", 1);
        initData(context);
    }

    /**
     * Constructor
     */
    public CardUI(Context context) {
        super(context);
        initData(context);
    }

    private void initData(Context context) {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        mStacks = new ArrayList<AbstractCard>();
        //inflate a different layout, depending on the number of columns
        if (mColumnNumber == 1) {
            inflater.inflate(R.layout.cards_view, this);
            // init observable scrollview
            mListView = (QuickReturnListView) findViewById(R.id.listView);
        } else {
            //initialize the mulitcolumn view
            inflater.inflate(R.layout.cards_view_multicolumn, this);
            mTableLayout = (TableLayout) findViewById(R.id.tableLayout);
        }
        // mListView.setCallbacks(this);

        mHeader = inflater.inflate(R.layout.header, null);
        mQuickReturnView = (ViewGroup) findViewById(R.id.sticky);
        mPlaceholderView = mHeader.findViewById(R.id.placeholder);

    }

    public void setSwipeable(boolean b) {
        mSwipeable = b;
    }

    public void setHeader(View header) {

        mPlaceholderView.setVisibility(View.VISIBLE);

        mListView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        mQuickReturnHeight = mQuickReturnView.getHeight();
                        mListView.computeScrollY();
                        mCachedVerticalScrollRange = mListView.getListHeight();

                    }
                });

        mListView.setOnScrollListener(new OnScrollListener() {
            @SuppressLint("NewApi")
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                mScrollY = 0;
                int translationY = 0;

                if (mListView.scrollYIsComputed()) {
                    mScrollY = mListView.getComputedScrollY();
                }

                int rawY = mPlaceholderView.getTop()
                        - Math.min(
                        mCachedVerticalScrollRange
                                - mListView.getHeight(), mScrollY);

                switch (mState) {
                    case STATE_OFFSCREEN:
                        if (rawY <= mMinRawY) {
                            mMinRawY = rawY;
                        } else {
                            mState = STATE_RETURNING;
                        }
                        translationY = rawY;
                        break;

                    case STATE_ONSCREEN:
                        if (rawY < -mQuickReturnHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                        translationY = rawY;
                        break;

                    case STATE_RETURNING:
                        translationY = (rawY - mMinRawY) - mQuickReturnHeight;
                        if (translationY > 0) {
                            translationY = 0;
                            mMinRawY = rawY - mQuickReturnHeight;
                        }

                        if (rawY > 0) {
                            mState = STATE_ONSCREEN;
                            translationY = rawY;
                        }

                        if (translationY < -mQuickReturnHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                        break;
                }

                /** this can be used if the build is below honeycomb **/
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
                    TranslateAnimation anim = new TranslateAnimation(0, 0,
                            translationY, translationY);
                    anim.setFillAfter(true);
                    anim.setDuration(0);
                    mQuickReturnView.startAnimation(anim);
                } else {
                    mQuickReturnView.setTranslationY(translationY);
                }

            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        if (header != null) {
            try {
                mQuickReturnView.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mQuickReturnView.addView(header);
        }

    }

    public void scrollToCard(int pos) {
        // int y = 0;
        try {
            // y = getY(pos);

            mListView.smoothScrollToPosition(pos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scrollToY(int y) {

        try {

            mListView.scrollTo(0, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public QuickReturnListView getScrollView() {
        return mListView;
    }

    public int getLastCardStackPosition() {

        return mStacks.size() - 1;
    }

    public void addSeparateCards(Card[] cards) {

        addSeparateCards(cards, false);

    }

    public void addSeparateCards(Card[] cards, boolean refresh) {

        for (int i = 0; i < cards.length; i++) {
            CardStack stack = new CardStack();
            stack.add(cards[i]);
            mStacks.add(stack);
        }

        if (refresh)
            refresh();
    }

    public void addCard(Card card) {

        addCard(card, false);

    }

    public void addCard(Card card, boolean refresh) {

        CardStack stack = new CardStack();
        stack.add(card);
        mStacks.add(stack);
        if (refresh)
            refresh();

    }

    public void addCardToLastStack(Card card) {
        addCardToLastStack(card, false);

    }

    public void addCardToLastStack(Card card, boolean refresh) {
        if (mStacks.isEmpty()) {
            addCard(card, refresh);
            return;
        }
        int lastItemPos = mStacks.size() - 1;
        CardStack cardStack = (CardStack) mStacks.get(lastItemPos);
        cardStack.add(card);
        mStacks.set(lastItemPos, cardStack);
        if (refresh)
            refresh();

    }

    public void addStack(CardStack stack) {
        addStack(stack, false);

    }

    public void addStack(CardStack stack, boolean refresh) {
        mStacks.add(stack);
        if (refresh)
            refresh();

    }

    //suppress this error message to be able to use spaces in higher api levels
    @SuppressLint("NewApi")
    public void refresh() {

        if (mAdapter == null) {
            mAdapter = new StackAdapter(mContext, mStacks, mSwipeable);
            if (mListView != null) {
                mListView.setAdapter(mAdapter);
            } else if (mTableLayout != null) {
                TableRow tr = null;
                for (int i = 0; i < mAdapter.getCount(); i += mColumnNumber) {
                    //add a new table row with the current context
                    tr = (TableRow) new TableRow(mTableLayout.getContext());
                    tr.setOrientation(TableRow.HORIZONTAL);
                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    //add as many cards as the number of columns indicates per row
                    for (int j = 0; j < mColumnNumber; j++) {
                        if (i + j < mAdapter.getCount()) {
                            View card = mAdapter.getView(i + j, null, tr);
                            if (card.getLayoutParams() != null) {
                                card.setLayoutParams(new TableRow.LayoutParams(card.getLayoutParams().width, card.getLayoutParams().height, 1f));
                            } else {
                                card.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                            }
                            tr.addView(card);
                        }
                    }
                    mTableLayout.addView(tr);
                }
                if (tr != null) {
                    //fill the empty space with spacers
                    for (int j = mAdapter.getCount() % mColumnNumber; j > 0; j--) {
                        View space = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            space = new Space(tr.getContext());
                        } else {
                            space = new View(tr.getContext());
                        }
                        space.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        tr.addView(space);
                    }
                }

            }
        } else {
            mAdapter.setSwipeable(mSwipeable); // in case swipeable changed;
            mAdapter.setItems(mStacks);

        }

    }

    public void clearCards() {
        mStacks = new ArrayList<AbstractCard>();
        renderedCardsStacks = 0;
        refresh();
    }

    public void setCurrentStackTitle(String title) {
        CardStack cardStack = (CardStack) mStacks
                .get(getLastCardStackPosition());
        cardStack.setTitle(title);

    }

    public OnRenderedListener getOnRenderedListener() {
        return onRenderedListener;
    }

    public void setOnRenderedListener(OnRenderedListener onRenderedListener) {
        this.onRenderedListener = onRenderedListener;
    }

    public interface OnRenderedListener {
        public void onRendered();
    }

}
