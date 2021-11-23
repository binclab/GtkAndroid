package org.gtk

import android.app.Application

class GtkApplication: Application(){
    @Override
    override fun onCreate() {
        super.onCreate()
        gtkapp = this
    }
}