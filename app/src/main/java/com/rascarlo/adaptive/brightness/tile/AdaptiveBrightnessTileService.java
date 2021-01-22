package com.rascarlo.adaptive.brightness.tile;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.appcompat.app.AlertDialog.Builder;

public class AdaptiveBrightnessTileService extends TileService {

    private static final int PERMISSION_DIALOG = 42;
    private static final int SETTING_NOT_FOUND_DIALOG = 24;

    @Override
    public void onCreate() {
        Log.d(getClass().getSimpleName(), "Created.");
        requestListeningState(this, new ComponentName(this, getClass()));
        AdaptiveBrightnessTileJobService.scheduleUpdateJob(this);
        super.onCreate();
    }

    @Override
    public void onStartListening() {
        Log.d(getClass().getSimpleName(), "Starting to listen...");
        updateTileResources();
        super.onStartListening();
        Log.d(getClass().getSimpleName(), "Started to listen.");
    }

    @Override
    public void onStopListening() {
        Log.d(getClass().getSimpleName(), "Stopping listening...");
        super.onStopListening();
        Log.d(getClass().getSimpleName(), "Stopped listening.");
    }

    @Override
    public void onTileRemoved() {
        AdaptiveBrightnessTileJobService.cancelJob(this);
        super.onTileRemoved();
    }

    @Override
    public void onClick() {
        Log.d(getClass().getSimpleName(), "Tapped on tile.");
        if (Settings.System.canWrite(this)) {
            Log.d(getClass().getSimpleName(), "This application has permissions to change system settings, so toggling Adaptive Brightness.");
            changeBrightnessMode();
        } else {
            Log.d(getClass().getSimpleName(), "This application does not have permissions to change system settings, so showing permissions dialog.");
            showDialog(PERMISSION_DIALOG);
        }
        super.onClick();
    }

    private void updateTileResources() {
        Log.d(getClass().getSimpleName(), "Getting tile so its appearance can be updated...");
        Tile tile = this.getQsTile();
        if (tile != null) {
            Log.d(getClass().getSimpleName(), "Got tile, updating tile appearance...");
            tile.setLabel(getString(R.string.adaptive_brightness));
            try {
                if (Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    Log.i(getClass().getSimpleName(), "Adaptive Brightness is on, so changing tile icon to not have slash through it and not be grayed out.");
                    tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_brightness_auto_white_24dp));
                    tile.setState(Tile.STATE_ACTIVE);
                } else {
                    Log.i(getClass().getSimpleName(), "Adaptive Brightness is off, so changing tile icon to have slash through it and be grayed out.");
                    tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_brightness_auto_off_white_24dp));
                    tile.setState(Tile.STATE_INACTIVE);
                }
            } catch (Settings.SettingNotFoundException e) {
                Log.w(getClass().getSimpleName(), "Adaptive Brightness settings were not found, so changing tile icon to not have slash through it and not be grayed out.");
                tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_brightness_auto_white_24dp));
                tile.setState(Tile.STATE_INACTIVE);
            }
            tile.updateTile();
            Log.d(getClass().getSimpleName(), "Committed tile appearance changes");
        } else {
            Log.e(getClass().getSimpleName(), "Could not get tile, not updating tile appearance.");
        }
    }

    private void changeBrightnessMode() {
        Log.d(getClass().getSimpleName(), "Toggling Adaptive Brightness...");
        try {
            boolean wasAutomatic = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            int newMode = wasAutomatic ? Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL : Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            Log.i(getClass().getSimpleName(), "Adaptive Brightness was previously "+(wasAutomatic ? "enabled" : "disabled")+", so "+(wasAutomatic ? "disabling" : "enabling")+" it now by setting screen_brightness_mode to "+newMode);
            if(!Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, newMode)){
                Log.e(getClass().getSimpleName(), "Failed to set Adaptive Brightness state due to settings database error.");
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.e(getClass().getSimpleName(), "settings not found");
            showDialog(SETTING_NOT_FOUND_DIALOG);
        }
    }

    private void showDialog(int whichDialog) {
        Builder builder = new Builder(this, R.style.AppTheme_AlertDialog);
        builder.setCancelable(true)
                .setIcon(R.drawable.ic_brightness_auto_white_24dp)
                .setTitle(R.string.app_name)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        switch (whichDialog) {
            case PERMISSION_DIALOG:
                builder.setMessage(R.string.permission_alert_dialog_message);
                builder.setPositiveButton(R.string.settings, (dialog, which) ->
                        startActivityAndCollapse(new Intent(getApplicationContext(), MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)));
                break;
            case SETTING_NOT_FOUND_DIALOG:
                builder.setMessage(R.string.setting_not_found_alert_dialog_message);
                builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel());
                break;
        }
        showDialog(builder.create());
    }
}