package com.example.bskapp.com.example.bskapp.dataapi;

import com.microsoft.windowsazure.mobileservices.table.DateTimeOffset;

public class DriverLocationInfo
{
    @com.google.gson.annotations.SerializedName("id")
    private String mId;
    public String getId() { return mId; }
    public final void setId(String id) { mId = id; }


    @com.google.gson.annotations.SerializedName("driverID")
    private int mDriverID;
    public int getDriverID() { return mDriverID; }
    public void setDriverID(int DriverID) { mDriverID = DriverID; }


    @com.google.gson.annotations.SerializedName("lastKnownLocation")
    private String mLastKnownLocation;
    public String getLastKnownLocation() { return mLastKnownLocation; }
    public final void setLastKnownLocation(String LastKnownLocation) { mLastKnownLocation= LastKnownLocation; }

    @com.google.gson.annotations.SerializedName("branchId")
    private int mBranchId;
    public int getBranchId() { return mBranchId; }
    public void setBranchId(int BranchId) { mBranchId = BranchId; }

    @com.google.gson.annotations.SerializedName("companyId")
    private int mCompanyId;
    public int getCompanyId() { return mCompanyId; }
    public void setCompanyId(int CompanyId) { mCompanyId = CompanyId; }

    @com.google.gson.annotations.SerializedName("latitude")
    private double mlatitude;
    public double getLatitude() { return mlatitude; }
    public void setLatitude(double Latitude) { mlatitude = Latitude; }

    @com.google.gson.annotations.SerializedName("longitude")
    private double mlongitude;
    public double getLongitude() { return mlongitude; }
    public void setLongitude(double Longitude) { mlongitude = Longitude; }

    public DriverLocationInfo() { }

    @Override
    public boolean equals(Object o) {
        return o instanceof DriverLocationInfo && ((DriverLocationInfo) o).mId == mId;
    }

    @Override
    public String toString() {
        return getLastKnownLocation();
    }
}
