package de.visualfan.picchat.ui;

import android.databinding.BindingAdapter;
import android.view.View;

/**
 * Created by administrator on 23.01.18.
 */

public class BindingAdapters {
    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
