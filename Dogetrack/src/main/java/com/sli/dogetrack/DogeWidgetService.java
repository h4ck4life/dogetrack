package com.sli.dogetrack;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DogeWidgetService extends Service {

    private MainActivity m;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

    }

}
