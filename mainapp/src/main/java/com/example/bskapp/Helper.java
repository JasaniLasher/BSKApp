package com.example.bskapp;

public class Helper {

    public static String ResolveTenant(String url)
    {
        if(url.toUpperCase().contains("IMEIRELATION"))
        {
            return "CommonDB";
        }
        else
        {
           MainApplication mApp = MainApplication.getContext();
           return mApp.getTenantID();
        }
    }
}
