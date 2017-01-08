package cn.mime.binderpool_shareclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import cn.mime.binderpoolservice.IBinderPool;
import cn.mime.binderpoolservice.IShareContent;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private IShareContent mRemoteService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"onServiceConnected  connected success !");
            IBinderPool binderPool = IBinderPool.Stub.asInterface(service);
            try {
                mRemoteService = IShareContent.Stub.asInterface(binderPool.getShareContentBinder());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"onServiceDisconnected  connected break ! try to connect again !");
            bindRemoteService();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void bindService(View view){
        bindRemoteService();
    }

    private void bindRemoteService() {
        Intent intent = new Intent();
        intent.setAction("cn.mime.binderpoolservice.BinderPoolService");
        intent.setPackage("cn.mime.binderpoolservice");
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }

    public void callRemoteMethod1(View view){
        if (mRemoteService !=null){
            try {
                boolean shareStatus = mRemoteService.share("分享的内容。。。");
                Toast.makeText(this,shareStatus+"",Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
