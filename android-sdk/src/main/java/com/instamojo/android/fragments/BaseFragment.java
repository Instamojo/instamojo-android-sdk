package com.instamojo.android.fragments;

import androidx.fragment.app.Fragment;
import android.view.View;

/**
 * An abstract Fragment
 */
public abstract class BaseFragment extends Fragment {

    /**
     * Attach views to Java in this method
     *
     * @param view rootview of the fragment
     */
    public void inflateXML(View view) {
        // No logic
    }

    /**
     * @return Name of the fragment
     */
    public String getFragmentName() {
        return "";
    }
}
