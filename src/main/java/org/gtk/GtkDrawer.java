package org.gtk;

import static android.content.Context.MODE_PRIVATE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import android.widget.*;
import android.graphics.*;
import android.view.*;

public class GtkDrawer extends RelativeLayout implements GtkWidget {

    private boolean parent = false;
    private FragmentManager manager;
    private Fragment[] fragments;
    private FrameLayout content;
    private ListView navList;
    private SharedPreferences preferences;
    private LinearLayout tinter, drawer, header, progress;
    private final TouchListener touchListener = new TouchListener();

    public GtkDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        /*TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.GtkDrawer, 0, 0
        );
        right = a.getBoolean(R.styleable.GtkDrawer_right, false);
        a.recycle();*/
        setVariables();
        setupContent();
        setupTinter();
        setupDrawer();
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) throw new IllegalStateException();
        else super.addView(child, params);
    }

    @Override
    public void addView(View child) {
        if (parent) super.addView(child);
        else content.addView(child);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        post(new Runnable() {
            @Override
            public void run() {
                changeVisibility(progress, false);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void switchFragment(int position, CharSequence text) {
        ActionBar actionBar = ((Activity) getContext()).getActionBar();
        preferences.edit().putInt("LastFragment", position).apply();
        actionBar.setTitle("");
        manager.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.gtk_fragment, fragments[position]).commit();
        changeVisibility(progress, true);
        navigate(false);
        if (position == 0) navList.setAdapter(new NavigationAdapter(getContext(), new String[]{}));
        new Thread(new Runnable() {
            final Handler handler = new Handler();

            @Override
            public void run() {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        actionBar.setTitle(text);
                        changeVisibility(progress, false);
                    }
                });
            }
        }).start();
    }

    private class TouchListener implements View.OnTouchListener {

        boolean move = false, listItem = false;
        float startX = 0.0f;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(@GtkNullable View view, @GtkNullable MotionEvent motionEvent) {
            if (view != null && motionEvent != null) {
                listItem = view.getClass().getSimpleName().equals("ListView");
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = motionEvent.getX();
                        if (listItem && startX > drawer.getWidth() - 128) move = true;
                        else if (motionEvent.getX() <= 48) move = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (move) {
                            float position;
                            if (listItem)
                                position = drawer.getTranslationX() + motionEvent.getX() - startX;
                            else
                                position = motionEvent.getX() - drawer.getWidth();
                            if (motionEvent.getX() - startX <= -5) tinter.setVisibility(View.GONE);
                            else if (motionEvent.getX() - startX >= 5)
                                tinter.setVisibility(View.VISIBLE);
                            if (position <= 0) drawer.setTranslationX(position);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (move) {
                            if (drawer.getTranslationX() < -drawer.getWidth() * 0.5)
                                animateDrawer(0.0f - drawer.getWidth(), false);
                            else
                                animateDrawer(0.0f, true);
                            move = false;
                        }
                        break;
                    default:
                        break;
                }
                view.onTouchEvent(motionEvent);
            }
            return true;
        }

        public void animateDrawer(float position, boolean drawerOpen) {
            ObjectAnimator animator =
                    ObjectAnimator.ofFloat(drawer, "translationX", position);
            int drawable, shortAnimationDuration =
                    getResources().getInteger(android.R.integer.config_shortAnimTime);
            if (drawerOpen) {
                changeVisibility(tinter, true);
                if (getResources().getConfiguration().uiMode == UI_MODE_NIGHT_YES)
                    drawable = R.drawable.gtk_menu_close_dark;
                else drawable = R.drawable.gtk_menu_close_light;
            } else {
                changeVisibility(tinter, false);
                if (getResources().getConfiguration().uiMode == UI_MODE_NIGHT_YES)
                    drawable = R.drawable.gtk_menu_dark;
                else drawable = R.drawable.gtk_menu_light;
            }
            ((Activity) getContext()).getActionBar().setHomeAsUpIndicator(drawable);
            animator.setDuration(shortAnimationDuration).start();
        }
    }

    private static class NavigationAdapter extends ArrayAdapter<String> {
        public NavigationAdapter(Context context, String[] stringArray) {
            super(context, android.R.layout.simple_list_item_1, stringArray);
        }

    }

    public void setAdapter(String[] subjects, ArrayList<Fragment> list) {
        int position = preferences.getInt("LastFragment", 0);
        navList.setAdapter(new NavigationAdapter(getContext(), subjects));
        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switchFragment(position + 1, ((TextView) view).getText());
            }
        });
        fragments = list.toArray(new Fragment[0]);
        if (subjects.length != 0 && position != 0)
            ((Activity) getContext()).getActionBar().setTitle(subjects[position - 1]);
        manager.beginTransaction().add(R.id.gtk_fragment, fragments[position]).commit();
    }

    public void setHeader(View view) {
        header.addView(view);
    }

    public void navigate(boolean open) {
        if (getWidth() == 0) touchListener.animateDrawer(0.0f - Math.min(
                getResources().getDisplayMetrics().widthPixels, 432), false);
        else if (open) touchListener.animateDrawer(0.0f, true);
        else touchListener.animateDrawer((0.0f - getWidth()), false);
    }

    private void setVariables() {
        preferences = ((Activity) getContext()).getPreferences(MODE_PRIVATE);
        manager = ((Activity) getContext()).getFragmentManager();
        tinter = new LinearLayout(getContext());
        drawer = new LinearLayout(getContext());
        header = new LinearLayout(getContext());
        navList = new ListView(getContext());
        progress = new LinearLayout(getContext());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupContent() {
        ScrollView scroller = new ScrollView(getContext());
        ProgressBar loader = new ProgressBar(getContext());
        content = new FrameLayout(getContext());
        content.setId(R.id.gtk_fragment);
        parent = true;
        addView(scroller);
        addView(tinter);
        addView(drawer);
        addView(progress);
        scroller.addView(content);
        scroller.setFillViewport(true);
        scroller.getLayoutParams().height = LayoutParams.MATCH_PARENT;
        scroller.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        scroller.setOnTouchListener(touchListener);
        progress.setBackgroundColor(Color.WHITE);
        progress.getLayoutParams().height = LayoutParams.MATCH_PARENT;
        progress.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        progress.setGravity(Gravity.CENTER);
        progress.addView(loader);
        loader.getLayoutParams().height = 200;
        loader.getLayoutParams().width = 200;
        parent = false;
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setupDrawer() {
        int width = Math.min(getResources().getDisplayMetrics().widthPixels, 432);
        drawer.getLayoutParams().width = width;
        drawer.setBackgroundColor(Color.WHITE);
        drawer.setOrientation(LinearLayout.VERTICAL);
        drawer.setTranslationX(-width);
        drawer.getLayoutParams().height = LayoutParams.MATCH_PARENT;
        drawer.addView(header);
        drawer.addView(navList);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setMinimumHeight(100);
        header.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
        navList.getLayoutParams().height = LayoutParams.MATCH_PARENT;
        navList.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        navList.setOnTouchListener(touchListener);
    }

    private void setupTinter() {
        tinter.setBackgroundColor(Color.parseColor("#801C1A1A"));
        tinter.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        tinter.getLayoutParams().height = LayoutParams.MATCH_PARENT;
        tinter.setClickable(true);
        tinter.setFocusable(true);
        tinter.setVisibility(GONE);
        tinter.setLayoutTransition(new LayoutTransition());
    }

    private void changeVisibility(View view, boolean visible) {
        int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        if (visible) {
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(1f).setDuration(duration).setListener(null);
        } else {
            view.animate().alpha(0f).setDuration(duration).setListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            tinter.setVisibility(View.GONE);
                        }
                    }
            );
        }
    }
}
