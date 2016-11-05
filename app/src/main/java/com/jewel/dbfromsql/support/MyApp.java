package com.jewel.dbfromsql.support;

import android.app.Application;
import android.content.Context;

import com.jewel.dbfromsql.model.MPerson;

/**
 * Created by Jewel on 5/12/2016.
 */
public class MyApp extends Application {
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        DBManager.createTable(new MPerson());
    }
}
