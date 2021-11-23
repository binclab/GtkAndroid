package org.gtk

import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Xml
import android.widget.LinearLayout
import androidx.core.view.setPadding
import org.xmlpull.v1.XmlPullParser

class GtkBox(orientation: GtkOrientation, spacing: Int): GtkWidget() {
    init {
        view = LinearLayout(gtkapp)
        setSpacing(spacing)
        if (orientation == GtkOrientation.VERTICAL){
            (view as LinearLayout).orientation = LinearLayout.VERTICAL
        }
    }

    private fun setSpacing(spacing: Int){
        view.setPadding(spacing)
    }
}