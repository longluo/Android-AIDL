package me.longluo.aidl.remote;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Random;

import me.longluo.aidl.IMyAidlInterface;

/**
 * Service implementation of the AIDL interface contract
 * for the remote application process.
 */
public class RemoteAidlService extends Service {

    private static final String TAG = "luolong";

    private boolean mIsLocalKaleidoscopeInterfaceBound;

    private IMyAidlInterface mLocalKaleidoscopeInterface;

    private static final int RANDOM_RANGE = 30;

    private Random mRandom;

    @Override
    public void onCreate() {
        super.onCreate();

        mRandom = new Random();

        Intent localServiceIntent = createLocalServiceIntent();

        if (localServiceIntent != null) {
            bindService(localServiceIntent, mLocalServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mIsLocalKaleidoscopeInterfaceBound) {
            unbindService(mLocalServiceConnection);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mKaleidoscopeInterface.asBinder();
    }

    private IMyAidlInterface mKaleidoscopeInterface = new IMyAidlInterface.Stub() {
        @Override
        public void drawLine(int x1, int y1, int x2, int y2) throws RemoteException {
            Log.d(TAG, "drawLine (" + x1 + "," + y1 + ") -> (" + x2 + "," + y2 + ")");

            handleDrawLineCommand(x1, y1, x2, y2);
        }
    };

    private Intent createLocalServiceIntent() {
        // Create a basic intent using the intent filter action of
        // the local service as per its manifest configuration.
        Intent intent = new Intent("LocalKaleidoscopeService");

        // Attempt to resolve the service through the Android OS
        ResolveInfo info = getPackageManager().resolveService(intent, Context.BIND_AUTO_CREATE);

        // If the service failed to resolve it could mean that the
        // remote app is not installed or something wasn't configured
        // correctly, so we can't really start/bind the service...
        if (info == null) {
            return null;
        }

        // Otherwise, grab the resolved package name and service name and
        // assign them to the intent and bingo we have an explicit service intent!
        intent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
        return intent;
    }

    private ServiceConnection mLocalServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocalKaleidoscopeInterface = IMyAidlInterface.Stub.asInterface(service);
            mIsLocalKaleidoscopeInterfaceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocalKaleidoscopeInterface = null;
            mIsLocalKaleidoscopeInterfaceBound = false;
        }
    };

    private void handleDrawLineCommand(int x1, int y1, int x2, int y2) {
        Log.d(TAG, "handleDrawLineCommand (" + x1 + "," + y1 + ") -> (" + x2 + "," + y2 + ")");

        if (mIsLocalKaleidoscopeInterfaceBound) {
            try {
                mLocalKaleidoscopeInterface.drawLine(
                        applyNoise(x1),
                        applyNoise(y1),
                        applyNoise(x2),
                        applyNoise(y2));

                mLocalKaleidoscopeInterface.drawLine(
                        applyNoise(y1),
                        applyNoise(x1),
                        applyNoise(y2),
                        applyNoise(x2));

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private int applyNoise(int input) {
        return input + mRandom.nextInt(RANDOM_RANGE) - (RANDOM_RANGE / 2);
    }

}
