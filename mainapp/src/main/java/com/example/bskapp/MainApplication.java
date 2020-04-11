package com.example.bskapp;

import android.app.Application;

public class MainApplication extends Application {

    private String mTenantID;

    public String getTenantID() {
        return mTenantID;
    }

    public void setTenantID(String tenantID) {
        this.mTenantID = tenantID;
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

    private static MainApplication mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static MainApplication getContext() {
        return mContext;
    }
}
