package me.longluo.aidl.local;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import me.longluo.aidl.IMyAidlInterface;
import me.longluo.aidl.local.events.DrawLineEvent;

/**
 * Service for the local application process that
 * uses our AIDL contract when something binds to it.
 */
public class LocalAidlService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMyAidlInterface.asBinder();
    }

    /**
     * This is the secret sauce, our service will create an implementation of
     * the AIDL contract 'stub' which can then be returned as the 'binder'
     * when the 'onBind' method is invoked on the service.
     * <p>
     * When the consumer of the service is connected and invokes methods via
     * AIDL, this is where they will be executed.
     */
    private final IMyAidlInterface mMyAidlInterface = new IMyAidlInterface.Stub() {

        @Override
        public void drawLine(int x1, int y1, int x2, int y2) throws RemoteException {
            Log.d("luolong", "Local drawLine (" + x1 + "," + y1 + ") -> (" + x2 + "," + y2 + ")");

            EventBus.getDefault().post(new DrawLineEvent(x1, y1, x2, y2));
        }
    };
}
