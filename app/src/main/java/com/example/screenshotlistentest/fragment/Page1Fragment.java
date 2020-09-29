package com.example.screenshotlistentest.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.screenshotlistentest.R;

public class Page1Fragment extends Fragment {

    private Button openListener,closeListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page1_frag,container,false);
        openListener = view.findViewById(R.id.open_listener);
        closeListener = view.findViewById(R.id.close_listener);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        openListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"开启截图监听",Toast.LENGTH_SHORT).show();
            }
        });

        closeListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"关闭截图监听",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
