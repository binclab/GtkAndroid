package org.gtk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class GtkNotebook extends LinearLayout implements GtkWidget {
    private final LinearLayout pageTitles;
    private final FrameLayout pageFrame;
    private final LinearLayout space, barColour;
    private int currentPage = 0, previousPage = 0;
    private float startX = 0f;

    @SuppressLint("ClickableViewAccessibility")
    public GtkNotebook(Context context, AttributeSet attributes) {
        super(context, attributes);
        HorizontalScrollView scrollView = new HorizontalScrollView(context);
        barColour = new LinearLayout(context);
        pageTitles = new LinearLayout(context);
        pageFrame = new FrameLayout(context);
        space = new LinearLayout(context);
        setOrientation(VERTICAL);
        pageTitles.setGravity(Gravity.START);
        addView(scrollView, new LayoutParams(-1, -2));
        addView(pageFrame, new LayoutParams(-1, -1));
        scrollView.addView(pageTitles, new LayoutParams(-1, -2));
        scrollView.setFillViewport(true);
        pageTitles.addView(space, new LayoutParams(-1, 88));
        space.setPadding(2, 0, 2, 2);
        space.addView(barColour, new LayoutParams(-1, -1));
    }

    public GtkNotebook(Context context) {
        this(context, null);
    }

    public void setBarColour(int colour) {
        barColour.setBackgroundColor(colour);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = motionEvent.getX();
                break;
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() - startX < -100 && getCurrentPage() + 1 < getPageCount())
                    setCurrentPage(getCurrentPage() + 1);
                else if (motionEvent.getX() - startX > 100 && getCurrentPage() - 1 >= 0)
                    setCurrentPage(getCurrentPage() - 1);
                break;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    public void appendPage(CharSequence title, View view) {
        Button tabLabel = new Button(getContext());
        LinearLayout labelColour = new LinearLayout(getContext());
        labelColour.setPadding(2, 0, 2, 2);
        labelColour.addView(tabLabel, new LayoutParams(-1, -1));
        pageTitles.addView(labelColour, new LayoutParams(304, 88));
        pageFrame.addView(view, new LayoutParams(-1, -1));
        tabLabel.setText(title);
        tabLabel.setBackgroundColor(Color.WHITE);
        tabLabel.setGravity(Gravity.CENTER);
        tabLabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int page = pageTitles.indexOfChild((View) tabLabel.getParent());
                setCurrentPage(page);
            }
        });
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                performClick();
                onTouchEvent(motionEvent);
                view.onTouchEvent(motionEvent);
                return true;
            }
        });
        view.setVisibility(INVISIBLE);
        space.bringToFront();
    }

    public void setCurrentPage(int page) {
        currentPage = page;
        if (pageTitles.getChildAt(currentPage) != null) {
            pageFrame.getChildAt(previousPage).setVisibility(INVISIBLE);
            pageTitles.getChildAt(previousPage).setBackgroundColor(Color.WHITE);
            pageTitles.getChildAt(previousPage).setElevation(0);
            pageTitles.getChildAt(currentPage).setBackgroundColor(Color.parseColor("#bdb76b"));
            pageTitles.getChildAt(currentPage).setElevation(4);
            pageFrame.getChildAt(currentPage).setVisibility(VISIBLE);
            previousPage = page;
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageCount() {
        return pageFrame.getChildCount();
    }
}
