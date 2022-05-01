package org.gtk;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class GtkScrollView extends ScrollView {
    public GtkScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFillViewport(true);
    }
}
