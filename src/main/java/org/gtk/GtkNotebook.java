package org.gtk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class GtkNotebook extends LinearLayout implements GtkWidget {
    private final LinearLayout pageTitles;
    private final FrameLayout pageFrame;
    private final LinearLayout tabBar;
    private final int colour = Color.parseColor("#bdb76b");
    private View currentView;
    private NotebookLabel firstLabel, currentLabel;
    private float startX = 0f;

    @SuppressLint("ClickableViewAccessibility")
    public GtkNotebook(Context context, AttributeSet attributes) {
        super(context, attributes);
        HorizontalScrollView scrollView = new HorizontalScrollView(context);
        ImageButton previousTab = new ImageButton(context);
        ImageButton nextTab = new ImageButton(context);
        tabBar = new LinearLayout(context);
        pageTitles = new LinearLayout(context);
        pageFrame = new FrameLayout(context);
        setOrientation(VERTICAL);
        pageTitles.setGravity(Gravity.START);
        addView(tabBar, new LayoutParams(-1, 88));
        addView(pageFrame, new LayoutParams(-1, -1));
        tabBar.addView(scrollView, new LayoutParams(-1, -1));
        scrollView.addView(pageTitles, new LayoutParams(-1, -2));
        scrollView.setFillViewport(true);
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
        if (tabBar.getChildCount() == 1) {
            firstLabel = new NotebookLabel(getContext(), title);
            currentLabel = firstLabel;
            currentView = view;
            tabBar.addView(firstLabel, 0);
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

    public void setCurrentPage(int i) {
        NotebookLabel current = i == 0 ? (NotebookLabel) tabBar.getChildAt(0) :
                (NotebookLabel) pageTitles.getChildAt(i - 1);
        currentView.setVisibility(INVISIBLE);
        currentLabel.selectTab(false);
        current.selectTab(true);
        currentLabel = current;
        currentView = pageFrame.getChildAt(i);
        currentView.setVisibility(VISIBLE);
    }

    public int getCurrentPage() {
        return pageFrame.indexOfChild(currentView);
    }

    public int getPageCount() {
        return pageFrame.getChildCount();
    }

    private class NotebookLabel extends LinearLayout {

        private final LinearLayout shade;

        public NotebookLabel(Context context, CharSequence text) {
            super(context);
            Button title = new Button(context);
            shade = new LinearLayout(context);
            setOrientation(VERTICAL);
            title.setBackground(null);
            title.setText(text);
            title.setGravity(Gravity.CENTER);
            shade.setBackgroundColor(Color.WHITE);
            addView(title, new LayoutParams(304, 84));
            addView(shade, new LayoutParams(-1, 4));
            title.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    View label = (View) view.getParent();
                    setCurrentPage(label == firstLabel ? 0 : pageTitles.indexOfChild(label) + 1);
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
