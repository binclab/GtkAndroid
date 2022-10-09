package org.gtk;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Space;

public class GtkNotebook extends LinearLayout implements GtkWidget {
    private final LinearLayout pageTitles;
    private final FrameLayout pageFrame;
    private int currentPage = 0, previousPage = 0;
    private float startX = 0f;

    public GtkNotebook(Context context, AttributeSet attributes) {
        super(context, attributes);
        HorizontalScrollView scrollView = new HorizontalScrollView(context);
        pageTitles = new LinearLayout(context);
        pageFrame = new FrameLayout(context);
        setOrientation(LinearLayout.VERTICAL);
        addView(scrollView, new LayoutParams(-1, -2));
        addView(pageFrame, new LayoutParams(-1, -1));
        scrollView.addView(pageTitles, new LayoutParams(-1, -2));
        scrollView.setFillViewport(true);
    }

    public GtkNotebook(Context context) {
        this(context, null);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            startX = motionEvent.getX();
        else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            if (motionEvent.getX() - startX < -100 && getCurrentPage() + 1 < getPageCount())
                setCurrentPage(getCurrentPage() + 1);
            else if (motionEvent.getX() - startX > 100 && getCurrentPage() - 1 >= 0)
                setCurrentPage(getCurrentPage() - 1);

        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    public void appendPage(CharSequence title, View view) {
        Space space = new Space(getContext());
        Button tabTitle = new Button(getContext());
        LinearLayout tabLayout = new LinearLayout(getContext());
        tabLayout.addView(tabTitle, new LayoutParams(-1, 88));
        tabLayout.addView(space, new LayoutParams(4, 100));
        pageTitles.addView(tabLayout, new LinearLayout.LayoutParams(-1, 100, 1f));
        pageFrame.addView(view, new LayoutParams(-1, -1));
        space.setBackgroundColor(Color.LTGRAY);
        tabTitle.setText(title);
        tabTitle.setBackgroundColor(Color.WHITE);
        tabTitle.setGravity(Gravity.CENTER);
        tabTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int page = pageTitles.indexOfChild((View) tabTitle.getParent());
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
    }

    public void setCurrentPage(int page) {
        currentPage = page;
        if (pageTitles.getChildAt(currentPage) != null) {
            pageFrame.getChildAt(previousPage).setVisibility(INVISIBLE);
            pageTitles.getChildAt(previousPage).setBackgroundColor(Color.WHITE);
            pageTitles.getChildAt(currentPage).setBackgroundColor(Color.YELLOW);
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
