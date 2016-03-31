package com.example.ray_liu.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by ray_liu on 16/3/26.
 */
public class PollService extends IntentService {
    private static final String TAG = "PollService";

    public static final String ACTION_SHOW_NOTIFICATION =
            "com.example.ray_liu.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE =
            "com.example.ray_liu.photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    private static final int POLL_INTERVAL = 1000*6;

    public static Intent newIntent(Context context){
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        //context: with which to send the intent
        //requestcode: used to distinguish this PendingIntent from others
        //intent: intent to send
        //flags: used to tweak how the PendingIntent is created

        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(ALARM_SERVICE);

        if(isOn){
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);//set the alarm
            //constant: describe the time basis for the alarm
            //time: at which to start the alarm
            //time interval
            //a PendingIntent to fire when the alarm goes off
        }else{
            alarmManager.cancel(pi);
            pi.cancel();
        }
        QueryPreferences.setAlarmOn(context,isOn);
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent i = PollService.newIntent(context);
        PendingIntent pi= PendingIntent.getService(context,0,i,PendingIntent.FLAG_NO_CREATE);
        return pi!= null;
    }

    public PollService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        if(!isNetworkAvailableAndConnected()){
            return;
        }

        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        List<GalleyItem> items;

        if(query == null){
            items = new FlickrFetchr().fetchRecentPhotos();
        }else{
            items = new FlickrFetchr().searchPhotos(query);
        }

        if(items.size()==0){
            return;
        }
        String new_resultId = items.get(0).getId();
        if(new_resultId.equals(lastResultId)){
            Log.i(TAG, "Got an old result!" + new_resultId);
        }else {
            Log.i(TAG, "Got a new result!" + new_resultId);

            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getService(this, 0, i, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))//config the ticker text
                    .setSmallIcon(R.drawable.flr) //config the small icon
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi) //pi will be fired when user press the Notification
                    .setAutoCancel(true)
                    .build();

//            NotificationManagerCompat notificationManagerCompat =
//                    NotificationManagerCompat.from(this);
//            notificationManagerCompat.notify(0, notification); //0 is an identifier for this one
//            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);
            showBackgroundNotification(0, notification);
            //Using the permission here ensures taht any application must use that same
            // permisson to receive the intent you are sending.
        }
        QueryPreferences.setLastResultId(this,new_resultId);
    }
    private void showBackgroundNotification(int requestCode, Notification notification){
        Intent i  = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE,null, null,
                Activity.RESULT_OK, null, null);
        //1. result receiver;   2. a handler to run the result receiver on
        //3. the initial values for result code;  4. result data
        //5. result extras for the ordered broadcast;
    }

    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo()!=null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }
}
