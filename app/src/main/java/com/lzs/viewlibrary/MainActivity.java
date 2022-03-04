package com.lzs.viewlibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lzs.banner.BannerView;
import com.lzs.rockerview.RootView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new BannerView().getBannerInfo();
        new RootView().getRootInfo();
    }
}