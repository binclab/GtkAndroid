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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class GtkPaned extends FrameLayout implements GtkWidget {

    private final int duration = 200;
    private final RelativeLayout pane;
    LinearLayout tinter, dragger;
    private boolean parent;

    @SuppressLint("ClickableViewAccessibility")
    public GtkPaned(Context context, AttributeSet attrs) {
        super(context, attrs);
        int width = Math.min(getResources().getDisplayMetrics().widthPixels, 432);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(128, -1);
        FrameLayout content = new FrameLayout(context);
        LinearLayout layout = new LinearLayout(context);
        dragger = new LinearLayout(context);
        tinter = new LinearLayout(context);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        pane = new RelativeLayout(context);
        tinter.setBackgroundColor(Color.parseColor("#801C1A1A"));
        tinter.setClickable(false);
        tinter.setFocusable(false);
        tinter.setVisibility(GONE);
        tinter.setLayoutParams(new LayoutParams(-1, -1));
        content.setLayoutParams(new LayoutParams(-1, -1));
        layout.setLayoutParams(new LayoutParams(width, -1));
        layout.setBackgroundColor(Color.WHITE);
        pane.setLayoutParams(new LayoutParams(width + 24, -1));
        dragger.setLayoutParams(params);
        dragger.setOnTouchListener(new OnTouchListener() {
            boolean move = false;
            float startX = 0.0f;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (view != null && motionEvent != null) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = motionEvent.getX();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float position = pane.getTranslationX() + motionEvent.getX() - startX;
                            if (startX != motionEvent.getX() && position <= 0) {
                                move = true;
                                tinter.setVisibility(GONE);
                                pane.setTranslationX(position);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (move && pane.getTranslationX() != 0 || pane.getTranslationX() != -pane.getWidth())
                                openDrawer(pane.getTranslationX() > -pane.getWidth() * 0.5);
                            move = false;
                            break;
                        default:
                            move = false;
                            break;
                    }
                    view.onTouchEvent(motionEvent);
                }
                return true;
            }
        });
        pane.addView(layout);
        pane.addView(dragger);
        parent = true;
        addView(content);
        addView(tinter);
        addView(pane);
        parent = false;
    }

    @Override
    public void addView(View child) {
        if (parent) super.addView(child);
        else ((ViewGroup) getChildAt(0)).addView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (parent) super.addView(child, params);
        else ((ViewGroup) getChildAt(0)).addView(child, params);
    }

    public void setSidePane(View child) {
        ((ViewGroup) pane.getChildAt(0)).addView(child);
    }

    public void openDrawer(boolean open) {
        ActionBar actionBar = ((Activity) getContext()).getActionBar();
        changeVisibility(getChildAt(1), open);
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(
                    getResources().getConfiguration().uiMode == Configuration.UI_MODE_NIGHT_YES && open ?
                            R.drawable.gtk_menu_close_dark :
                            getResources().getConfiguration().uiMode == Configuration.UI_MODE_NIGHT_YES ?
                                    R.drawable.gtk_menu_dark :
                                    open ? R.drawable.gtk_menu_close_light : R.drawable.gtk_menu_light);
        }
        ObjectAnimator.ofFloat(pane, "translationX", open ? 0f : 24 - pane.getWidth())
                .setDuration(duration).start();
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
}