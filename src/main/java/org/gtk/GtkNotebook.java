package org.gtk;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import android.view.*;

public class GtkNotebook extends LinearLayout implements GtkWidget {
    private final LinearLayout pageTitles;
    private final FrameLayout pageFrame;
    private TouchListener touchListener = new TouchListener();
    private int currentPage = 0, previousPage = 0;

    /***
     * Set
     ***/
    public GtkNotebook(Context context, AttributeSet attributes) {
        super(context, attributes);
        pageTitles = new LinearLayout(context);
        pageFrame = new FrameLayout(context);
        pageTitles.setLayoutParams(new LayoutParams(-1, -2));
        setOrientation(LinearLayout.VERTICAL);
        addView(pageTitles);
        addView(pageFrame);
        setOnTouchListener(touchListener);
    }

    public GtkNotebook(Context context) {
        this(context, null);
    }

    public void appendPage(CharSequence title, View view) {
        Space space = new Space(getContext());
        final Button tabTitle = new Button(getContext());
        LinearLayout tabLayout = new LinearLayout(getContext());
        space.setLayoutParams(new LayoutParams(4, 100));
        space.setBackgroundColor(Color.LTGRAY);
        tabLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, 100, 1f));
        tabTitle.setLayoutParams(new LayoutParams(-1, 88));
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
        tabLayout.addView(tabTitle);
        tabLayout.addView(space);
        pageTitles.addView(tabLayout);
        pageFrame.addView(view);
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                onTouchEvent(motionEvent);
                view.onTouchEvent(motionEvent);
                return true;
            }
        });
        view.setVisibility(GONE);
    }

    public void setCurrentPage(int page) {
        currentPage = page;
        if (pageTitles.getChildAt(currentPage) != null) {
            pageFrame.getChildAt(previousPage).setVisibility(GONE);
            pageTitles.getChildAt(previousPage).setBackgroundColor(Color.WHITE);
            pageTitles.getChildAt(currentPage).setBackgroundColor(Color.YELLOW);
            pageFrame.getChildAt(currentPage).setVisibility(VISIBLE);
            previousPage = page;
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    private class TouchListener implements OnTouchListener {
        private float startX;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (view != null && motionEvent != null) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    startX = motionEvent.getX();
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (motionEvent.getX() - startX < -100 &&
                            getCurrentPage() + 1 <= pageTitles.getChildCount())
                        setCurrentPage(getCurrentPage() + 1);
                    else if (motionEvent.getX() - startX > 100 && getCurrentPage() - 1 >= 0)
                        setCurrentPage(getCurrentPage() - 1);
                }
                if (view.getId() != getId()) view.onTouchEvent(motionEvent);
            }
            return true;
        }


    }
}
