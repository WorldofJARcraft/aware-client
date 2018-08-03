package com.aware;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.aware.ui.PermissionsHandler;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PermissionService extends JobService {
    private static final String NOTIFICATION_ID = "PERMISSION_NOTIFICATION";
    public static final int SYNC_SERVICE_JOB_ID = 11234;
   public static final int PERMISSION_CHECK_RATE = 10000;
    private void notifyUser(Context mContext, String message, String titel, boolean dismiss, boolean indetermined, int id) {
        NotificationManager notManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!dismiss) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_ID);
            mBuilder.setSmallIcon(com.aware.R.drawable.ic_stat_aware_sync);
            mBuilder.setContentTitle(titel);
            mBuilder.setContentText(message);
            mBuilder.setAutoCancel(true);
            mBuilder.setOnlyAlertOnce(true); //notify the user only once
            mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS); //we only blink the LED, nothing else.
            //mBuilder.setProgress(100, 100, indetermined);
            Intent resultIntent = new Intent(this,PermissionsHandler.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            PendingIntent clickIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(clickIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                mBuilder.setChannelId(Aware.AWARE_NOTIFICATION_ID);

            try {
                notManager.notify(id, mBuilder.build());
            } catch (NullPointerException e) {
                if (Aware.DEBUG) Log.d(Aware.TAG, "Notification exception: " + e);
            }
        } else {
            try {
                notManager.cancel(id);
            } catch (NullPointerException e) {
                if (Aware.DEBUG) Log.d(Aware.TAG, "Notification exception: " + e);
            }
        }
    }
    @Override
    public boolean onStartJob(JobParameters params) {
        System.out.println("JobService aufgerufen!");
        Intent permissions = new Intent(this, PermissionsHandler.class);
        permissions.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        permissions.putExtra(PermissionsHandler.EXTRA_REDIRECT_SERVICE, getPackageName() + "/" + getClass().getName()); //restarts plugin once permissions are accepted
        if(!AwareApplication.getRequested_permissions().isEmpty())
            notifyUser(getApplicationContext(),getResources().getString(R.string.permission_needed),getResources().getString(R.string.please_grant_permission),false,true,2);
        else notifyUser(getApplicationContext(),"","",true,false,2);
            scheduleRefresh();
        return true;
    }
    private void scheduleRefresh() {
        JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder( SYNC_SERVICE_JOB_ID,
                new ComponentName( getPackageName(),
                        PermissionService.class.getName() ) );
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            builder.setMinimumLatency(PERMISSION_CHECK_RATE);
        }
        else{
            builder.setPeriodic(PERMISSION_CHECK_RATE);
        }
        if (mJobScheduler != null && mJobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {
            //If something goes wrong
            Log.d(Aware_Preferences.DEBUG_TAG, "JobScheduler konnte nicht erstellt werden.");
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobFinished(params, true);
        return false;
    }

}
