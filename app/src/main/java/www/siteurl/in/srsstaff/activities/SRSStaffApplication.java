package www.siteurl.in.srsstaff.activities;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.evernote.android.job.JobManager;

/**
 * Created by siteurl on 11/4/18.
 */

public class SRSStaffApplication extends Application {
    private static SRSStaffApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        //ever note
        JobManager.create(this).addJobCreator(new DemoJobCreator());
        int currentApi = Build.VERSION.SDK_INT;
        if (currentApi == Build.VERSION_CODES.M) {
            JobManager.instance().getConfig().setAllowSmallerIntervalsForMarshmallow(true);
        }

        mInstance = this;
    }

    public static synchronized SRSStaffApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}


