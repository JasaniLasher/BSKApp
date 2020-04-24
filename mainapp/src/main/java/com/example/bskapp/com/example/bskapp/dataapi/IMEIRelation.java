package com.example.bskapp.com.example.bskapp.dataapi;
import com.microsoft.windowsazure.mobileservices.table.DateTimeOffset;

public class IMEIRelation
{
    @com.google.gson.annotations.SerializedName("id")
    private String mId;
    public String getId() { return mId; }
    public final void setId(String id) { mId = id; }

    @com.google.gson.annotations.SerializedName("branchId")
    private int mBranchId;
    public int getBranchId() { return mBranchId; }
    public void setBranchId(int BranchId) { mBranchId = BranchId; }

    @com.google.gson.annotations.SerializedName("companyId")
    private int mCompanyId;
    public int getCompanyId() { return mCompanyId; }
    public void setCompanyId(int CompanyId) { mCompanyId = CompanyId; }

    @com.google.gson.annotations.SerializedName("imei")
    private String mIMEI;
    public String getIMEI() { return mIMEI; }
    public  void setIMEI(String IMEI) { mIMEI= IMEI; }

    @com.google.gson.annotations.SerializedName("branchName")
    private String mBranchName;
    public String getBranchName() { return mBranchName; }
    public  void setBranchName(String BranchName) { mBranchName= BranchName; }

    @com.google.gson.annotations.SerializedName("databaseName")
    private String mDatabaseName;
    public String getDatabaseName() { return mDatabaseName; }
    public  void setDatabaseName(String DatabaseName) { mDatabaseName= DatabaseName; }

    @com.google.gson.annotations.SerializedName("notificationToken")
    private String mNotificationToken;
    public String getNotificationToken() { return mNotificationToken; }
    public  void setNotificationToken(String NotificationToken) { mNotificationToken= NotificationToken; }








    public IMEIRelation() { }

    @Override
    public boolean equals(Object o) {
        return o instanceof IMEIRelation && ((IMEIRelation) o).mId == mId;
    }

    @Override
    public String toString() {
        return getIMEI();
    }
}
