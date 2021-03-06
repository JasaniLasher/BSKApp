package com.example.bskapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bskapp.com.example.bskapp.dataapi.AzureServiceAdapter;
import com.example.bskapp.com.example.bskapp.dataapi.DriverLocationInfo;
import com.example.bskapp.com.example.bskapp.dataapi.DriverVehicleInfo;
import com.example.bskapp.com.example.bskapp.dataapi.IMEIRelation;
import com.example.bskapp.com.example.bskapp.dataapi.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity  {

    public static final String EXTRA_MESSAGE = "key";
    private MobileServiceClient mClient;
    private String itemId;
    private android.content.Context curContext;
    private MainApplication mApp;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private android.location.Location lastAvailableLocation;
    private String TAG = "MainActivity";
    private boolean isLoading = false;
    private BigDecimal kms;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isLoading = true;
        curContext = this;
        getImei();
        mClient = AzureServiceAdapter.getInstance().getClient();
        mApp = MainApplication.getContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initializeLocationRequest();
        mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null);
        Switch sw_Log = (Switch) findViewById(R.id.switchButton_Log);
        sw_Log.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tv_Login = (TextView)findViewById(R.id.textView_Switch1);
                if (isChecked) {
                    tv_Login.setText("Logged ON");
                    startService();

                } else {
                    tv_Login.setText("Logged OFF");
                    stopService();

                }
            }
        });

        sw_Log.setOnClickListener(new CompoundButton.OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean checked = ((Switch)v).isChecked();
                if(checked)
                    UpdateShedInShedOut("Shed In");
                else
                    UpdateShedInShedOut("Shed Out");

            }

        });


        Switch sw_Trip = (Switch) findViewById(R.id.switchButton_Trip);

        sw_Trip.setOnClickListener(new CompoundButton.OnClickListener(){
            @Override
            public void onClick(View v) {
                final boolean checked = ((Switch)v).isChecked();
                AlertDialog.Builder builder = new AlertDialog.Builder(curContext);
                if(checked)
                    builder.setTitle("Opening Kms");
                else
                    builder.setTitle("Closing Kms");
                // Set up the input
                final EditText input = new EditText(curContext);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextView tv_Trip = (TextView)findViewById(R.id.textView_Switch2);

                        // Create a DecimalFormat that fits your requirements
                        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                        //symbols.setGroupingSeparator(',');
                        symbols.setDecimalSeparator('.');
                        String pattern = "###0.0#";
                        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
                        decimalFormat.setParseBigDecimal(true);

                        // parse the string
                        BigDecimal bigDecimal = null;
                        try {
                            bigDecimal = (BigDecimal) decimalFormat.parse(input.getText().toString());
                            Log.d(TAG, "kms: " + bigDecimal);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        kms = bigDecimal;
                        if(checked) {
                            UpdateTripStatus("Running",kms);
                            tv_Trip.setText("Trip ON");
                            serviceTripOn();
                        }
                        else{
                            UpdateTripStatus("Standing",kms);
                            tv_Trip.setText("Trip OFF");
                            serviceTripOff();
                        }

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Switch sw_Trip = (Switch) findViewById(R.id.switchButton_Trip);
                        if(checked)
                            sw_Trip.setChecked(false);
                        else
                            sw_Trip.setChecked(true);
                        dialog.cancel();
                    }
                });

                builder.show();
            }

        });

        Ringtone r = mApp.getNotificationRingtone();
        r.stop();
        mApp.syncFirebaseToken();
        isLoading = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //setIntent(intent);//must store the new intent unless getIntent() will return the old one
        Log.d(TAG, "Notification Clicked!");
        Ringtone r = mApp.getNotificationRingtone();
        r.stop();
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, LocationUpdaterService.class);
        serviceIntent.setAction(LocationUpdaterService.ACTION_START);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, LocationUpdaterService.class);
        serviceIntent.setAction(LocationUpdaterService.ACTION_STOP);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void serviceTripOn() {
        Intent serviceIntent = new Intent(this, LocationUpdaterService.class);
        serviceIntent.setAction(LocationUpdaterService.ACTION_TRIP_ON);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void serviceTripOff() {
        Intent serviceIntent = new Intent(this, LocationUpdaterService.class);
        serviceIntent.setAction(LocationUpdaterService.ACTION_TRIP_OFF);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void trigger(View view)
    {
        isLoading = true;
        mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null);
        getIMEIRelation();
        isLoading = false;
    }




    private void getDriverVehicleInfo()
    {
        EditText et_IMEI = (EditText) findViewById(R.id.editText_IMEI);
        EditText et_CompanyId = (EditText) findViewById(R.id.editText_companyID);

        final String imei = et_IMEI.getText().toString();
        final int companyId = Integer.parseInt(et_CompanyId.getText().toString());

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Log.d(TAG, "doInBackground: entered background");
                    MobileServiceTable<DriverVehicleInfo> mDriverVehicleInfoTable = mClient.getTable(DriverVehicleInfo.class);
                    final List<DriverVehicleInfo> result = mDriverVehicleInfoTable
                            .where()
                            .field("IMEI").eq(imei)
                            .execute()
                            .get();

                    Log.d(TAG, "doInBackground: completed get");


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            updateDriverVehicleInfo(result.get(0).getDriverName(),result.get(0).getDriverID(),result.get(0).getDriverMobileNo(),result.get(0).getRegistrationNo(),
                            result.get(0).getTripStatus(),result.get(0).getShedInShedOutStatus(),result.get(0).getId());
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);
    }


    private void getLocation(final double latitude,final double longitude)
    {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    List<Pair<String,String>> apiParams = new ArrayList<Pair<String,String>>();
                    apiParams.add(new Pair("companyId",Integer.toString(mApp.getCompanyID())));
                    apiParams.add(new Pair("branchId",Integer.toString(mApp.getBranchID())));
                    apiParams.add(new Pair("driverId",Integer.toString(mApp.getDriverID())));
                    apiParams.add(new Pair("latitude",String.valueOf(latitude)));
                    apiParams.add(new Pair("longitude",String.valueOf(longitude)));

                    final Location loc =
                    mClient.invokeApi("Location","GET",apiParams,Location.class).get();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            updateLocation(loc.getLocation());
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);
    }


    private void updateLocation(String pLocation)
    {

        TextView tv_Location = (TextView)findViewById(R.id.textView_LocationName);
        tv_Location.setText(pLocation);
    }

    private void UpdateDBLocation(final String id)
    {


        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Log.d(TAG, "doInBackground: Entered background task");
                    DriverVehicleInfo driverUpdateInfo = new DriverVehicleInfo();
                    driverUpdateInfo.setId(id);
                    driverUpdateInfo.setLastKnownLocation("Dummy");
                    MobileServiceTable<DriverVehicleInfo> mDriverVehicleInfoTable = mClient.getTable(DriverVehicleInfo.class);
                    final DriverVehicleInfo result = mDriverVehicleInfoTable
                            .insert(driverUpdateInfo)
                            .get();
                    Log.d(TAG, "doInBackground: Updated DB location");

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);
    }

    private void updateDriverVehicleInfo(String pDriverName,int pDriverID,String pMobileNo,String RegNo,String pTripStatus,String pShedInShedOutStatus,String driverGuid)
    {
        mApp.setDriverID(pDriverID);
        mApp.setDriverGuid(driverGuid);
        //Log.d(TAG, "updateDriverVehicleInfo: " + driverGuid);
        //UpdateDBLocation(driverGuid);


/*        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            return;
        }*/



        TextView tv_Welcome = (TextView)findViewById(R.id.textView_Welcome);
        tv_Welcome.setText("Welcome " + pDriverName + "!" );

        TextView tv_MobileNo = (TextView)findViewById(R.id.textView_MobileNo);
        tv_MobileNo.setText(pMobileNo);

        TextView tv_RegNo = (TextView)findViewById(R.id.textView_RegNo);
        tv_RegNo.setText(RegNo);

        Switch sw_Login = (Switch) findViewById(R.id.switchButton_Log);


        if(pShedInShedOutStatus.equalsIgnoreCase("Shed In")) {
            sw_Login.setChecked(true);

        }
        else {
            sw_Login.setChecked(false);

        }
        Log.d(TAG,"Check Location");
        if(lastAvailableLocation != null) {
            Log.d(TAG,"Location Available");
            getLocation(lastAvailableLocation.getLatitude(), lastAvailableLocation.getLongitude());
        }



        Switch sw_Trip = (Switch) findViewById(R.id.switchButton_Trip);
        TextView tv_Trip= (TextView)findViewById(R.id.textView_Switch2);

        if(pTripStatus.equalsIgnoreCase("Running")) {
            sw_Trip.setChecked(true);
            serviceTripOn();
            tv_Trip.setText("Trip ON");
        }
        else
        {
            sw_Trip.setChecked(false);
            serviceTripOff();
            tv_Trip.setText("Trip OFF");
        }
    }

    private void getIMEIRelation()
    {
        EditText et_IMEI = (EditText) findViewById(R.id.editText_IMEI);
        EditText et_CompanyId = (EditText) findViewById(R.id.editText_companyID);


        final String imei = et_IMEI.getText().toString();
        final int companyId = Integer.parseInt(et_CompanyId.getText().toString());
        mApp.setCompanyID(companyId);


        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceTable<IMEIRelation> mIMEITable = mClient.getTable(IMEIRelation.class);
                    final List<IMEIRelation> result = mIMEITable
                            .where()
                            .field("IMEI").eq(imei)
                            .and()
                            .field("CompanyId").eq(companyId)
                            .execute()
                            .get();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            updateUIIMEIRelation(result.get(0).getBranchName(),result.get(0).getDatabaseName(),
                                    result.get(0).getBranchId());
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);

    }

    private void updateUIIMEIRelation(String pBranchName,String pDatabaseName,int pBranchID)
    {
        mApp.setTenantID(pDatabaseName);
        mApp.setBranchID(pBranchID);

        TextView tv_BranchName = (TextView)findViewById(R.id.textView_BranchName);
        tv_BranchName.setText(pBranchName);

        getDriverVehicleInfo();
    }

    private void initializeLocationRequest(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(100);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG,"Location Callback Successful");
                mFusedLocationClient.removeLocationUpdates(locationCallback);
                if (locationResult == null) {
                    Log.d(TAG,"Location result is null");
                    return;
                }
                for (android.location.Location location : locationResult.getLocations()) {
                    if (location != null) {
                        lastAvailableLocation = location;
                    }
                }
            }
        };
    }

    private void UpdateShedInShedOut(final String status)
    {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceTable<DriverVehicleInfo> mDriverVehicleInfoTable = mClient.getTable(DriverVehicleInfo.class);
                    DriverVehicleInfo driverVehicleInfo =   new DriverVehicleInfo();
                    driverVehicleInfo.setId(UUID.randomUUID().toString());
                    driverVehicleInfo.setIMEI(mApp.getImei());
                    driverVehicleInfo.setShedInShedOutStatus(status);
                    driverVehicleInfo.setReading(new BigDecimal(0));

                    mDriverVehicleInfoTable
                            .insert(driverVehicleInfo)
                            .get();

                } catch (final Exception e) {
                    e.printStackTrace();
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);
    }

    private void UpdateTripStatus(final String status,final BigDecimal reading)
    {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceTable<DriverVehicleInfo> mDriverVehicleInfoTable = mClient.getTable(DriverVehicleInfo.class);
                    DriverVehicleInfo driverVehicleInfo =   new DriverVehicleInfo();
                    driverVehicleInfo.setId(UUID.randomUUID().toString());
                    driverVehicleInfo.setIMEI(mApp.getImei());
                    driverVehicleInfo.setTripStatus(status);
                    driverVehicleInfo.setReading(reading);
                    mDriverVehicleInfoTable
                            .insert(driverVehicleInfo)
                            .get();

                } catch (final Exception e) {
                    e.printStackTrace();
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);
    }



    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });

    }

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }



    public void getImei() {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei="";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            // READ_PHONE_STATE permission has been granted.
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                try {
                    imei = telephonyManager.getImei();
                }
                catch(SecurityException ex) {
                    createAndShowDialogFromTask(ex, "Error");
                }
            }
            else
            {
                try {
                    imei = telephonyManager.getDeviceId();
                }
                catch(SecurityException ex) {
                    createAndShowDialogFromTask(ex, "Error");
                }
            }
        }


        Log.d(TAG,"IMEI :" +imei);

    }
}