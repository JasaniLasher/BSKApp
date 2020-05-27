package com.example.bskapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.SyncStateContract;
import android.util.Log;
import android.util.Pair;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import com.example.bskapp.com.example.bskapp.dataapi.AzureServiceAdapter;
import com.example.bskapp.com.example.bskapp.dataapi.DriverLocationInfo;
import com.example.bskapp.com.example.bskapp.dataapi.DriverVehicleInfo;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LocationUpdaterService extends Service {

    public static final String CHANNEL_ID = "BSKAppChannel";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_TRIP_ON = "ACTION_TRIP_ON";
    public static final String ACTION_TRIP_OFF = "ACTION_TRIP_OFF";
    private Location lastAvailableLocation;
    private Date updateTime;
    Timer timer = new Timer();
    private MobileServiceClient mClient;
    private  String driverGuid;
    private int driverID;
    private int companyID;
    private int branchID;
    private boolean isOnTrip;
    private static AzureServiceAdapter mInstance = null;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String TAG = "LocationUpdaterService";

    @Override
    public void onCreate() {
        super.onCreate();
        //AzureServiceAdapter.Initialize(this);
        Log.d(TAG, "onCreate:Entered ");
        MainApplication mApp = MainApplication.getContext();
        driverGuid = mApp.getDriverGuid();
        driverID = mApp.getDriverID();
        companyID = mApp.getCompanyID();
        branchID = mApp.getBranchID();
        updateTime = Calendar.getInstance().getTime();
        mClient = AzureServiceAdapter.getInstance().getClient();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initializeLocationRequest();
        mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Entered");
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_START:

                    createNotificationChannel();

                    Intent notificationIntent = new Intent(this, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this,
                            0, notificationIntent, 0);


                    Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setContentTitle("BSK Driver App")
                            .setContentText("Tracking your location.Logout to stop tracking.")
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentIntent(pendingIntent)
                            .build();

                    startForeground(1, notification);

                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                        startEveryMinTask();
                        }
                        }, 60000, 60000);


                    return START_NOT_STICKY;

                case ACTION_STOP:
                    timer.cancel();
                    // Stop foreground service and remove the notification.
                    stopForeground(true);
                    // Stop the foreground service.
                    stopSelf();
                    break;
                case ACTION_TRIP_ON:

                    isOnTrip = true;
                    break;

                case ACTION_TRIP_OFF:
                    isOnTrip = false;
                    break;

            }

        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "BSKApp Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }



    private void initializeLocationRequest(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(55000);
        locationRequest.setFastestInterval(30000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d(TAG, "onLocationResult: NULL");
                    return;
                }
                for (android.location.Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d(TAG, "onLocationResult: Entered");
                        lastAvailableLocation = location;

                    }
                }
            }
        };
    }


    private void updateLocationInfo()
    {
        Log.d(TAG, "Entered updateLocation");
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceTable<DriverLocationInfo> mDriverLocationInfoTable = mClient.getTable(DriverLocationInfo.class);
                    DriverLocationInfo driverUpdateInfo =   new DriverLocationInfo();
                    driverUpdateInfo.setId(driverGuid);
                    driverUpdateInfo.setLastKnownLocation("");
                    driverUpdateInfo.setDriverID(driverID);
                    driverUpdateInfo.setBranchId(branchID);
                    driverUpdateInfo.setCompanyId(companyID);
                    driverUpdateInfo.setLatitude(lastAvailableLocation.getLatitude());
                    driverUpdateInfo.setLongitude(lastAvailableLocation.getLongitude());

                    mDriverLocationInfoTable
                            .insert(driverUpdateInfo)
                            .get();

                    updateTime = Calendar.getInstance().getTime();
                    Log.d(TAG, "doInBackground: process completed");
/*                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {

                        }
                    };
                    mainHandler.post(myRunnable);*/
                } catch (final Exception e) {
                    Log.d(TAG, "Location exception: "+e.getMessage());
                }
                return null;
            }
        };

        runAsyncTask(task);
    }

    private void startEveryMinTask()
    {
        Log.d(TAG, "startEveryMinTask: Entered");
        Date curTime = Calendar.getInstance().getTime();
        long timeDifferenceSecs = TimeUnit.MILLISECONDS.toSeconds(curTime.getTime() - updateTime.getTime());
        Log.d(TAG, "startEveryMinTask - TimeDifference: "+ String.valueOf(timeDifferenceSecs) + " " + String.valueOf(isOnTrip));
        if(isOnTrip && timeDifferenceSecs > 59)
        {
            Log.d(TAG, "startEveryMinTask: OnTrip");
            updateLocationInfo();
        }

        else if(!isOnTrip && timeDifferenceSecs > 295)
        {
            Log.d(TAG, "startEveryMinTask: OffTrip");
            updateLocationInfo();
        }
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

}
