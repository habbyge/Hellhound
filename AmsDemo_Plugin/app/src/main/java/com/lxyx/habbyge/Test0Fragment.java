package com.lxyx.habbyge;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class Test0Fragment extends Fragment {

    public Test0Fragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_test0, container, false);
    }
}
