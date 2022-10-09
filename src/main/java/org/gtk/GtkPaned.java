package org.gtk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class GtkPaned extends FrameLayout implements GtkWidget {

    private final LinearLayout tint, child, handle;
    private final RelativeLayout pane;
    private boolean add = false;
    public final int duration = 200;

    @SuppressLint("ClickableViewAccessibility")
    public GtkPaned(Context context, AttributeSet attrs) {
        super(context, attrs);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(64, -1);
        LinearLayout border = new LinearLayout(context);
        setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        tint = new LinearLayout(context);
        handle = new LinearLayout(context);
        child = new LinearLayout(context);
        pane = new RelativeLayout(context);
        params.setMarginStart(-32);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        addView(tint, new FrameLayout.LayoutParams(-1, -1));
        addView(pane, new FrameLayout.LayoutParams(432, -1));
        pane.addView(child, new RelativeLayout.LayoutParams(400, -1));
        pane.addView(handle, params);
        handle.addView(border, new LinearLayout.LayoutParams(2, -1));
        tint.setBackgroundColor(Color.parseColor("#801C1A1A"));
        tint.setVisibility(INVISIBLE);
        pane.setTranslationX(-400f);
        child.setBackgroundColor(Color.WHITE);
        border.setBackgroundColor(Color.parseColor("#801C1A1A"));
        handle.setGravity(Gravity.CENTER_HORIZONTAL);
        handle.setOnTouchListener(new PaneMover());
        tint.setOnTouchListener(new PaneCloser());
        add = true;
    }

    @Override
    public void addView(View child) {
        Log.e("", add + " " + getChildCount());
        if (add && getChildCount() > 0) super.addView(child, 0);
        else super.addView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (add && getChildCount() > 0) super.addView(child, 0);
        else super.addView(child, params);
    }

    public GtkPaned(Context context) {
        this(context, null);
    }

    public void openPane(boolean open) {
        ActionBar actionBar = ((Activity) getContext()).getActionBar();
        tint.setClickable(open);
        tint.setFocusable(open);
        changeVisibility(tint, open);
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(
                    getResources().getConfiguration().uiMode == Configuration.UI_MODE_NIGHT_YES && open ?
                            R.drawable.gtk_menu_close_dark :
                            getResources().getConfiguration().uiMode == Configuration.UI_MODE_NIGHT_YES ?
                                    R.drawable.gtk_menu_dark : open ?
                                    R.drawable.gtk_menu_close_light : R.drawable.gtk_menu_light);
        }
        ObjectAnimator.ofFloat(pane, "translationX", open ?
                0f : handle.getWidth() * 0.5f - pane.getWidth()).setDuration(duration).start();
    }

    public boolean isOpen() {
        return pane.getTranslationX() == 0;
    }

    public void changeVisibility(final View view, boolean visible) {
        if (visible) {
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(1f).setDuration(duration).setListener(null);
        } else {
            view.animate().alpha(0f).setDuration(duration).setListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(View.INVISIBLE);
                        }
                    }
            );
        }

    }

    public void setPane(View view) {
        child.removeAllViews();
        child.addView(view);
    }

    public ViewGroup getPane() {
        return child;
    }

    private class PaneCloser implements OnTouchListener {

        private boolean close = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            performClick();
            if (view != null && motionEvent != null) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        close = motionEvent.getX() >= pane.getWidth();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (close) openPane(false);
                        break;
                }
            }
            return true;
        }
    }

    private class PaneMover implements OnTouchListener {
        boolean move = false, leftSwipe = false, rightSwipe = false;
        float startX = 0.0f;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            performClick();
            if (view != null && motionEvent != null) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = motionEvent.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float distance = motionEvent.getX() - startX;
                        float position = pane.getTranslationX() + distance;
                        leftSwipe = distance < -5;
                        rightSwipe = distance > 5;
                        boolean limit = position >= handle.getWidth() * 0.5 - pane.getWidth();
                        if (startX != motionEvent.getX() && position <= 0 && limit) {
                            move = true;
                            tint.setVisibility(GONE);
                            pane.setTranslationX(position);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (move && pane.getTranslationX() != 0 || pane.getTranslationX() != -pane.getWidth()) {
                            if (leftSwipe) openPane(false);
                            else if (rightSwipe) openPane(true);
                            else openPane(pane.getTranslationX() > -pane.getWidth() * 0.5);
                        }
                        move = false;
                        break;
                    default:
                        break;
                }
                //view.onTouchEvent(motionEvent);
            }
            return true;
        }
    }
}
