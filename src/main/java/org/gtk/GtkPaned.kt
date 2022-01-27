package org.gtk

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout

open class GtkPaned (context: Context, attributeSet: AttributeSet): LinearLayout(context,attributeSet) {
    lateinit var drawer: RelativeLayout
    lateinit var child1: LinearLayout
    lateinit var handle: LinearLayout

    init {
        inflate(context, R.layout.gtk_paned, this)
        setVariables()
        setDefaults()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setVariables() {
        Log.e("GtkPaned", "Clicked")
        drawer = findViewById(R.id.paneDrawer)
        //handle = findViewById(R.id.paneHandle)
        child1 = findViewById(R.id.paneChild1)
    }

    fun setDefaults(){
        setOnTouchListener(DragView())
        drawer.setOnTouchListener(DragView())
        //handle.setOnTouchListener(DragView())
        child1.setOnTouchListener(DragView())
    }

    inner class DragView : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event != null) {
                /*val content = v.findViewById(R.id.paneContent)
                val handle = v.findViewById(R.id.paneHandle)
                val drawer = v.findViewById(R.id.paneDrawer)*/
                //if (event.x < 50)
                drawer.translationX = event.x
                Log.e("GtkPaned", event.x.toString())
            }
            return true
        }
    }
}