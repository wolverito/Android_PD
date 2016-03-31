package com.example.ray_liu.photogallery;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


/**
 * Created by ray_liu on 16/3/1.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {
    protected  abstract Fragment createFragment();
    private static final String TAG = "SingleFragmentActivity";

    @LayoutRes
    protected int getLayoutResId(){
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        Log.d(TAG,"onCreate called");

        FragmentManager fm =  getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment == null){
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragment_container,fragment).commit();
        }

    }

}
