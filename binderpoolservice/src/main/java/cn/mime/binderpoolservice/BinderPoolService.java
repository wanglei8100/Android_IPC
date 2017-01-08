package cn.mime.binderpoolservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BinderPoolService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new BinderPoolImpl();
    }
}
