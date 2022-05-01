package org.gtk;

import static android.content.Context.MODE_PRIVATE;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import java.util.Objects;

import android.widget.*;

public class GtkDrawer extends FrameLayout implements GtkWidget {

    private final int duration = 200;
    private boolean parent = false;
    private FrameLayout content;
    private ListView navList;
    private SharedPreferences preferences;
    private LinearLayout drawer, header, progress, tinter;
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

    public GtkDrawer(Context context, String[] strings, View[] objects) {
        this(context, null);
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
        
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void switchView(View[] views, final CharSequence text) {
        changeVisibility(progress, true);
        openDrawer(false);
        if (text == null) {
            changeVisibility(views[0], true);
        } else for (View view : views) {
            if (view.getTag() != null && view.getTag().equals(text)) {
                preferences.edit().putString("LastFragment", (String) text).apply();
                changeVisibility(view, true);
            } else changeVisibility(view, false);

        }
		((Activity) getContext()).getActionBar().setTitle(text);
        new Thread(new Runnable(){
			@Override
			public void run()
			{
				try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
				post(new Runnable(){
					@Override
					public void run(){
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
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (view != null && motionEvent != null) {
                listItem = view.getClass().getSimpleName().equals("ListView");
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = motionEvent.getX();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
					if (startX != motionEvent.getX()){
                    	float f = listItem ? drawer.getTranslationX() + motionEvent.getX() - startX :
                            motionEvent.getX() - drawer.getWidth();
                    	if (f <= 0) {
							move = true;
							tinter.setVisibility(GONE);
							drawer.setTranslationX(f);
						}
					}
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && move) {
					if (drawer.getTranslationX() != 0 || drawer.getTranslationX() != -drawer.getWidth())
                    	openDrawer(drawer.getTranslationX() > -drawer.getWidth() * 0.5);
                    move = false;
                }
                view.onTouchEvent(motionEvent);
            }
            return true;
        }

    }

    public void setAdapter(final String[] subjects, final View[] views) {
        navList.setAdapter(new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, subjects));

        for (View view : views) {
            if (view.getParent() == null) addView(view);
            view.setVisibility(GONE);
        }
        switchView(views, preferences.getString("LastFragment", null));
        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                switchView(views, subjects[i]);
            }
        });
    }

    public void setHeader(View view) {
        header.addView(view);
    }

    public void openDrawer(boolean open) {
        tinter.setClickable(open);
        tinter.setFocusable(open);
        changeVisibility(tinter, open);
		tinter.setBackgroundColor(open? Color.parseColor("#801C1A1A"): Color.WHITE);
		((Activity) getContext()).getActionBar().setHomeAsUpIndicator(
			getResources().getConfiguration().uiMode == UI_MODE_NIGHT_YES && open?
				R.drawable.gtk_menu_close_dark : 
			getResources().getConfiguration().uiMode == UI_MODE_NIGHT_YES?
				R.drawable.gtk_menu_dark :
			open? R.drawable.gtk_menu_close_light : R.drawable.gtk_menu_light);
  		ObjectAnimator.ofFloat(drawer, "translationX", open? 0f: 
			- Math.min(getResources().getDisplayMetrics().widthPixels, 432))
			.setDuration(duration).start();
    }

    public boolean isOpen() {
        return drawer.getTranslationX() == 0;
    }

    private void setVariables() {
        preferences = ((Activity) getContext()).getPreferences(MODE_PRIVATE);
        drawer = new LinearLayout(getContext());
        header = new LinearLayout(getContext());
        navList = new ListView(getContext());
        progress = new LinearLayout(getContext());
        tinter = new LinearLayout(getContext());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupContent() {
        ScrollView scroller = new ScrollView(getContext());
        ProgressBar loader = new ProgressBar(getContext());
        LinearLayout dragger = new LinearLayout(getContext());
        content = new FrameLayout(getContext());
        content.setId(R.id.gtk_fragment);
        parent = true;
        progress.addView(loader);
        addView(scroller);
        addView(dragger);
        addView(progress);
        addView(tinter);
        addView(drawer);
        scroller.addView(content);
        scroller.setFillViewport(true);
        scroller.getLayoutParams().height = LayoutParams.MATCH_PARENT;
        scroller.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        dragger.getLayoutParams().height = LayoutParams.MATCH_PARENT;
        dragger.getLayoutParams().width = 24;
        dragger.setOnTouchListener(touchListener);
        progress.setBackgroundColor(Color.WHITE);
        progress.getLayoutParams().height = LayoutParams.MATCH_PARENT;
        progress.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        progress.setGravity(Gravity.CENTER);
        progress.setVisibility(VISIBLE);
        loader.getLayoutParams().height = duration;
        loader.getLayoutParams().width = duration;
        parent = false;
    }

    private void setupTinter() {
        tinter.getLayoutParams().height = LayoutParams.MATCH_PARENT;
        tinter.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        tinter.setVisibility(GONE);
        tinter.setLayoutTransition(new LayoutTransition());
        tinter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer(false);
            }
        });
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

    private void changeVisibility(final View view, boolean visible) {
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
