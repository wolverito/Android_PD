package com.example.ray_liu.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by ray_liu on 16/3/27.
 */
public abstract class VisibleFragment extends Fragment{
    private static final String TAG = "VisibleFragment";

    @Override
    public void onStart(){
        super.onStart();

        IntentFilter intentFilter = new IntentFilter(PollService
                .ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification,intentFilter, PollService.PERM_PRIVATE,null);

    }
    @Override
    public void onStop(){
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG,"canceling notification");
                    setResultCode(Activity.RESULT_CANCELED);
                }
            };
}
