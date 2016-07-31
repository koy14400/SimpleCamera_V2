package com.example.koy14400.simplecamera_v2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.koy14400.simplecamera_v2.cambase2.CamBaseV2;
import com.example.koy14400.simplecamera_v2.previewtype.CamGLPreviewV2;
import com.example.koy14400.simplecamera_v2.previewtype.CamPreviewV2;
import com.example.koy14400.simplecamera_v2.previewtype.CamSurfacePreviewV2;
import com.example.koy14400.simplecamera_v2.previewtype.CamTexturePreviewV2;

import java.util.ArrayList;
import java.util.List;

public class SimpleCamera2 extends AppCompatActivity {
    public static final String TAG = "SimpleCamera2";
    public static final String[] Permission_String = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    public static boolean[] Permission_Granted = {
            false,
            false,
    };
    public CamPreviewV2.PreviewViewType mViewType = CamPreviewV2.PreviewViewType.GLSurfaceView;

    //    private Camera mCamera = null;
    private Activity mApp = null;
    private CamBaseV2 mCamBase = null;
    private CamPreviewV2 mCamPreview = null;
    private LinearLayout mRootView = null;
    private Button mCaptureButton = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "LifeCycle, onCreate");
        super.onCreate(savedInstanceState);
        mApp = this;

        // set full screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_surfaceview);
        // make screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mRootView = (LinearLayout) findViewById(R.id.root_view);
        setupUIVariable();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Target API >= M, need grant permission.");
            checkAndGrantPermission();
        } else {
            Log.d(TAG, "Target API < M, not need grant permission.");
        }
        mCamBase = new CamBaseV2(mApp);
        mCamPreview = getPreviewView(mViewType);
    }

    private void setupUIVariable() {
        mCaptureButton = (Button) findViewById(R.id.NextPreviewType);
        mCaptureButton.setOnClickListener(mCaptureButtonClickListener);
        mCaptureButton = (Button) findViewById(R.id.Capture);
        mCaptureButton.setOnClickListener(mCaptureButtonClickListener);
    }

    @TargetApi(23)
    private boolean checkAndGrantPermission() {
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
            for (int i = 0;i<list.size();i++) {
                needGrantPermission[i] = list.get(i);
            }
            mApp.requestPermissions(needGrantPermission, 100);
        }
        return true;
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "LifeCycle, onResume");
        super.onResume();
        if (Permission_Granted[0]) {
            mCamBase.onActivityResume();
        } else {
            Log.w(TAG, "No Camera permission, CamBase resume fail.");
        }
        mRootView.addView(mCamPreview.getView());
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "LifeCycle, onPause");
        super.onPause();
        if (mCamBase != null) {
            mCamBase.onActivityPause();
        }
        if (mRootView != null) {
            mRootView.removeAllViews();
        }
        // return from screen always on state
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "LifeCycle, onDestroy");
        super.onDestroy();
    }

    View.OnClickListener mCaptureButtonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Capture:
                    mCamBase.takePicture();
                    break;
                case R.id.NextPreviewType:
                    changeNextPreviewViewType();
                    break;
            }
        }
    };

    private CamPreviewV2.OnSurfaceReadyListener mOnSurfaceReadyListener = new CamPreviewV2.OnSurfaceReadyListener() {
        public void onSurfaceReady(Surface surface) {
            mCamBase.startPreview(surface);
        }
    };

    public void changeNextPreviewViewType() {
        int nextTypeOrdinal = mViewType.ordinal() + 1;
        if (nextTypeOrdinal >= CamPreviewV2.PreviewViewType.values().length)
            nextTypeOrdinal = 0;
        mViewType = CamPreviewV2.PreviewViewType.values()[nextTypeOrdinal];
        mCamBase.onActivityPause();
        mRootView.removeAllViews();
        mCamBase.onActivityResume();
        mCamPreview = getPreviewView(mViewType);
        mRootView.addView(mCamPreview.getView());
    }

    private CamPreviewV2 getPreviewView(CamPreviewV2.PreviewViewType viewType) {
        switch (viewType) {
            case SurfaceView:
                return new CamSurfacePreviewV2(this, mViewType, mOnSurfaceReadyListener);
            case TextureView:
                return new CamTexturePreviewV2(this, mViewType, mOnSurfaceReadyListener);
            case GLSurfaceView:
                return new CamGLPreviewV2(this, mViewType, mOnSurfaceReadyListener);
        }
        return new CamSurfacePreviewV2(this, mViewType, mOnSurfaceReadyListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i<permissions.length;i++) {
            for (int j = 0; j<Permission_String.length; j++) {
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
