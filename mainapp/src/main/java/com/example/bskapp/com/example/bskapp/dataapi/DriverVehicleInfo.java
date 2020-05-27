package com.example.bskapp.com.example.bskapp.dataapi;

import com.microsoft.windowsazure.mobileservices.table.DateTimeOffset;

public class DriverVehicleInfo
{
    @com.google.gson.annotations.SerializedName("id")
    private String mId;
    public String getId() { return mId; }
    public final void setId(String id) { mId = id; }

    @com.google.gson.annotations.SerializedName("driverMobileNo")
    private String mDriverMobileNo;
    public String getDriverMobileNo() { return mDriverMobileNo; }
    public void setDriverMobileNo(String DriverMobileNo) { mDriverMobileNo = DriverMobileNo; }

    @com.google.gson.annotations.SerializedName("insuranceNo")
    private String mVehicleAssignedNumber;
    public String getVehicleAssignedNumber() { return mVehicleAssignedNumber; }
    public void setVehicleAssignedNumber(String VehicleAssignedNumber) { mVehicleAssignedNumber = VehicleAssignedNumber; }

    @com.google.gson.annotations.SerializedName("registrationNo")
    private String mRegistrationNo;
    public String getRegistrationNo() { return mRegistrationNo; }
    public void setRegistrationNo(String RegistrationNo) { mRegistrationNo = RegistrationNo;}

    @com.google.gson.annotations.SerializedName("imei")
    private String mIMEI;
    public String getIMEI() { return mIMEI; }
    public void setIMEI(String IMEI) { mIMEI = IMEI;}

    @com.google.gson.annotations.SerializedName("vehicleID")
    private int mVehicleID;
    public int getVehicleID() { return mVehicleID; }
    public void setVehicleID(int VehicleID) { mVehicleID = VehicleID; }

    @com.google.gson.annotations.SerializedName("driverID")
    private int mDriverID;
    public int getDriverID() { return mDriverID; }
    public void setDriverID(int DriverID) { mDriverID = DriverID; }

    @com.google.gson.annotations.SerializedName("tripStatus")
    private String mTripStatus;
    public String getTripStatus() { return mTripStatus; }
    public void setTripStatus(String TripStatus) {mTripStatus =  TripStatus;}

    @com.google.gson.annotations.SerializedName("shedInShedOutStatus")
    private String mShedInShedOutStatus;
    public String getShedInShedOutStatus() { return mShedInShedOutStatus; }
    public void setShedInShedOutStatus(String ShedInShedOutStatus) {mShedInShedOutStatus =  ShedInShedOutStatus;   }


    @com.google.gson.annotations.SerializedName("driverName")
    private String mDriverName;
    public String getDriverName() { return mDriverName; }
    public final void setDriverName(String DriverName) { mDriverName= DriverName; }

    @com.google.gson.annotations.SerializedName("lastKnownLocation")
    private String mLastKnownLocation;
    public String getLastKnownLocation() { return mLastKnownLocation; }
    public final void setLastKnownLocation(String LastKnownLocation) { mLastKnownLocation= LastKnownLocation; }

    public DriverVehicleInfo() { }

    @Override
    public boolean equals(Object o) {
        return o instanceof DriverVehicleInfo && ((DriverVehicleInfo) o).mId == mId;
    }

    @Override
    public String toString() {
        return getDriverName();
    }
}
