package org.gtk

import android.app.Activity
import android.os.Bundle
import android.util.Log

open class GtkApplicationWindow : Activity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view: GtkBox = GtkBox(GtkOrientation.VERTICAL, 0)
        setContentView(view.view)
    }

}