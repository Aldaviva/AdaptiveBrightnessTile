package com.rascarlo.adaptive.brightness.tile;

import android.app.job.JobInfo;
import android.app.job.JobInfo.TriggerContentUri;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

public class AdaptiveBrightnessTileJobService extends JobService {

    private static final int ADAPTIVE_BRIGHTNESS_TILE_JOB_ID = 234299;

    public static void scheduleUpdateJob(Context context) {
        ComponentName componentName = new ComponentName(context, AdaptiveBrightnessTileJobService.class);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        if (jobScheduler != null) {
            int result = jobScheduler.schedule(new JobInfo.Builder(ADAPTIVE_BRIGHTNESS_TILE_JOB_ID, componentName)
                    .addTriggerContentUri(new TriggerContentUri(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                            TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
                    .setTriggerContentMaxDelay(0)
                    .setTriggerContentUpdateDelay(0)
                    .setRequiresCharging(false)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                    .setRequiresDeviceIdle(false)
                    .build());

            if(result == JobScheduler.RESULT_SUCCESS) {
                Log.d(AdaptiveBrightnessTileJobService.class.getSimpleName(), "Scheduled job successfully.");
            } else {
                Log.e(AdaptiveBrightnessTileJobService.class.getSimpleName(), "Failed to schedule job.");
            }
        } else {
            Log.e(AdaptiveBrightnessTileJobService.class.getSimpleName(), "No job scheduler, not scheduling job.");
        }
    }

    public static void cancelJob(Context context) {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        if (jobScheduler != null) {
            jobScheduler.cancel(ADAPTIVE_BRIGHTNESS_TILE_JOB_ID);
        }
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(getClass().getSimpleName(), "Job started, requesting tile listening state.");
        AdaptiveBrightnessTileService.requestListeningState(this, new ComponentName(this, AdaptiveBrightnessTileService.class));
        Log.d(getClass().getSimpleName(), "Scheduling update job.");
        scheduleUpdateJob(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}