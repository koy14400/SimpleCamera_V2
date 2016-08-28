package com.example.koy14400.simplecamera_v2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.koy14400.simplecamera_v2.SimpleCamera2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koy14400 on 2016/8/29.
 * Use to check Permission
 */
public class PermissionController {
    private static final String TAG = SimpleCamera2.TAG;
    private static final int sRequestCode = 100;

    public static final String[] Permission_String = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    public static boolean[] Permission_Granted = {
            false,
            false,
    };

    public static boolean isCameraGranted() {
        return Permission_Granted[0];
    }

    public static void checkPermission(Activity mApp) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Target API >= M, need grant permission.");
            checkAndGrantPermission(mApp);
        } else {
            Log.d(TAG, "Target API < M, not need grant permission.");
        }
    }

    @TargetApi(23)
    private static boolean checkAndGrantPermission(Activity mApp) {
        List<String> list = new ArrayList<>(Permission_String.length);
        String[] needGrantPermission;
        for (int i = 0; i < Permission_String.length; i++) {
            String s = Permission_String[i];
            if (mApp.checkSelfPermission(s) == PackageManager.PERMISSION_DENIED) {
                list.add(s);
                Permission_Granted[i] = false;
                Log.d(TAG, "Need to grant permission " + s);
            } else {
                Permission_Granted[i] = true;
                Log.d(TAG, "Not need to grant permission " + s);
            }
        }
        if (list.size() > 0) {
            needGrantPermission = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                needGrantPermission[i] = list.get(i);
            }
            mApp.requestPermissions(needGrantPermission, sRequestCode);
        }
        return true;
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != sRequestCode) {
            Log.w(TAG, "onRequestPermissionsResult request code not correct. RequestCode:" + requestCode);
            return;
        }
        for (int i = 0; i < permissions.length; i++) {
            for (int j = 0; j < Permission_String.length; j++) {
                if (permissions[i].equalsIgnoreCase(Permission_String[j])) {
                    Permission_Granted[j] = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                    if (Permission_Granted[j]) {
                        Log.d(TAG, Permission_String[j] + " is granted permission.");
                    } else {
                        Log.w(TAG, Permission_String[j] + " is not granted permission.");
                    }
                }
            }
        }
    }
}
