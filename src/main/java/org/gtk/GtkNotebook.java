package org.gtk;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.Space;
import android.widget.TextView;

public class GtkNotebook extends LinearLayout implements GtkWidget {
    private final LinearLayout pageTitles;
    private final FrameLayout pageFrame;
    private final LinearLayout tabBar;
    private NotebookLabel firstPage = null;
    private int currentPage = 0, previousPage = 0;
    private float startX = 0f;

    @SuppressLint("ClickableViewAccessibility")
    public GtkNotebook(Context context, AttributeSet attributes) {
        super(context, attributes);
        HorizontalScrollView scrollView = new HorizontalScrollView(context);
        Button previousTab = new Button(context);
        Button nextTab = new Button(context);
        tabBar = new LinearLayout(context);
        pageTitles = new LinearLayout(context);
        pageFrame = new FrameLayout(context);
        setOrientation(VERTICAL);
        pageTitles.setGravity(Gravity.START);
        addView(tabBar, new LayoutParams(-1, -2));
        addView(pageFrame, new LayoutParams(-1, -1));
        tabBar.addView(previousTab, new LayoutParams(48, 88));
        //tabBar.addView(scrollView, new LayoutParams(-1, 88));
        tabBar.addView(nextTab, new LayoutParams(48, 88));
        scrollView.addView(pageTitles, new LayoutParams(-1, -2));
        scrollView.setFillViewport(true);
        //space.addView(barColour, new LayoutParams(-1, -1));
    }

    public GtkNotebook(Context context) {
        this(context, null);
    }

    public void setBarColour(int colour) {
        tabBar.setBackgroundColor(colour);
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
        Log.e("", String.valueOf(tabBar.getChildCount()));
        if (tabBar.getChildCount() == 3) {
            firstPage = new NotebookLabel(getContext(), title);
            tabBar.addView(firstPage, 0);
        } else {
            NotebookLabel tabLabel = new NotebookLabel(getContext(), title);
            pageTitles.addView(tabLabel, new LayoutParams(304, 88));
        }
        pageFrame.addView(view, new LayoutParams(-1, -1));
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
        NotebookLabel current = currentPage == 0 ? (NotebookLabel) tabBar.getChildAt(0) :
                (NotebookLabel) pageTitles.getChildAt(currentPage);
        NotebookLabel previous = previousPage == 0 ? (NotebookLabel) tabBar.getChildAt(0) :
                (NotebookLabel) pageTitles.getChildAt(previousPage);
        pageFrame.getChildAt(previousPage).setVisibility(INVISIBLE);
        if (previous != null) previous.selectTab(false);
        if (current != null) current.selectTab(true);
        pageFrame.getChildAt(currentPage).setVisibility(VISIBLE);
        previousPage = page;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageCount() {
        return pageFrame.getChildCount();
    }

    private class NotebookLabel extends LinearLayout {

        private final LinearLayout shade;
        private final int colour = Color.parseColor("#bdb76b");

        public NotebookLabel(Context context, CharSequence text) {
            super(context);
            Button label = new Button(context);
            shade = new LinearLayout(context);
            setOrientation(VERTICAL);
            label.setBackground(null);
            label.setText(text);
            label.setGravity(Gravity.CENTER);
            addView(label, new LayoutParams(304, 88));
            addView(shade, new LayoutParams(-1, 4));
            label.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    int page = view.getId() == firstPage.getId()? 0 :
                            pageTitles.indexOfChild((View) view.getParent()) + 1;
                    setCurrentPage(page);
                    selectTab(true);
                }
            });
        }

        public void selectTab(boolean select) {
            if (select) {
                shade.setBackgroundColor(colour);
                shade.setElevation(4);
            } else {
                shade.setBackgroundColor(Color.WHITE);
                shade.setElevation(0);
            }
        }
    }
}
