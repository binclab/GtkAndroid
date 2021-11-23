package org.gtk

import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout


open class GtkWidget {
    lateinit var view: View

    fun set_vexpand(expand: Boolean) {
            if (view == LinearLayout::class.java && expand) {
                val parameters = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            } else if (view == RelativeLayout::class.java) {
                val parameters = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
            }
    }
}