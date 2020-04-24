package com.example.bskapp;

import android.app.Application;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.bskapp.com.example.bskapp.dataapi.AzureServiceAdapter;
import com.example.bskapp.com.example.bskapp.dataapi.DriverLocationInfo;
import com.example.bskapp.com.example.bskapp.dataapi.IMEIRelation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.util.Calendar;
import java.util.UUID;

public class MainApplication extends Application {

    private String TAG = "BSKApp";
    private MobileServiceClient mClient;

    private String mTenantID;

    public String getTenantID() {
        return mTenantID;
    }

    public void setTenantID(String tenantID) {
        this.mTenantID = tenantID;
    }

    private String mImei;

    public String getImei() {
        return mImei;
    }

    public void setImei(String imei) {
        this.mImei = imei;
    }

    private String mDriverGuid;

    public String getDriverGuid() {
        return mDriverGuid;
    }

    public void setDriverGuid(String DriverGuid) {
        this.mDriverGuid = DriverGuid;
    }

    private int mDriverID;

    public int getDriverID() {
        return mDriverID;
    }

    public void setDriverID(int driverID) {
        this.mDriverID = driverID;
    }


    private int mBranchID;

    public int getBranchID() {
        return mBranchID;
    }

    public void setBranchID(int branchID) {
        this.mBranchID = branchID;
    }


    private int mCompanyID;

    public int getCompanyID() {
        return mCompanyID;
    }

    public void setCompanyID(int companyID) {
        this.mCompanyID = companyID;
    }

    private Ringtone mNotificationRingtone;

    public Ringtone getNotificationRingtone() {
        return mNotificationRingtone;
    }

    private void setNotificationRingtone() {
        Uri notificationuri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        this.mNotificationRingtone = RingtoneManager.getRingtone(getApplicationContext(), notificationuri);
    }

    private static MainApplication mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        setNotificationRingtone();
        setImei("1111");
        mContext = this;
        AzureServiceAdapter.Initialize(this);
        mClient = AzureServiceAdapter.getInstance().getClient();
    }

    public static MainApplication getContext() {
        return mContext;
    }

    public void syncFirebaseToken()
    {
        Log.d(TAG, "Begin Token Sync..");
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("BSKApp", "getFirebaseInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        updateNotificationToken(token);
                        // Log and toast

                        Log.d(TAG, token);

                    }
                });
    }

    public void updateNotificationToken(final String token)
    {
        Log.d(TAG, "Entered Token Update");
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceTable<IMEIRelation> mIMEIRelationTable = mClient.getTable(IMEIRelation.class);
                    UUID uuid = UUID.randomUUID();
                    String randomUUIDString = uuid.toString();
                    IMEIRelation tokenUpdateInfo =   new IMEIRelation();
                    tokenUpdateInfo.setId(randomUUIDString);
                    tokenUpdateInfo.setIMEI(mImei);
                    tokenUpdateInfo.setNotificationToken(token);
                    mIMEIRelationTable
                            .insert(tokenUpdateInfo)
                            .get();
                    Log.d(TAG, "Token Inserted");
/*                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {

                        }
                    };
                    mainHandler.post(myRunnable);*/
                } catch (final Exception e) {
                    Log.d(TAG, "Token insert exception: "+e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };

        runAsyncTask(task);
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }
}
