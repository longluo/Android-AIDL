package me.longluo.aidl.local;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import me.longluo.aidl.IMyAidlInterface;
import me.longluo.aidl.local.events.DrawLineEvent;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "luolong";

    private boolean mIsRemoteKaleidoscopeInterfaceBound;

    private IMyAidlInterface mRemoteKaleidoscopeInterface;
	
    private InputDrawingCanvas mInputDrawingCanvas;
	
    private OutputDrawingCanvas mOutputDrawingCanvas;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_clear) {
            mOutputDrawingCanvas.clear();
            mInputDrawingCanvas.clear();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInputDrawingCanvas = findViewById(R.id.input_drawing_canvas);
        mInputDrawingCanvas.setDelegate(new InputDrawingCanvas.Delegate() {
            @Override
            public void onLineDrawn(int x1, int y1, int x2, int y2) {
                if (mIsRemoteKaleidoscopeInterfaceBound) {
                    try {
                        mRemoteKaleidoscopeInterface.drawLine(x1, y1, x2, y2);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mOutputDrawingCanvas = findViewById(R.id.output_drawing_canvas);

        Intent remoteServiceIntent = createRemoteServiceIntent();
        if (remoteServiceIntent != null) {
            bindService(remoteServiceIntent, mRemoteServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        mOutputDrawingCanvas.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        mOutputDrawingCanvas.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mIsRemoteKaleidoscopeInterfaceBound) {
            unbindService(mRemoteServiceConnection);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void processEvent(@NonNull DrawLineEvent event) {
        if (mOutputDrawingCanvas != null) {
            mOutputDrawingCanvas.drawLine(event.x1, event.y1, event.x2, event.y2);
            Toast.makeText(this, "draw: " + event.x1 + "," + event.x2 , Toast.LENGTH_SHORT).show();
        }
		
        Toast.makeText(this, "Local Main Activity received DrawLineEvent!", Toast.LENGTH_SHORT).show();
    }

    private final ServiceConnection mRemoteServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected " + name + ", service = " + service);

            mRemoteKaleidoscopeInterface = IMyAidlInterface.Stub.asInterface(service);
            mIsRemoteKaleidoscopeInterfaceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteKaleidoscopeInterface = null;
            mIsRemoteKaleidoscopeInterfaceBound = false;
        }
    };

    private Intent createRemoteServiceIntent() {
        // Create a basic intent using the intent filter action of
        // the remote service as per its manifest configuration.
        Intent intent = new Intent("RemoteKaleidoscopeService");

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
}
