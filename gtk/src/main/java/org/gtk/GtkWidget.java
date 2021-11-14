package org.gtk;

import android.view.View;

public class GtkWidget {
    View view;
    
    public void set_vexpand(boolean expand){
        if(view.getClass() == LinearLayout.class && expand){
            LinearLayout.LayoutParams parameters = new 
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        
    }
}
