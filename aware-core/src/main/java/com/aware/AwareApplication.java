package com.aware;

import android.app.Application;
import android.content.Context;

import java.util.Vector;

/**
 * Provides static access to Application context.
 */
public class AwareApplication extends Application {
    private static Application application = null;

    /**
     * Acces to Application object.
     * @return null when uninitialised, else Application object.
     */
    public static Application getApplication() {
        return application;
    }
    private static final Vector<String> requested_permissions = new Vector<>();

    public static Vector<String> getRequested_permissions() {
        return requested_permissions;
    }

    /**
     * Accesses Application context.
     * @return null when application not launched yet, else Application context.
     */
    public static Context getContext() {
        if(application==null)
            return null;
        return application.getApplicationContext();
    }
    @Override
    public void onCreate(){
        super.onCreate();
        application = this;
    }
}
